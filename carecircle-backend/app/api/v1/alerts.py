from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID, uuid4
from datetime import datetime, timezone
from typing import List
from app.api.deps import get_current_user_id, verify_caregiver_access
from app.services.store import store
from app.services.rules_engine import rules_engine
from app.services.websocket_manager import ws_manager
from app.models.enums import AlertType, AlertSeverity, AlertStatus
from app.schemas.alerts import (
    AlertResponse, AlertAcknowledgeRequest, AlertResolveRequest, ManualSOSRequest,
    ThresholdRuleList, ThresholdRuleItem
)

router = APIRouter(tags=["Alerts & Thresholds"])

@router.get("/patients/{patient_id}/thresholds", response_model=ThresholdRuleList)
async def get_patient_thresholds(patient_id: UUID, _auth = Depends(verify_caregiver_access)):
    async with store.lock:
        rules = [
            ThresholdRuleItem(
                metric=r["metric"],
                min_value=r["min_value"],
                max_value=r["max_value"],
                severity=r["severity"],
                sustained_seconds=r["sustained_seconds"],
                max_age_seconds=r["max_age_seconds"],
                enabled=r["enabled"]
            )
            for (p_id, m), r in store.threshold_rules.items()
            if p_id == patient_id
        ]
        return ThresholdRuleList(rules=rules)

@router.put("/patients/{patient_id}/thresholds", response_model=ThresholdRuleList)
async def update_patient_thresholds(
    patient_id: UUID,
    payload: ThresholdRuleList,
    _auth = Depends(verify_caregiver_access)
):
    async with store.lock:
        for r in payload.rules:
            store.threshold_rules[(patient_id, r.metric)] = {
                "id": uuid4(),
                "patient_id": patient_id,
                "metric": r.metric,
                "min_value": r.min_value,
                "max_value": r.max_value,
                "severity": r.severity,
                "sustained_seconds": r.sustained_seconds,
                "max_age_seconds": r.max_age_seconds,
                "enabled": r.enabled
            }
        return payload

@router.get("/patients/{patient_id}/alerts", response_model=List[AlertResponse])
async def list_patient_alerts(patient_id: UUID, _auth = Depends(verify_caregiver_access)):
    async with store.lock:
        alerts = [a for a in store.alerts.values() if a["patient_id"] == patient_id]
        alerts.sort(key=lambda x: x["opened_at"], reverse=True)
        return alerts

@router.post("/alerts/{alert_id}/acknowledge", response_model=AlertResponse)
async def acknowledge_alert(
    alert_id: UUID,
    body: AlertAcknowledgeRequest = None,
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Stops the escalation ladder immediately.
    Records acknowledging caregiver and timestamp in audit events.
    """
    now = datetime.now(timezone.utc)
    async with store.lock:
        alert = store.alerts.get(alert_id)
        if not alert:
            raise HTTPException(status_code=404, detail="Alert not found")
        
        alert["status"] = AlertStatus.ACKNOWLEDGED
        alert["acknowledged_by"] = current_user_id
        alert["acknowledged_at"] = now
        
        note = body.note if body else "Acknowledged via app"
        store.alert_events.append({
            "id": uuid4(),
            "alert_id": alert_id,
            "event_type": "ALERT_ACKNOWLEDGED",
            "recipient_id": current_user_id,
            "details": {"note": note},
            "created_at": now
        })
        
        await ws_manager.broadcast_patient_event(alert["patient_id"], "ALERT_ACKNOWLEDGED", alert)
        return alert

@router.post("/alerts/{alert_id}/resolve", response_model=AlertResponse)
async def resolve_alert(
    alert_id: UUID,
    body: AlertResolveRequest = None,
    current_user_id: UUID = Depends(get_current_user_id)
):
    now = datetime.now(timezone.utc)
    async with store.lock:
        alert = store.alerts.get(alert_id)
        if not alert:
            raise HTTPException(status_code=404, detail="Alert not found")
        
        alert["status"] = AlertStatus.RESOLVED
        alert["resolved_at"] = now
        
        reason = body.reason if body else "Resolved by user"
        store.alert_events.append({
            "id": uuid4(),
            "alert_id": alert_id,
            "event_type": "ALERT_MANUALLY_RESOLVED",
            "recipient_id": current_user_id,
            "details": {"reason": reason},
            "created_at": now
        })
        
        await ws_manager.broadcast_patient_event(alert["patient_id"], "ALERT_RESOLVED", alert)
        return alert

@router.post("/sos", response_model=AlertResponse)
async def trigger_manual_sos(
    body: ManualSOSRequest,
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Patient Manual SOS button:
    Immediately creates a critical alert and fires Level 0 / 1 escalation.
    """
    now = datetime.now(timezone.utc)
    async with store.lock:
        alert_id = uuid4()
        alert_obj = {
            "id": alert_id,
            "patient_id": body.patient_id,
            "type": AlertType.MANUAL_SOS,
            "metric": None,
            "severity": AlertSeverity.CRITICAL,
            "status": AlertStatus.OPEN,
            "trigger_message": body.note or "EMERGENCY: Patient activated manual SOS",
            "opened_at": now,
            "acknowledged_by": None,
            "acknowledged_at": None,
            "resolved_at": None,
            "escalation_level": 0,
            "last_escalated_at": now
        }
        store.alerts[alert_id] = alert_obj
        
        store.alert_events.append({
            "id": uuid4(),
            "alert_id": alert_id,
            "event_type": "MANUAL_SOS_TRIGGERED",
            "recipient_id": current_user_id,
            "details": {"note": body.note},
            "created_at": now
        })
        
        await rules_engine._dispatch_escalation(alert_obj, level=0)
        await ws_manager.broadcast_patient_event(body.patient_id, "ALERT_OPENED", alert_obj)
        return alert_obj

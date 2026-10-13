from datetime import datetime, timezone, timedelta
from uuid import UUID, uuid4
from typing import List, Dict, Any, Optional
from app.models.enums import (
    VitalMetric, AlertType, AlertSeverity, AlertStatus, QualityStatus
)
from app.services.store import store
from app.services.websocket_manager import ws_manager
from app.integrations.notifications import expo_service, telegram_service, twilio_service
from app.core.logging import logger

class RulesEngine:
    async def evaluate_readings(self, patient_id: UUID, new_readings: List[Dict[str, Any]]):
        """
        Evaluates threshold rules on valid, sufficiently recent readings.
        Requires breach to be sustained for sustained_seconds to avoid artifact spikes.
        Auto-resolves open alerts when readings return to normal.
        """
        now = datetime.now(timezone.utc)
        
        async with store.lock:
            # Group candidate metrics
            metrics_to_check = set(r["metric"] for r in new_readings)
            
            for metric in metrics_to_check:
                rule = store.threshold_rules.get((patient_id, metric))
                if not rule or not rule.get("enabled", True):
                    continue
                    
                max_age_s = rule.get("max_age_seconds", 120)
                sustained_s = rule.get("sustained_seconds", 30)
                min_val = rule.get("min_value")
                max_val = rule.get("max_value")
                
                # Fetch recent valid readings for this metric within max_age
                cutoff_time = now - timedelta(seconds=max_age_s)
                recent_readings = [
                    v for v in store.vitals
                    if v["patient_id"] == patient_id
                    and v["metric"] == metric
                    and v["quality_status"] == QualityStatus.VALID
                    and not v.get("is_suppressed", False)
                    and v["measured_at"] >= cutoff_time
                ]
                recent_readings.sort(key=lambda x: x["measured_at"])
                
                if not recent_readings:
                    continue
                    
                latest = recent_readings[-1]
                val = latest["canonical_value"]
                
                is_breach = False
                breach_msg = ""
                if min_val is not None and val < min_val:
                    is_breach = True
                    breach_msg = f"{metric.value} low: {val}{latest['canonical_unit']} (min threshold: {min_val})"
                elif max_val is not None and val > max_val:
                    is_breach = True
                    breach_msg = f"{metric.value} high: {val}{latest['canonical_unit']} (max threshold: {max_val})"
                    
                # Check if there is an existing open threshold alert
                existing_alert = next(
                    (a for a in store.alerts.values() 
                     if a["patient_id"] == patient_id 
                     and a["metric"] == metric 
                     and a["type"] == AlertType.THRESHOLD 
                     and a["status"] in (AlertStatus.OPEN, AlertStatus.ESCALATED)),
                    None
                )
                
                if is_breach:
                    # Check sustained breach condition
                    sustained_cutoff = now - timedelta(seconds=sustained_s)
                    breach_readings = [
                        r for r in recent_readings 
                        if r["measured_at"] >= sustained_cutoff
                        and ((min_val is not None and r["canonical_value"] < min_val) or 
                             (max_val is not None and r["canonical_value"] > max_val))
                    ]
                    
                    # If breach has persisted across the sustained window or initial reading sustained
                    time_span = (latest["measured_at"] - recent_readings[0]["measured_at"]).total_seconds()
                    if len(breach_readings) >= 1 and (time_span >= sustained_s or len(recent_readings) == 1):
                        if not existing_alert:
                            # Create new open alert
                            new_alert_id = uuid4()
                            alert_obj = {
                                "id": new_alert_id,
                                "patient_id": patient_id,
                                "type": AlertType.THRESHOLD,
                                "metric": metric,
                                "severity": rule.get("severity", AlertSeverity.WARNING),
                                "status": AlertStatus.OPEN,
                                "trigger_message": breach_msg,
                                "opened_at": now,
                                "acknowledged_by": None,
                                "acknowledged_at": None,
                                "resolved_at": None,
                                "escalation_level": 0,
                                "last_escalated_at": now
                            }
                            store.alerts[new_alert_id] = alert_obj
                            
                            # Log audit event
                            store.alert_events.append({
                                "id": uuid4(),
                                "alert_id": new_alert_id,
                                "event_type": "ALERT_OPENED",
                                "channel": "engine",
                                "details": {"message": breach_msg, "value": val},
                                "created_at": now
                            })
                            
                            # Trigger level 0 notification
                            await self._dispatch_escalation(alert_obj, level=0)
                            
                            # Broadcast to WS
                            await ws_manager.broadcast_patient_event(patient_id, "ALERT_OPENED", alert_obj)
                else:
                    # Normal reading: auto-resolve open alert if any
                    if existing_alert:
                        existing_alert["status"] = AlertStatus.RESOLVED
                        existing_alert["resolved_at"] = now
                        store.alert_events.append({
                            "id": uuid4(),
                            "alert_id": existing_alert["id"],
                            "event_type": "ALERT_AUTO_RESOLVED",
                            "channel": "engine",
                            "details": {"message": f"Vitals returned to normal: {val}{latest['canonical_unit']}"},
                            "created_at": now
                        })
                        await ws_manager.broadcast_patient_event(patient_id, "ALERT_RESOLVED", existing_alert)

    async def _dispatch_escalation(self, alert: Dict[str, Any], level: int):
        now = datetime.now(timezone.utc)
        patient_id = alert["patient_id"]
        patient = store.profiles.get(patient_id, {})
        
        # Caregivers for patient in priority order
        accepted_links = [
            l for l in store.care_links.values()
            if l["patient_id"] == patient_id and l["status"] == "accepted"
        ]
        accepted_links.sort(key=lambda x: x.get("priority_order", 1))
        caregivers = [store.profiles.get(l["caregiver_id"]) for l in accepted_links if l["caregiver_id"] in store.profiles]
        
        title = f"CareCircle {alert['severity'].value.upper()} Alert"
        body = f"{patient.get('name', 'Patient')}: {alert['trigger_message']}"
        
        if level == 0:
            # Level 0: Push to primary caregiver
            if caregivers:
                primary = caregivers[0]
                token = primary.get("expo_push_token")
                if token:
                    await expo_service.send(token, title, body, {"alert_id": str(alert["id"])})
                store.alert_events.append({
                    "id": uuid4(),
                    "alert_id": alert["id"],
                    "event_type": "ESCALATION_LEVEL_0_PUSH",
                    "channel": "expo_push",
                    "recipient_id": primary["id"],
                    "details": {"caregiver": primary.get("name")},
                    "created_at": now
                })
        elif level == 1:
            # Level 1: Push + Telegram to ALL caregivers
            for cg in caregivers:
                token = cg.get("expo_push_token")
                if token:
                    await expo_service.send(token, title, f"[ESCALATED] {body}")
                tg_id = cg.get("telegram_chat_id")
                if tg_id:
                    await telegram_service.send(tg_id, title, f"[ESCALATED L1] {body}")
            store.alert_events.append({
                "id": uuid4(),
                "alert_id": alert["id"],
                "event_type": "ESCALATION_LEVEL_1_BROADCAST",
                "channel": "expo+telegram",
                "details": {"notified_count": len(caregivers)},
                "created_at": now
            })
        elif level >= 2:
            # Level 2: SMS then automated voice call
            for cg in caregivers:
                phone = cg.get("phone")
                if phone:
                    await twilio_service.send(phone, title, body, {"call_type": "sms"})
                    await twilio_service.send(phone, title, body, {"call_type": "voice"})
            store.alert_events.append({
                "id": uuid4(),
                "alert_id": alert["id"],
                "event_type": "ESCALATION_LEVEL_2_CALL",
                "channel": "twilio_voice_sms",
                "details": {"notified_count": len(caregivers)},
                "created_at": now
            })

rules_engine = RulesEngine()

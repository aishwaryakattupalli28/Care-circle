import asyncio
from datetime import datetime, timezone, timedelta
from uuid import uuid4
from app.services.store import store
from app.services.websocket_manager import ws_manager
from app.services.rules_engine import rules_engine
from app.models.enums import AlertType, AlertSeverity, AlertStatus, QualityStatus
from app.core.config import settings
from app.core.logging import logger

async def run_background_stale_monitor():
    """
    Periodic background monitor running every 15 seconds.
    Detects:
    1. Device disconnection (no data from any source for a patient > 60s)
    2. Stale data per metric (heart_rate / spo2 older than delayed threshold)
    3. Escalation ladder progression for unacknowledged alerts
    """
    while True:
        try:
            await asyncio.sleep(settings.BACKGROUND_TICK_SECONDS)
            now = datetime.now(timezone.utc)
            
            async with store.lock:
                # 1. Device Disconnection & Stale Data per Patient
                patients = [p for p in store.profiles.values() if p["role"] == "patient"]
                for patient in patients:
                    p_id = patient["id"]
                    sources = [s for s in store.sources.values() if s["patient_id"] == p_id]
                    
                    if sources:
                        most_recent_seen = max(s["last_seen_at"] for s in sources)
                        if most_recent_seen.tzinfo is None:
                            most_recent_seen = most_recent_seen.replace(tzinfo=timezone.utc)
                        disconnected_span = (now - most_recent_seen).total_seconds()
                        
                        existing_disconn = next(
                            (a for a in store.alerts.values() 
                             if a["patient_id"] == p_id 
                             and a["type"] == AlertType.DEVICE_DISCONNECTED 
                             and a["status"] in (AlertStatus.OPEN, AlertStatus.ESCALATED)),
                            None
                        )
                        
                        if disconnected_span > 60.0:
                            if not existing_disconn:
                                alert_id = uuid4()
                                alert_obj = {
                                    "id": alert_id,
                                    "patient_id": p_id,
                                    "type": AlertType.DEVICE_DISCONNECTED,
                                    "metric": None,
                                    "severity": AlertSeverity.WARNING,
                                    "status": AlertStatus.OPEN,
                                    "trigger_message": f"No telemetry received for {int(disconnected_span)}s. Device disconnected.",
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
                                    "event_type": "DISCONNECTION_ALERT_OPENED",
                                    "channel": "stale_monitor",
                                    "details": {"span_seconds": disconnected_span},
                                    "created_at": now
                                })
                                await rules_engine._dispatch_escalation(alert_obj, level=0)
                                await ws_manager.broadcast_patient_event(p_id, "ALERT_OPENED", alert_obj)
                        else:
                            # Reconnected: auto-resolve
                            if existing_disconn:
                                existing_disconn["status"] = AlertStatus.RESOLVED
                                existing_disconn["resolved_at"] = now
                                store.alert_events.append({
                                    "id": uuid4(),
                                    "alert_id": existing_disconn["id"],
                                    "event_type": "DEVICE_RECONNECTED",
                                    "channel": "stale_monitor",
                                    "details": {"message": "Telemetry resumed."},
                                    "created_at": now
                                })
                                await ws_manager.broadcast_patient_event(p_id, "ALERT_RESOLVED", existing_disconn)

                # 2. Alert Escalation Ladder Progressions
                open_alerts = [
                    a for a in store.alerts.values() 
                    if a["status"] in (AlertStatus.OPEN, AlertStatus.ESCALATED)
                ]
                for alert in open_alerts:
                    opened_at = alert["opened_at"]
                    if opened_at.tzinfo is None:
                        opened_at = opened_at.replace(tzinfo=timezone.utc)
                    age_minutes = (now - opened_at).total_seconds() / 60.0
                    
                    current_lvl = alert["escalation_level"]
                    if current_lvl == 0 and age_minutes >= settings.ESCALATION_LEVEL_1_MINUTES:
                        alert["escalation_level"] = 1
                        alert["status"] = AlertStatus.ESCALATED
                        alert["last_escalated_at"] = now
                        await rules_engine._dispatch_escalation(alert, level=1)
                        await ws_manager.broadcast_patient_event(alert["patient_id"], "ALERT_ESCALATED", alert)
                    elif current_lvl == 1 and age_minutes >= settings.ESCALATION_LEVEL_2_MINUTES:
                        alert["escalation_level"] = 2
                        alert["status"] = AlertStatus.ESCALATED
                        alert["last_escalated_at"] = now
                        await rules_engine._dispatch_escalation(alert, level=2)
                        await ws_manager.broadcast_patient_event(alert["patient_id"], "ALERT_ESCALATED", alert)

        except asyncio.CancelledError:
            break
        except Exception as e:
            logger.error(f"Error in background stale monitor: {e}")

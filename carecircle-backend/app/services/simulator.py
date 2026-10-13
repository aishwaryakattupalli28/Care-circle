import asyncio
import random
from datetime import datetime, timezone
from uuid import UUID, uuid4
from typing import Dict, Optional
from app.models.enums import VitalMetric, SourceKind, SyncMode, QualityStatus
from app.services.store import store
from app.services.normalizer import normalize_reading
from app.services.rules_engine import rules_engine
from app.services.websocket_manager import ws_manager
from app.services.freshness import calculate_freshness
from app.core.logging import logger

class PatientSimulatorService:
    """
    First-class simulator service supporting scenarios:
    - normal
    - tachycardia (high HR > 130 bpm)
    - low_spo2 (hypoxemia < 89%)
    - fever (temperature > 39.2 °C)
    - device_dropout (stops transmitting to trigger device_disconnected alert)
    - bp_manual (periodic manual blood pressure)
    Emits readings every 2 seconds with sync_mode=live.
    """
    def __init__(self):
        self.active_tasks: Dict[UUID, asyncio.Task] = {}
        self.active_scenarios: Dict[UUID, str] = {}

    def start_simulation(self, patient_id: UUID, scenario: str) -> bool:
        self.stop_simulation(patient_id)
        self.active_scenarios[patient_id] = scenario
        task = asyncio.create_task(self._simulation_loop(patient_id, scenario))
        self.active_tasks[patient_id] = task
        logger.info(f"Started simulation for patient {patient_id} with scenario '{scenario}'")
        return True

    def stop_simulation(self, patient_id: UUID) -> bool:
        if patient_id in self.active_tasks:
            self.active_tasks[patient_id].cancel()
            del self.active_tasks[patient_id]
        if patient_id in self.active_scenarios:
            del self.active_scenarios[patient_id]
        logger.info(f"Stopped simulation for patient {patient_id}")
        return True

    async def _simulation_loop(self, patient_id: UUID, scenario: str):
        try:
            # Locate simulator source
            sim_source = next(
                (s for s in store.sources.values() 
                 if s["patient_id"] == patient_id and s["kind"] == SourceKind.SIMULATOR),
                None
            )
            if not sim_source:
                sim_source_id = uuid4()
                sim_source = {
                    "id": sim_source_id,
                    "patient_id": patient_id,
                    "kind": SourceKind.SIMULATOR,
                    "device_name": "CareCircle Realtime Simulator",
                    "priority": 10,
                    "last_seen_at": datetime.now(timezone.utc)
                }
                store.sources[sim_source_id] = sim_source
            else:
                sim_source_id = sim_source["id"]

            while True:
                now = datetime.now(timezone.utc)
                
                # If device_dropout, update nothing and sleep
                if scenario == "device_dropout":
                    await asyncio.sleep(2.0)
                    continue
                    
                sim_source["last_seen_at"] = now
                readings_to_ingest = []

                if scenario == "normal":
                    hr = round(random.uniform(70.0, 78.0), 1)
                    spo2 = round(random.uniform(97.0, 99.0), 1)
                    temp = round(random.uniform(36.6, 37.1), 1)
                    resp = round(random.uniform(14.0, 18.0), 1)
                elif scenario == "tachycardia":
                    hr = round(random.uniform(135.0, 155.0), 1)  # Breach > 110 max
                    spo2 = round(random.uniform(95.0, 97.0), 1)
                    temp = round(random.uniform(36.8, 37.2), 1)
                    resp = round(random.uniform(22.0, 26.0), 1)
                elif scenario == "low_spo2":
                    hr = round(random.uniform(88.0, 98.0), 1)
                    spo2 = round(random.uniform(84.0, 88.0), 1)  # Breach < 92 min
                    temp = round(random.uniform(36.5, 37.0), 1)
                    resp = round(random.uniform(24.0, 28.0), 1)
                elif scenario == "fever":
                    hr = round(random.uniform(105.0, 118.0), 1)
                    spo2 = round(random.uniform(96.0, 98.0), 1)
                    temp = round(random.uniform(39.1, 40.2), 1)  # Breach > 38.3 max
                    resp = round(random.uniform(20.0, 24.0), 1)
                elif scenario == "bp_manual":
                    hr = round(random.uniform(72.0, 80.0), 1)
                    spo2 = round(random.uniform(98.0, 99.0), 1)
                    temp = round(random.uniform(36.7, 37.0), 1)
                    resp = round(random.uniform(14.0, 16.0), 1)
                    # Add blood pressure readings
                    readings_to_ingest.append((VitalMetric.BLOOD_PRESSURE_SYSTOLIC, 148.0, "mmHg", SyncMode.MANUAL))
                    readings_to_ingest.append((VitalMetric.BLOOD_PRESSURE_DIASTOLIC, 96.0, "mmHg", SyncMode.MANUAL))
                else:
                    hr, spo2, temp, resp = 72.0, 98.0, 36.8, 16.0

                readings_to_ingest.extend([
                    (VitalMetric.HEART_RATE, hr, "bpm", SyncMode.LIVE),
                    (VitalMetric.SPO2, spo2, "%", SyncMode.LIVE),
                    (VitalMetric.BODY_TEMPERATURE, temp, "°C", SyncMode.LIVE),
                    (VitalMetric.RESPIRATORY_RATE, resp, "rpm", SyncMode.LIVE),
                ])

                inserted = []
                for metric, raw_val, raw_unit, sync_m in readings_to_ingest:
                    c_val, c_unit, quality = normalize_reading(metric, raw_val, raw_unit)
                    reading_obj = {
                        "id": uuid4(),
                        "patient_id": patient_id,
                        "metric": metric,
                        "value": raw_val,
                        "canonical_value": c_val,
                        "unit": raw_unit,
                        "canonical_unit": c_unit,
                        "measured_at": now,
                        "received_at": now,
                        "source_id": sim_source_id,
                        "source_record_id": f"sim_{now.timestamp()}_{metric.value}",
                        "sync_mode": sync_m,
                        "quality_status": quality,
                        "is_suppressed": False
                    }
                    store.vitals.append(reading_obj)
                    inserted.append(reading_obj)
                    
                    # Compute freshness
                    freshness, age_s = calculate_freshness(metric, SourceKind.SIMULATOR, now, now)
                    ws_payload = {
                        **reading_obj,
                        "freshness": freshness.value,
                        "age_seconds": age_s,
                        "source_kind": SourceKind.SIMULATOR.value
                    }
                    await ws_manager.broadcast_patient_event(patient_id, "VITAL_READING", ws_payload)

                # Run rules engine evaluation
                await rules_engine.evaluate_readings(patient_id, inserted)

                await asyncio.sleep(2.0)
        except asyncio.CancelledError:
            pass
        except Exception as e:
            logger.error(f"Error in simulator loop: {e}")

simulator_service = PatientSimulatorService()

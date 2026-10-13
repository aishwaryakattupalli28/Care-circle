import asyncio
from datetime import datetime, timezone, timedelta
from uuid import UUID, uuid4
from typing import Dict, List, Optional, Any
from app.models.enums import (
    UserRole, CareLinkStatus, SourceKind, VitalMetric, 
    SyncMode, QualityStatus, AlertType, AlertSeverity, AlertStatus
)

class DataStore:
    """
    Thread-safe, async-friendly in-memory backing store for CareCircle.
    Provides persistence for demo, local development, tests, and acts as fallback 
    when external Supabase instance is not configured.
    """
    def __init__(self):
        self.lock = asyncio.Lock()
        
        # Tables
        self.profiles: Dict[UUID, Dict[str, Any]] = {}
        self.care_links: Dict[UUID, Dict[str, Any]] = {}
        self.sources: Dict[UUID, Dict[str, Any]] = {}
        self.vitals: List[Dict[str, Any]] = []
        self.threshold_rules: Dict[Tuple[UUID, VitalMetric], Dict[str, Any]] = {}
        self.alerts: Dict[UUID, Dict[str, Any]] = {}
        self.alert_events: List[Dict[str, Any]] = []
        self.audit_logs: List[Dict[str, Any]] = []
        
        self._seed_default_demo_data()

    def _seed_default_demo_data(self):
        # Demo Patient: Eleanor Vance
        p_id = UUID("11111111-1111-1111-1111-111111111111")
        self.profiles[p_id] = {
            "id": p_id,
            "role": UserRole.PATIENT,
            "name": "Eleanor Vance (Patient)",
            "phone": "+15550198234",
            "telegram_chat_id": "987654321",
            "expo_push_token": "ExponentPushToken[demo_patient_token]",
            "created_at": datetime.now(timezone.utc)
        }
        
        # Demo Caregiver: Sarah Vance
        c_id = UUID("22222222-2222-2222-2222-222222222222")
        self.profiles[c_id] = {
            "id": c_id,
            "role": UserRole.CAREGIVER,
            "name": "Sarah Vance (Primary Caregiver)",
            "phone": "+15550198235",
            "telegram_chat_id": "123456789",
            "expo_push_token": "ExponentPushToken[demo_caregiver_token]",
            "created_at": datetime.now(timezone.utc)
        }

        # Care link
        link_id = uuid4()
        self.care_links[link_id] = {
            "id": link_id,
            "patient_id": p_id,
            "caregiver_id": c_id,
            "status": CareLinkStatus.ACCEPTED,
            "priority_order": 1,
            "consented_at": datetime.now(timezone.utc),
            "created_at": datetime.now(timezone.utc)
        }

        # Simulator Source
        s_sim_id = UUID("33333333-3333-3333-3333-333333333333")
        self.sources[s_sim_id] = {
            "id": s_sim_id,
            "patient_id": p_id,
            "kind": SourceKind.SIMULATOR,
            "device_name": "CareCircle Realtime Simulator",
            "priority": 10,
            "last_seen_at": datetime.now(timezone.utc)
        }

        # BLE Watch Source
        s_ble_id = UUID("44444444-4444-4444-4444-444444444444")
        self.sources[s_ble_id] = {
            "id": s_ble_id,
            "patient_id": p_id,
            "kind": SourceKind.BLE,
            "device_name": "Galaxy Watch 6 BLE",
            "priority": 20,
            "last_seen_at": datetime.now(timezone.utc)
        }

        # Default thresholds for Patient
        defaults = [
            (VitalMetric.HEART_RATE, 50.0, 110.0, AlertSeverity.CRITICAL, 20, 120),
            (VitalMetric.SPO2, 92.0, None, AlertSeverity.CRITICAL, 15, 120),
            (VitalMetric.BODY_TEMPERATURE, 35.5, 38.3, AlertSeverity.WARNING, 30, 300),
            (VitalMetric.BLOOD_PRESSURE_SYSTOLIC, 90.0, 140.0, AlertSeverity.WARNING, 30, 300),
            (VitalMetric.BLOOD_PRESSURE_DIASTOLIC, 60.0, 90.0, AlertSeverity.WARNING, 30, 300),
            (VitalMetric.RESPIRATORY_RATE, 10.0, 26.0, AlertSeverity.WARNING, 30, 180),
        ]
        for metric, min_v, max_v, sev, sustained, max_age in defaults:
            self.threshold_rules[(p_id, metric)] = {
                "id": uuid4(),
                "patient_id": p_id,
                "metric": metric,
                "min_value": min_v,
                "max_value": max_v,
                "severity": sev,
                "sustained_seconds": sustained,
                "max_age_seconds": max_age,
                "enabled": True
            }

store = DataStore()

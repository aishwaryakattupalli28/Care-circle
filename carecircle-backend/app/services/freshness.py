from datetime import datetime, timezone
from typing import Tuple, Dict
from app.models.enums import VitalMetric, SourceKind, FreshnessState

# Default policies: (live_max_age_s, delayed_max_age_s)
# 0 for live_max_age_s means the source can NEVER be live
FRESHNESS_MATRIX: Dict[Tuple[VitalMetric, SourceKind], Tuple[int, int]] = {
    # Heart Rate
    (VitalMetric.HEART_RATE, SourceKind.BLE): (10, 900),
    (VitalMetric.HEART_RATE, SourceKind.SIMULATOR): (10, 900),
    (VitalMetric.HEART_RATE, SourceKind.HEALTH_CONNECT): (0, 1800),
    (VitalMetric.HEART_RATE, SourceKind.HEALTHKIT): (0, 1800),
    (VitalMetric.HEART_RATE, SourceKind.MANUAL): (0, 3600),

    # SpO2
    (VitalMetric.SPO2, SourceKind.BLE): (30, 1800),
    (VitalMetric.SPO2, SourceKind.SIMULATOR): (30, 1800),
    (VitalMetric.SPO2, SourceKind.HEALTH_CONNECT): (0, 3600),
    (VitalMetric.SPO2, SourceKind.HEALTHKIT): (0, 3600),
    (VitalMetric.SPO2, SourceKind.MANUAL): (0, 7200),

    # Body Temperature
    (VitalMetric.BODY_TEMPERATURE, SourceKind.BLE): (60, 1800),
    (VitalMetric.BODY_TEMPERATURE, SourceKind.SIMULATOR): (60, 1800),
    (VitalMetric.BODY_TEMPERATURE, SourceKind.HEALTH_CONNECT): (0, 3600),
    (VitalMetric.BODY_TEMPERATURE, SourceKind.HEALTHKIT): (0, 3600),
    (VitalMetric.BODY_TEMPERATURE, SourceKind.MANUAL): (0, 14400),

    # Blood Pressure (Systolic & Diastolic)
    (VitalMetric.BLOOD_PRESSURE_SYSTOLIC, SourceKind.BLE): (60, 3600),
    (VitalMetric.BLOOD_PRESSURE_SYSTOLIC, SourceKind.SIMULATOR): (60, 3600),
    (VitalMetric.BLOOD_PRESSURE_SYSTOLIC, SourceKind.MANUAL): (0, 21600),
    (VitalMetric.BLOOD_PRESSURE_DIASTOLIC, SourceKind.BLE): (60, 3600),
    (VitalMetric.BLOOD_PRESSURE_DIASTOLIC, SourceKind.SIMULATOR): (60, 3600),
    (VitalMetric.BLOOD_PRESSURE_DIASTOLIC, SourceKind.MANUAL): (0, 21600),

    # Respiratory Rate
    (VitalMetric.RESPIRATORY_RATE, SourceKind.BLE): (30, 1800),
    (VitalMetric.RESPIRATORY_RATE, SourceKind.SIMULATOR): (30, 1800),
    (VitalMetric.RESPIRATORY_RATE, SourceKind.MANUAL): (0, 7200),
}

def calculate_freshness(
    metric: VitalMetric,
    source_kind: SourceKind,
    measured_at: datetime,
    reference_time: datetime = None
) -> Tuple[FreshnessState, float]:
    """
    Core Principle: Compute freshness dynamically at read time, not storage time.
    Returns (freshness_state, age_seconds).
    """
    if reference_time is None:
        reference_time = datetime.now(timezone.utc)
        
    if measured_at.tzinfo is None:
        measured_at = measured_at.replace(tzinfo=timezone.utc)
    if reference_time.tzinfo is None:
        reference_time = reference_time.replace(tzinfo=timezone.utc)
        
    age_seconds = max(0.0, (reference_time - measured_at).total_seconds())
    
    # Policy limits
    policy = FRESHNESS_MATRIX.get((metric, source_kind), (0, 1800))
    live_max_age, delayed_max_age = policy
    
    # Manual readings are NEVER live
    if source_kind == SourceKind.MANUAL:
        if age_seconds <= delayed_max_age:
            return FreshnessState.DELAYED, age_seconds
        return FreshnessState.STALE, age_seconds
        
    # Health Connect and HealthKit default to delayed at best
    if source_kind in (SourceKind.HEALTH_CONNECT, SourceKind.HEALTHKIT):
        if age_seconds <= delayed_max_age:
            return FreshnessState.DELAYED, age_seconds
        return FreshnessState.STALE, age_seconds
        
    # BLE and Simulator can be LIVE if age is within live_max_age
    if live_max_age > 0 and age_seconds <= live_max_age:
        return FreshnessState.LIVE, age_seconds
    elif age_seconds <= delayed_max_age:
        return FreshnessState.DELAYED, age_seconds
    else:
        return FreshnessState.STALE, age_seconds

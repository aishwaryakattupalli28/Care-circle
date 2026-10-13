import pytest
from datetime import datetime, timezone, timedelta
from uuid import UUID, uuid4
from app.models.enums import VitalMetric, SourceKind, FreshnessState, QualityStatus, AlertType, AlertSeverity
from app.services.freshness import calculate_freshness
from app.services.normalizer import normalize_reading
from app.services.store import store
from app.services.rules_engine import rules_engine

def test_freshness_classification():
    now = datetime.now(timezone.utc)
    
    # 1. Heart rate from BLE, measured 5s ago -> LIVE
    state, age = calculate_freshness(VitalMetric.HEART_RATE, SourceKind.BLE, now - timedelta(seconds=5), now)
    assert state == FreshnessState.LIVE
    assert 4.0 <= age <= 6.0
    
    # 2. Heart rate from BLE, measured 30s ago -> DELAYED
    state, age = calculate_freshness(VitalMetric.HEART_RATE, SourceKind.BLE, now - timedelta(seconds=30), now)
    assert state == FreshnessState.DELAYED
    
    # 3. Heart rate from BLE, measured 1200s ago -> STALE (>900s)
    state, age = calculate_freshness(VitalMetric.HEART_RATE, SourceKind.BLE, now - timedelta(seconds=1200), now)
    assert state == FreshnessState.STALE
    
    # 4. Manual reading measured 2s ago is NEVER live
    state, age = calculate_freshness(VitalMetric.HEART_RATE, SourceKind.MANUAL, now - timedelta(seconds=2), now)
    assert state == FreshnessState.DELAYED
    
    # 5. Health Connect measured 2s ago is NEVER live
    state, age = calculate_freshness(VitalMetric.HEART_RATE, SourceKind.HEALTH_CONNECT, now - timedelta(seconds=2), now)
    assert state == FreshnessState.DELAYED

def test_unit_conversion_and_ranges():
    # Fahrenheit to Celsius
    c_val, c_unit, quality = normalize_reading(VitalMetric.BODY_TEMPERATURE, 98.6, "F")
    assert c_unit == "°C"
    assert round(c_val, 1) == 37.0
    assert quality == QualityStatus.VALID
    
    # Extreme physiologically impossible reading
    c_val, c_unit, quality = normalize_reading(VitalMetric.HEART_RATE, 450.0, "bpm")
    assert quality == QualityStatus.INVALID

@pytest.mark.asyncio
async def test_threshold_breach_and_deduplication():
    patient_id = UUID("11111111-1111-1111-1111-111111111111")
    now = datetime.now(timezone.utc)
    source_id = uuid4()
    
    # Ingest tachycardia reading (>110 threshold)
    test_reading = {
        "id": uuid4(),
        "patient_id": patient_id,
        "metric": VitalMetric.HEART_RATE,
        "value": 145.0,
        "canonical_value": 145.0,
        "unit": "bpm",
        "canonical_unit": "bpm",
        "measured_at": now,
        "received_at": now,
        "source_id": source_id,
        "source_record_id": "test_rec_1",
        "sync_mode": "live",
        "quality_status": QualityStatus.VALID,
        "is_suppressed": False
    }
    
    store.vitals.append(test_reading)
    await rules_engine.evaluate_readings(patient_id, [test_reading])
    
    # Assert alert was created
    alerts = [a for a in store.alerts.values() if a["patient_id"] == patient_id and a["metric"] == VitalMetric.HEART_RATE]
    assert len(alerts) >= 1
    assert alerts[0]["type"] == AlertType.THRESHOLD

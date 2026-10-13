from typing import Tuple
from app.models.enums import VitalMetric, QualityStatus

# Physiological plausible bounds for validity checking
# Values outside physiological extremes are INVALID (sensor glitch or artifact)
# Values slightly improbable but physically possible are flagged SUSPECT
PHYSIOLOGICAL_RANGES = {
    VitalMetric.HEART_RATE: {"min_valid": 30.0, "max_valid": 240.0, "min_suspect": 40.0, "max_suspect": 180.0, "unit": "bpm"},
    VitalMetric.SPO2: {"min_valid": 50.0, "max_valid": 100.0, "min_suspect": 70.0, "max_suspect": 100.0, "unit": "%"},
    VitalMetric.BODY_TEMPERATURE: {"min_valid": 30.0, "max_valid": 44.0, "min_suspect": 35.0, "max_suspect": 41.5, "unit": "C"},
    VitalMetric.BLOOD_PRESSURE_SYSTOLIC: {"min_valid": 50.0, "max_valid": 260.0, "min_suspect": 80.0, "max_suspect": 200.0, "unit": "mmHg"},
    VitalMetric.BLOOD_PRESSURE_DIASTOLIC: {"min_valid": 30.0, "max_valid": 150.0, "min_suspect": 50.0, "max_suspect": 120.0, "unit": "mmHg"},
    VitalMetric.RESPIRATORY_RATE: {"min_valid": 4.0, "max_valid": 60.0, "min_suspect": 8.0, "max_suspect": 40.0, "unit": "bpm"},
}

def normalize_reading(metric: VitalMetric, value: float, unit: str) -> Tuple[float, str, QualityStatus]:
    """
    Normalizes vital reading to canonical unit and determines quality status.
    Returns (canonical_value, canonical_unit, quality_status).
    """
    u = unit.strip().lower()
    canonical_val = float(value)
    canonical_unit = PHYSIOLOGICAL_RANGES[metric]["unit"]
    
    # 1. Unit conversions
    if metric == VitalMetric.BODY_TEMPERATURE:
        if u in ("f", "fahrenheit", "°f"):
            canonical_val = (canonical_val - 32.0) * 5.0 / 9.0
        canonical_unit = "°C"
    elif metric in (VitalMetric.BLOOD_PRESSURE_SYSTOLIC, VitalMetric.BLOOD_PRESSURE_DIASTOLIC):
        if u in ("kpa", "kilopascal"):
            canonical_val = canonical_val * 7.50062
        canonical_unit = "mmHg"
    elif metric == VitalMetric.HEART_RATE:
        canonical_unit = "bpm"
    elif metric == VitalMetric.SPO2:
        canonical_unit = "%"
    elif metric == VitalMetric.RESPIRATORY_RATE:
        canonical_unit = "rpm"

    canonical_val = round(canonical_val, 2)
    
    # 2. Physiological validity validation
    bounds = PHYSIOLOGICAL_RANGES[metric]
    if canonical_val < bounds["min_valid"] or canonical_val > bounds["max_valid"]:
        return canonical_val, canonical_unit, QualityStatus.INVALID
        
    if canonical_val < bounds["min_suspect"] or canonical_val > bounds["max_suspect"]:
        return canonical_val, canonical_unit, QualityStatus.SUSPECT
        
    return canonical_val, canonical_unit, QualityStatus.VALID

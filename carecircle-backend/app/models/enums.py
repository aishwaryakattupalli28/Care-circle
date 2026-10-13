from enum import Enum

class UserRole(str, Enum):
    PATIENT = "patient"
    CAREGIVER = "caregiver"

class CareLinkStatus(str, Enum):
    PENDING = "pending"
    ACCEPTED = "accepted"
    REVOKED = "revoked"

class SourceKind(str, Enum):
    SIMULATOR = "simulator"
    BLE = "ble"
    HEALTH_CONNECT = "health_connect"
    HEALTHKIT = "healthkit"
    MANUAL = "manual"

class VitalMetric(str, Enum):
    HEART_RATE = "heart_rate"
    SPO2 = "spo2"
    BODY_TEMPERATURE = "body_temperature"
    BLOOD_PRESSURE_SYSTOLIC = "blood_pressure_systolic"
    BLOOD_PRESSURE_DIASTOLIC = "blood_pressure_diastolic"
    RESPIRATORY_RATE = "respiratory_rate"

class SyncMode(str, Enum):
    LIVE = "live"
    PERIODIC = "periodic"
    MANUAL = "manual"

class QualityStatus(str, Enum):
    VALID = "valid"
    SUSPECT = "suspect"
    INVALID = "invalid"

class FreshnessState(str, Enum):
    LIVE = "live"
    DELAYED = "delayed"
    STALE = "stale"

class AlertType(str, Enum):
    THRESHOLD = "threshold"
    STALE_DATA = "stale_data"
    DEVICE_DISCONNECTED = "device_disconnected"
    MANUAL_SOS = "manual_sos"

class AlertSeverity(str, Enum):
    INFO = "info"
    WARNING = "warning"
    CRITICAL = "critical"

class AlertStatus(str, Enum):
    OPEN = "open"
    ACKNOWLEDGED = "acknowledged"
    ESCALATED = "escalated"
    RESOLVED = "resolved"

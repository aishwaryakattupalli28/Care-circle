from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from uuid import UUID
from app.models.enums import AlertType, VitalMetric, AlertSeverity, AlertStatus

class AlertResponse(BaseModel):
    id: UUID
    patient_id: UUID
    type: AlertType
    metric: Optional[VitalMetric] = None
    severity: AlertSeverity
    status: AlertStatus
    trigger_message: str
    opened_at: datetime
    acknowledged_by: Optional[UUID] = None
    acknowledged_at: Optional[datetime] = None
    resolved_at: Optional[datetime] = None
    escalation_level: int = 0
    last_escalated_at: datetime

class AlertAcknowledgeRequest(BaseModel):
    note: Optional[str] = None

class AlertResolveRequest(BaseModel):
    reason: Optional[str] = "Resolved by user"

class ManualSOSRequest(BaseModel):
    patient_id: UUID
    note: Optional[str] = "Patient pressed manual SOS button"

class ThresholdRuleItem(BaseModel):
    metric: VitalMetric
    min_value: Optional[float] = None
    max_value: Optional[float] = None
    severity: AlertSeverity = AlertSeverity.WARNING
    sustained_seconds: int = 30
    max_age_seconds: int = 120
    enabled: bool = True

class ThresholdRuleList(BaseModel):
    rules: List[ThresholdRuleItem]

from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from uuid import UUID
from app.models.enums import VitalMetric, SyncMode, QualityStatus, FreshnessState, SourceKind

class VitalReadingIngest(BaseModel):
    metric: VitalMetric
    value: float
    unit: str
    measured_at: datetime
    source_id: UUID
    source_record_id: str
    sync_mode: SyncMode = SyncMode.LIVE
    patient_id: Optional[UUID] = None

class VitalBatchIngestRequest(BaseModel):
    readings: List[VitalReadingIngest]

class VitalReadingResponse(BaseModel):
    id: UUID
    patient_id: UUID
    metric: VitalMetric
    value: float
    canonical_value: float
    unit: str
    canonical_unit: str
    measured_at: datetime
    received_at: datetime
    source_id: UUID
    source_kind: SourceKind
    sync_mode: SyncMode
    quality_status: QualityStatus
    freshness: FreshnessState
    age_seconds: float
    is_suppressed: bool = False

class LatestVitalsResponse(BaseModel):
    patient_id: UUID
    computed_at: datetime
    vitals: List[VitalReadingResponse]

class VitalHistoryBucket(BaseModel):
    bucket_start: datetime
    bucket_end: datetime
    avg_value: float
    min_value: float
    max_value: float
    count: int
    unit: str

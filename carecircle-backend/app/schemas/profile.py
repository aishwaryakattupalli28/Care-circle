from pydantic import BaseModel
from typing import Optional
from uuid import UUID
from app.models.enums import UserRole, CareLinkStatus, SourceKind
from datetime import datetime

class ProfileResponse(BaseModel):
    id: UUID
    role: UserRole
    name: str
    phone: Optional[str] = None
    telegram_chat_id: Optional[str] = None
    expo_push_token: Optional[str] = None

class ProfileUpdateRequest(BaseModel):
    name: Optional[str] = None
    phone: Optional[str] = None
    telegram_chat_id: Optional[str] = None
    expo_push_token: Optional[str] = None

class PushTokenRequest(BaseModel):
    expo_push_token: str

class CareLinkInviteRequest(BaseModel):
    patient_id: UUID
    caregiver_email_or_id: str
    priority_order: int = 1

class CareLinkResponse(BaseModel):
    id: UUID
    patient_id: UUID
    caregiver_id: UUID
    status: CareLinkStatus
    priority_order: int
    consented_at: Optional[datetime] = None

class SourceCreateRequest(BaseModel):
    patient_id: UUID
    kind: SourceKind
    device_name: str
    priority: int = 10

class SourceResponse(BaseModel):
    id: UUID
    patient_id: UUID
    kind: SourceKind
    device_name: str
    priority: int
    last_seen_at: datetime

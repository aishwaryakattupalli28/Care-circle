from fastapi import Depends, HTTPException, status, Header
from typing import Optional
from uuid import UUID
from app.services.store import store
from app.models.enums import UserRole

async def get_current_user_id(authorization: Optional[str] = Header(None)) -> UUID:
    """
    Mock / JWT User identification for Supabase Auth.
    If header 'Authorization: Bearer <user_id_or_token>' is passed, extracts UUID or defaults
    to the pre-seeded demo caregiver / patient.
    """
    if not authorization:
        # Default to demo caregiver for seamless browser / expo dev testing
        return UUID("22222222-2222-2222-2222-222222222222")
    
    token = authorization.replace("Bearer ", "").strip()
    try:
        return UUID(token)
    except Exception:
        # Token format fallback: if token contains 'patient', return patient_id
        if "patient" in token.lower():
            return UUID("11111111-1111-1111-1111-111111111111")
        return UUID("22222222-2222-2222-2222-222222222222")

async def verify_caregiver_access(patient_id: UUID, current_user_id: UUID = Depends(get_current_user_id)):
    """
    Enforces Row Level Security / Consent:
    Allows access if current user is the patient themselves,
    OR if there is an accepted care_link with caregiver.
    """
    if current_user_id == patient_id:
        return True
        
    async with store.lock:
        is_consented = any(
            l["patient_id"] == patient_id 
            and l["caregiver_id"] == current_user_id 
            and l["status"] == "accepted"
            for l in store.care_links.values()
        )
        if not is_consented:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Caregiver consent required or link revoked. Access denied."
            )
            
        # Log audit access
        store.audit_logs.append({
            "caregiver_id": current_user_id,
            "patient_id": patient_id,
            "endpoint": "patient_telemetry_access"
        })
    return True

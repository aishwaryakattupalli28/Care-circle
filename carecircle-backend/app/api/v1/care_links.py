from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID, uuid4
from datetime import datetime, timezone
from app.api.deps import get_current_user_id
from app.services.store import store
from app.schemas.profile import CareLinkInviteRequest, CareLinkResponse
from app.models.enums import CareLinkStatus

router = APIRouter(prefix="/care-links", tags=["Care Links"])

@router.post("/invite", response_model=CareLinkResponse)
async def invite_caregiver(body: CareLinkInviteRequest, user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        link_id = uuid4()
        link_obj = {
            "id": link_id,
            "patient_id": body.patient_id,
            "caregiver_id": UUID("22222222-2222-2222-2222-222222222222"),
            "status": CareLinkStatus.PENDING,
            "priority_order": body.priority_order,
            "consented_at": None,
            "created_at": datetime.now(timezone.utc)
        }
        store.care_links[link_id] = link_obj
        return link_obj

@router.post("/{link_id}/accept", response_model=CareLinkResponse)
async def accept_care_link(link_id: UUID, user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        link = store.care_links.get(link_id)
        if not link:
            raise HTTPException(status_code=404, detail="Care link not found")
        link["status"] = CareLinkStatus.ACCEPTED
        link["consented_at"] = datetime.now(timezone.utc)
        return link

@router.delete("/{link_id}")
async def revoke_care_link(link_id: UUID, user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        link = store.care_links.get(link_id)
        if not link:
            raise HTTPException(status_code=404, detail="Care link not found")
        link["status"] = CareLinkStatus.REVOKED
        return {"status": "revoked", "id": str(link_id)}

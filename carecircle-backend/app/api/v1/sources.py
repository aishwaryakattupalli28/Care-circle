from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID, uuid4
from datetime import datetime, timezone
from typing import List
from app.api.deps import get_current_user_id
from app.services.store import store
from app.schemas.profile import SourceCreateRequest, SourceResponse

router = APIRouter(prefix="/sources", tags=["Sources & Devices"])

@router.get("", response_model=List[SourceResponse])
async def list_sources(user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        return list(store.sources.values())

@router.post("", response_model=SourceResponse)
async def create_source(body: SourceCreateRequest, user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        s_id = uuid4()
        source_obj = {
            "id": s_id,
            "patient_id": body.patient_id,
            "kind": body.kind,
            "device_name": body.device_name,
            "priority": body.priority,
            "last_seen_at": datetime.now(timezone.utc)
        }
        store.sources[s_id] = source_obj
        return source_obj

@router.patch("/{source_id}", response_model=SourceResponse)
async def update_source(source_id: UUID, priority: int = None, user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        src = store.sources.get(source_id)
        if not src:
            raise HTTPException(status_code=404, detail="Source not found")
        if priority is not None:
            src["priority"] = priority
        src["last_seen_at"] = datetime.now(timezone.utc)
        return src

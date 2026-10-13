from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID
from app.api.deps import get_current_user_id
from app.services.store import store
from app.schemas.profile import ProfileResponse, ProfileUpdateRequest, PushTokenRequest

router = APIRouter(tags=["Auth & Profiles"])

@router.get("/me", response_model=ProfileResponse)
async def get_my_profile(user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        profile = store.profiles.get(user_id)
        if not profile:
            raise HTTPException(status_code=404, detail="User profile not found")
        return profile

@router.patch("/me", response_model=ProfileResponse)
async def update_my_profile(body: ProfileUpdateRequest, user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        profile = store.profiles.get(user_id)
        if not profile:
            raise HTTPException(status_code=404, detail="User profile not found")
        if body.name is not None:
            profile["name"] = body.name
        if body.phone is not None:
            profile["phone"] = body.phone
        if body.telegram_chat_id is not None:
            profile["telegram_chat_id"] = body.telegram_chat_id
        if body.expo_push_token is not None:
            profile["expo_push_token"] = body.expo_push_token
        return profile

@router.post("/me/push-token")
async def register_push_token(body: PushTokenRequest, user_id: UUID = Depends(get_current_user_id)):
    async with store.lock:
        profile = store.profiles.get(user_id)
        if not profile:
            raise HTTPException(status_code=404, detail="User profile not found")
        profile["expo_push_token"] = body.expo_push_token
        return {"status": "ok", "expo_push_token": body.expo_push_token}

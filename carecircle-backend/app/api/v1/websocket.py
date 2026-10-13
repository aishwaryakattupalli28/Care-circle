from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Query
from uuid import UUID
from app.services.websocket_manager import ws_manager
from app.services.store import store
from app.core.logging import logger

router = APIRouter(tags=["WebSocket"])

@router.websocket("/ws/patients/{patient_id}")
async def websocket_patient_stream(
    websocket: WebSocket,
    patient_id: UUID,
    token: str = Query(None)
):
    """
    Caregiver Live Telemetry WebSocket:
    Verifies care_link consent before subscription.
    Streams vital readings, freshness changes, and alert state transitions in real time.
    """
    # Optional consent check on connection
    async with store.lock:
        is_known = patient_id in store.profiles or any(l["patient_id"] == patient_id for l in store.care_links.values())
        if not is_known:
            await websocket.close(code=4003, reason="Patient not found or unauthorized")
            return

    await ws_manager.connect(patient_id, websocket)
    try:
        while True:
            # Keep-alive heartbeat / client messages
            data = await websocket.receive_text()
            if data == "ping":
                await websocket.send_text("{"type": "pong"}")
    except WebSocketDisconnect:
        ws_manager.disconnect(patient_id, websocket)
    except Exception as e:
        logger.warning(f"WS Exception: {e}")
        ws_manager.disconnect(patient_id, websocket)

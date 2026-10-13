from typing import Dict, Set, Any
from uuid import UUID
from fastapi import WebSocket
from app.core.logging import logger
import json

class WebSocketManager:
    def __init__(self):
        # patient_id -> set of active WebSockets
        self.active_connections: Dict[UUID, Set[WebSocket]] = {}

    async def connect(self, patient_id: UUID, websocket: WebSocket):
        await websocket.accept()
        if patient_id not in self.active_connections:
            self.active_connections[patient_id] = set()
        self.active_connections[patient_id].add(websocket)
        logger.info(f"WebSocket client connected for patient {patient_id}")

    def disconnect(self, patient_id: UUID, websocket: WebSocket):
        if patient_id in self.active_connections:
            self.active_connections[patient_id].discard(websocket)
            if not self.active_connections[patient_id]:
                del self.active_connections[patient_id]
        logger.info(f"WebSocket client disconnected for patient {patient_id}")

    async def broadcast_patient_event(self, patient_id: UUID, event_type: str, payload: Any):
        if patient_id in self.active_connections:
            message = {
                "type": event_type,
                "patient_id": str(patient_id),
                "data": payload
            }
            dead_sockets = set()
            for ws in self.active_connections[patient_id]:
                try:
                    await ws.send_text(json.dumps(message, default=str))
                except Exception as e:
                    logger.warning(f"Failed to send WS message: {e}")
                    dead_sockets.add(ws)
            for ws in dead_sockets:
                self.disconnect(patient_id, ws)

ws_manager = WebSocketManager()

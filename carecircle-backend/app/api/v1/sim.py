from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from uuid import UUID
from app.services.simulator import simulator_service

router = APIRouter(prefix="/sim", tags=["Demo Simulator"])

class SimStartRequest(BaseModel):
    patient_id: UUID
    scenario: str = "normal" # normal, tachycardia, low_spo2, fever, device_dropout, bp_manual

@router.post("/start")
async def start_simulation(body: SimStartRequest):
    allowed = ("normal", "tachycardia", "low_spo2", "fever", "device_dropout", "bp_manual")
    if body.scenario not in allowed:
        raise HTTPException(status_code=400, detail=f"Invalid scenario. Allowed: {allowed}")
    simulator_service.start_simulation(body.patient_id, body.scenario)
    return {"status": "started", "patient_id": str(body.patient_id), "scenario": body.scenario}

@router.post("/stop")
async def stop_simulation(patient_id: UUID):
    simulator_service.stop_simulation(patient_id)
    return {"status": "stopped", "patient_id": str(patient_id)}

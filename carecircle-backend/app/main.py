import asyncio
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.core.config import settings
from app.core.logging import logger
from app.workers.stale_monitor import run_background_stale_monitor
from app.api.v1.auth import router as auth_router
from app.api.v1.care_links import router as care_links_router
from app.api.v1.sources import router as sources_router
from app.api.v1.vitals import router as vitals_router
from app.api.v1.alerts import router as alerts_router
from app.api.v1.sim import router as sim_router
from app.api.v1.websocket import router as ws_router

background_task = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global background_task
    logger.info("Starting CareCircle Backend service...")
    background_task = asyncio.create_task(run_background_stale_monitor())
    yield
    if background_task:
        background_task.cancel()
    logger.info("Shutting down CareCircle Backend service...")

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="Remote patient vital-sign monitoring backend with live freshness & escalation.",
    lifespan=lifespan
)

# CORS setup for Expo / Web
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Root Health Check
@app.get("/health", tags=["Monitoring"])
@app.get("/v1/health", tags=["Monitoring"])
async def health_check():
    return {
        "status": "healthy",
        "service": "carecircle-backend",
        "environment": settings.ENVIRONMENT,
        "version": settings.VERSION
    }

# Include API v1 Routers
app.include_router(auth_router, prefix="/v1")
app.include_router(care_links_router, prefix="/v1")
app.include_router(sources_router, prefix="/v1")
app.include_router(vitals_router, prefix="/v1")
app.include_router(alerts_router, prefix="/v1")
app.include_router(sim_router, prefix="/v1")
app.include_router(ws_router, prefix="/v1")

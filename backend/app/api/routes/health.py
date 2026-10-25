"""Liveness / readiness. Android's Home screen pings /health on launch."""

from __future__ import annotations

import time

from fastapi import APIRouter

from app.config import settings
from app.models.schemas import HealthResponse
from app.services.container import container

router = APIRouter(tags=["system"])
_started = time.time()


@router.get("/health", response_model=HealthResponse, summary="Liveness probe")
async def health() -> HealthResponse:
    ok, detail = await container.repo.health()
    return HealthResponse(
        status="ok" if ok else "degraded",
        service=settings.app_name,
        version=settings.app_version,
        env=settings.env,
        git_sha=settings.git_sha,
        uptime_s=round(time.time() - _started, 1),
        checks={"repository": detail},
    )


@router.get("/ready", summary="Readiness probe (503 until the index exists)")
async def ready() -> dict[str, object]:
    return {
        "ready": container.ready,
        "documents": len(container.engine.entries) if container.ready else 0,
    }

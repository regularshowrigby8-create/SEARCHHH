"""Operational endpoints (protected by API key / JWT when API_KEY is set)."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, BackgroundTasks, Depends

from app.api.deps import ContainerDep
from app.core.logging import get_logger
from app.core.security import require_api_key

log = get_logger(__name__)
router = APIRouter(prefix="/admin", tags=["admin"], dependencies=[Depends(require_api_key)])


@router.post("/refresh", summary="Rebuild the search index from the backing store")
async def refresh(container: ContainerDep) -> dict[str, object]:
    count = await container.refresh()
    return {"refreshed": True, "documents": count}


@router.post("/warm", summary="Warm caches in the background")
async def warm(container: ContainerDep, tasks: BackgroundTasks) -> dict[str, object]:
    async def _warm() -> None:
        from app.services.ai import generate_insight

        for doc in container.engine.docs[:5]:
            await generate_insight(doc.entry, "demo warmup")
        log.info("cache_warmed")

    tasks.add_task(_warm)
    return {"scheduled": True}


@router.get("/stats", summary="Index statistics")
async def stats(container: ContainerDep) -> dict[str, object]:
    engine = container.engine
    return {
        "documents": len(engine.docs),
        "vocabulary": len(engine.df),
        "avg_doc_len": round(engine.avg_len, 2),
        "categories": engine.categories(),
        "repository": container.repo.name,
    }


@router.get("/ping", summary="Auth smoke test")
async def ping(_: Annotated[None, Depends(require_api_key)]) -> dict[str, bool]:
    return {"ok": True}

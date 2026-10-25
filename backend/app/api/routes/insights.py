"""AI 'insight' card shown on the Android detail screen."""

from __future__ import annotations

from fastapi import APIRouter

from app.api.deps import EngineDep
from app.core.errors import NotFound
from app.models.schemas import Entry, InsightRequest, InsightResponse
from app.services.ai import generate_insight

router = APIRouter(prefix="/insights", tags=["insights"])


@router.post("", response_model=InsightResponse, summary="Explain an entry for a given audience")
async def insight(engine: EngineDep, payload: InsightRequest) -> InsightResponse:
    entry: Entry | None = next((doc.entry for doc in engine.docs if doc.entry.id == payload.entry_id), None)
    if entry is None:
        raise NotFound(f"No entry with id '{payload.entry_id}'")
    return await generate_insight(entry, payload.audience)

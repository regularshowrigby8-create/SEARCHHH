"""Feedback endpoint: Supabase table when configured, memory otherwise."""

from __future__ import annotations

from fastapi import APIRouter

from app.api.deps import ContainerDep
from app.models.schemas import FeedbackRequest, FeedbackResponse

router = APIRouter(prefix="/feedback", tags=["feedback"])


@router.post("", response_model=FeedbackResponse, status_code=201, summary="Submit feedback")
async def submit_feedback(container: ContainerDep, payload: FeedbackRequest) -> FeedbackResponse:
    record_id, store = await container.repo.store_feedback(payload)
    return FeedbackResponse(accepted=True, id=record_id, stored_in=store)  # type: ignore[arg-type]

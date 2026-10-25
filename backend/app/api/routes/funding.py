"""Funding eligibility matching (NYDA tiers, SEFA, YES)."""

from __future__ import annotations

from fastapi import APIRouter

from app.models.schemas import FundingMatchRequest, FundingMatchResponse
from app.services import funding

router = APIRouter(prefix="/funding", tags=["funding"])


@router.post("/match", response_model=FundingMatchResponse, summary="Which funding can I get today?")
async def match_funding(payload: FundingMatchRequest) -> FundingMatchResponse:
    return funding.match(payload)


@router.get("/rules", summary="The bundled funding rules and their sources")
async def rules() -> dict[str, object]:
    return {
        "reviewed": funding.RULES_REVIEWED,
        "count": len(funding.RULES),
        "rules": [
            {
                "id": rule.id,
                "name": rule.name,
                "provider": rule.provider,
                "min_amount": rule.min_amount,
                "max_amount": rule.max_amount,
                "requires_registration": rule.requires_registration,
                "requires_training": rule.requires_training,
                "source": rule.source,
                "url": rule.url,
            }
            for rule in funding.RULES
        ],
    }

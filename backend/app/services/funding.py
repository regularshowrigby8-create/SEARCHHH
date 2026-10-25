"""Funding eligibility matcher.

Encodes the published NYDA Grant Programme tiers and the headline SEFA/YES
rules as *rules*, not prose, so the app can answer the question graduates
actually ask: **"what can I get, today, with what I have?"**

Every rule carries its source so the UI can show the official page and the
listing can be verified. Figures are current as at 2026-10; see
`docs/GRADLINK.md` for citations and `RULES_REVIEWED` for the review date.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Literal

from app.models.schemas import FundingMatch, FundingMatchRequest, FundingMatchResponse

RULES_REVIEWED = "2026-10-22"

Stage = Literal["idea", "survivalist", "startup", "growth", "expansion"]


@dataclass(frozen=True, slots=True)
class FundingRule:
    id: str
    name: str
    provider: str
    min_amount: float
    max_amount: float
    stages: tuple[Stage, ...]
    min_age: int = 18
    max_age: int = 35
    requires_registration: bool = False
    requires_training: bool = False
    max_turnover: float | None = None
    url: str = ""
    source: str = ""
    notes: str = ""

    def evaluate(self, req: FundingMatchRequest) -> tuple[list[str], list[str]]:
        """Return (hard blockers, steps the applicant can complete themselves).

        The distinction matters to the user: "you are 41" is a no, while "finish
        the free training" is a to-do with a known path.
        """
        blockers: list[str] = []
        steps: list[str] = []

        if not (self.min_age <= req.age <= self.max_age):
            blockers.append(f"Age {req.age} is outside {self.min_age}-{self.max_age}")
        if req.stage not in self.stages:
            blockers.append(f"Stage '{req.stage}' is not funded at this tier")
        if self.max_turnover is not None and req.annual_turnover > self.max_turnover:
            blockers.append(f"Turnover above R{self.max_turnover:,.0f} cap")
        if req.prior_funding > self.max_amount:
            blockers.append("Prior development-finance funding already exceeds this tier")

        if self.requires_registration and not req.registered:
            steps.append("Register the business with CIPC")
        if self.requires_training and not req.training_completed:
            steps.append("Book the free NYDA Business Management Training")
        return blockers, steps


# --- NYDA Grant Programme (four published thresholds) ---------------------
NYDA_TIERS: tuple[FundingRule, ...] = (
    FundingRule(
        id="nyda-threshold-1",
        name="NYDA Grant — Idea generation & survivalist",
        provider="NYDA",
        min_amount=1_000,
        max_amount=10_000,
        stages=("idea", "survivalist"),
        url="https://www.nyda.gov.za",
        source="NYDA Grant Programme thresholds document",
        notes="For pre-revenue or survivalist ventures. Business Management Training is compulsory.",
        requires_training=True,
    ),
    FundingRule(
        id="nyda-threshold-2",
        name="NYDA Grant — Start-up",
        provider="NYDA",
        min_amount=10_001,
        max_amount=50_000,
        stages=("startup", "survivalist"),
        requires_registration=True,
        requires_training=True,
        max_turnover=750_000,
        url="https://www.nyda.gov.za",
        source="NYDA Grant Programme thresholds document",
        notes="Trading start-ups (Pty or co-operative) with a viable plan.",
    ),
    FundingRule(
        id="nyda-threshold-3",
        name="NYDA Grant — Growth",
        provider="NYDA",
        min_amount=50_001,
        max_amount=100_000,
        stages=("growth",),
        requires_registration=True,
        requires_training=True,
        max_turnover=750_000,
        url="https://www.nyda.gov.za",
        source="NYDA Grant Programme thresholds document",
    ),
    FundingRule(
        id="nyda-threshold-4",
        name="NYDA Grant — Expansion",
        provider="NYDA",
        min_amount=100_001,
        max_amount=200_000,
        stages=("expansion", "growth"),
        requires_registration=True,
        requires_training=True,
        max_turnover=750_000,
        url="https://www.nyda.gov.za",
        source="NYDA Grant Programme thresholds document",
        notes="Co-operatives up to R250,000; agriculture and technology projects up to R300,000.",
    ),
)

SEFA_RULE = FundingRule(
    id="sefa-loan",
    name="SEFA Small Business Loan",
    provider="SEFA",
    min_amount=500,
    max_amount=15_000_000,
    stages=("startup", "growth", "expansion"),
    min_age=18,
    max_age=99,
    requires_registration=True,
    url="https://www.sefa.org.za",
    source="SEFA product page",
    notes="Repayable. For businesses that cannot access commercial credit.",
)

YES_RULE = FundingRule(
    id="yes-placement",
    name="YES Programme paid placement",
    provider="YES / SA Youth",
    min_amount=5_241,  # monthly stipend, not a grant
    max_amount=5_241,
    stages=("idea", "survivalist", "startup", "growth", "expansion"),
    min_age=18,
    max_age=34,
    url="https://sayouth.mobi",
    source="YES programme guidance (stipend from March 2026)",
    notes="12-month paid work experience, ~R5,241 per month. Income, not capital — "
    "but it is the fastest route to a fundable track record.",
)

RULES: tuple[FundingRule, ...] = (*NYDA_TIERS, SEFA_RULE, YES_RULE)


def match(req: FundingMatchRequest) -> FundingMatchResponse:
    """Return every rule with its verdict, best matches first."""
    matches: list[FundingMatch] = []
    for rule in RULES:
        blockers, steps = rule.evaluate(req)
        status = "ineligible" if blockers else ("eligible" if not steps else "steps_required")
        matches.append(
            FundingMatch(
                id=rule.id,
                name=rule.name,
                provider=rule.provider,
                min_amount=rule.min_amount,
                max_amount=rule.max_amount,
                eligible=status == "eligible",
                status=status,  # type: ignore[arg-type]
                blockers=blockers,
                # A hard blocker makes the checklist moot; showing steps next to
                # "not eligible" would imply a path that does not exist.
                steps=[] if blockers else steps,
                url=rule.url,
                source=rule.source,
                notes=rule.notes,
            )
        )

    order = {"eligible": 0, "steps_required": 1, "ineligible": 2}
    matches.sort(key=lambda m: (order[m.status], m.min_amount))
    eligible = [m for m in matches if m.eligible]
    next_actions: list[str] = []
    if not req.registered:
        next_actions.append("Register your business with CIPC (R175, online)")
    if not req.training_completed:
        next_actions.append("Book the free NYDA Business Management Training (prerequisite for every NYDA grant)")
    next_actions.append("Open a business bank account with confirmation letter")
    if req.annual_turnover == 0 and req.stage in {"idea", "survivalist"}:
        next_actions.append("Start with the NYDA Threshold 1 grant (R1,000-R10,000) to test the idea")

    return FundingMatchResponse(
        eligible_count=len(eligible),
        matches=matches,
        next_actions=next_actions,
        rules_reviewed=RULES_REVIEWED,
    )

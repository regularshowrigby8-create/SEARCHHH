"""Pydantic v2 schemas - the single source of truth for the API contract.

The Android client mirrors these in `data/remote/dto/*.kt`.
"""

from __future__ import annotations

from datetime import datetime
from typing import Annotated, Literal

from pydantic import BaseModel, ConfigDict, Field, StringConstraints

Identifier = Annotated[str, StringConstraints(min_length=1, max_length=64, strip_whitespace=True)]
Query = Annotated[str, StringConstraints(max_length=160, strip_whitespace=True)]


class HealthResponse(BaseModel):
    status: Literal["ok", "degraded"] = "ok"
    service: str
    version: str
    env: str
    git_sha: str
    uptime_s: float
    checks: dict[str, str] = Field(default_factory=dict)


class Entry(BaseModel):
    """A searchable document. Domain-agnostic on purpose."""

    model_config = ConfigDict(from_attributes=True)

    id: Identifier
    title: str
    subtitle: str = ""
    description: str = ""
    category: str = "general"
    tags: list[str] = Field(default_factory=list)
    price: float | None = None
    currency: str = "ZAR"
    rating: float = 0.0
    image_url: str | None = None
    location: str | None = None
    url: str | None = None
    updated_at: datetime | None = None


class ScoredEntry(Entry):
    score: float = 0.0
    highlights: list[str] = Field(default_factory=list)


class SearchRequest(BaseModel):
    q: Query = ""
    category: str | None = None
    limit: Annotated[int, Field(ge=1, le=50)] = 20
    offset: Annotated[int, Field(ge=0, le=5000)] = 0
    sort: Literal["relevance", "rating", "price_asc", "price_desc", "recent"] = "relevance"


class SearchResponse(BaseModel):
    query: str
    total: int
    took_ms: float
    source: Literal["supabase", "memory"] = "memory"
    results: list[ScoredEntry] = Field(default_factory=list)
    suggestions: list[str] = Field(default_factory=list)


class InsightRequest(BaseModel):
    entry_id: Identifier
    audience: str = "first-time buyer"


class InsightResponse(BaseModel):
    entry_id: str
    insight: str
    provider: Literal["gemini", "openai", "heuristic"] = "heuristic"
    cached: bool = False


class FeedbackRequest(BaseModel):
    message: Annotated[str, StringConstraints(min_length=2, max_length=2000)]
    rating: Annotated[int, Field(ge=1, le=5)] = 5
    entry_id: Identifier | None = None
    contact: str | None = None
    platform: Literal["android", "web", "cli", "unknown"] = "android"
    app_version: str | None = None


class FeedbackResponse(BaseModel):
    accepted: bool
    id: str
    stored_in: Literal["supabase", "memory"]


class TokenRequest(BaseModel):
    subject: Annotated[str, StringConstraints(min_length=2, max_length=64)] = "demo-user"
    scopes: list[str] = Field(default_factory=lambda: ["read"])


class TokenResponse(BaseModel):
    access_token: str
    token_type: Literal["bearer"] = "bearer"
    expires_in: int
    scopes: list[str]


class FundingMatchRequest(BaseModel):
    """Answers the question a graduate actually asks before applying."""

    age: Annotated[int, Field(ge=15, le=80)] = 24
    stage: Literal["idea", "survivalist", "startup", "growth", "expansion"] = "idea"
    registered: bool = False
    training_completed: bool = False
    annual_turnover: Annotated[float, Field(ge=0, le=500_000_000)] = 0.0
    prior_funding: Annotated[float, Field(ge=0, le=500_000_000)] = 0.0
    province: str | None = None
    sector: str | None = None


class FundingMatch(BaseModel):
    id: str
    name: str
    provider: str
    min_amount: float
    max_amount: float
    eligible: bool
    status: Literal["eligible", "steps_required", "ineligible"] = "eligible"
    blockers: list[str] = Field(default_factory=list)
    steps: list[str] = Field(default_factory=list)
    url: str = ""
    source: str = ""
    notes: str = ""


class FundingMatchResponse(BaseModel):
    eligible_count: int
    matches: list[FundingMatch]
    next_actions: list[str]
    rules_reviewed: str


class IntegrationStatus(BaseModel):
    name: str
    configured: bool
    mode: str
    notes: str = ""


class IntegrationsResponse(BaseModel):
    count: int
    configured: int
    integrations: list[IntegrationStatus]

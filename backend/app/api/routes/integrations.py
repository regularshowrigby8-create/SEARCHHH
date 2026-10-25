"""Self-describing integration registry.

This is the endpoint the README screenshot points at: it answers "what is wired
up and what is running in fallback mode?" without reading any code.
"""

from __future__ import annotations

from fastapi import APIRouter

from app.config import settings
from app.models.schemas import IntegrationsResponse, IntegrationStatus

router = APIRouter(prefix="/integrations", tags=["system"])


def _items() -> list[IntegrationStatus]:
    s = settings
    return [
        IntegrationStatus(
            name="supabase",
            configured=s.supabase_configured,
            mode="postgrest" if s.supabase_configured else "in-memory fallback",
            notes="auth + table storage",
        ),
        IntegrationStatus(
            name="postgres",
            configured=bool(s.database_url),
            mode="asyncpg/sqlalchemy" if s.database_url else "not configured",
            notes="optional relational store",
        ),
        IntegrationStatus(
            name="redis", configured=bool(s.redis_url), mode="redis" if s.redis_url else "memory:// rate limiting"
        ),
        IntegrationStatus(
            name="sentry",
            configured=bool(s.sentry_dsn),
            mode="crash + performance" if s.sentry_dsn else "disabled",
            notes="set SENTRY_DSN",
        ),
        IntegrationStatus(name="prometheus", configured=True, mode="enabled", notes="GET /metrics"),
        IntegrationStatus(
            name="gemini",
            configured=bool(s.gemini_api_key),
            mode="llm insights" if s.gemini_api_key else "heuristic fallback",
        ),
        IntegrationStatus(
            name="openai",
            configured=bool(s.openai_api_key),
            mode="llm insights" if s.openai_api_key else "heuristic fallback",
        ),
        IntegrationStatus(name="jwt-auth", configured=True, mode="PyJWT HS256", notes=f"ttl {s.jwt_ttl_minutes}m"),
        IntegrationStatus(name="rate-limit", configured=s.rate_limit_enabled, mode=s.rate_limit_default),
    ]


@router.get("", response_model=IntegrationsResponse, summary="Integration registry")
async def integrations() -> IntegrationsResponse:
    items = _items()
    return IntegrationsResponse(
        count=len(items),
        configured=sum(1 for i in items if i.configured),
        integrations=items,
    )

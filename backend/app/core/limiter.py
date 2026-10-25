"""Rate limiting (slowapi). Enabled by default; disable with RATE_LIMIT_ENABLED=false."""

from __future__ import annotations

from slowapi import Limiter
from slowapi.util import get_remote_address

from app.config import settings

limiter = Limiter(
    key_func=get_remote_address,
    default_limits=[settings.rate_limit_default] if settings.rate_limit_enabled else [],
    headers_enabled=True,
    enabled=settings.rate_limit_enabled,
    storage_uri=settings.redis_url or "memory://",
)

SEARCH_LIMIT = settings.rate_limit_search

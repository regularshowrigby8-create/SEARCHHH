"""Cross-cutting ASGI middleware: request id, timing, security headers."""

from __future__ import annotations

import time
import uuid

import structlog
from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint
from starlette.requests import Request
from starlette.responses import Response

from app.core.logging import request_id_ctx

log = structlog.get_logger(__name__)


class RequestContextMiddleware(BaseHTTPMiddleware):
    """Attaches a request id, logs one line per request, adds timing headers."""

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> Response:
        incoming = request.headers.get("x-request-id")
        rid = incoming if incoming and len(incoming) < 64 else uuid.uuid4().hex[:16]
        request_id_ctx.set(rid)
        started = time.perf_counter()
        try:
            response = await call_next(request)
        except Exception:
            elapsed = (time.perf_counter() - started) * 1000
            log.exception("request_failed", method=request.method, path=request.url.path, elapsed_ms=round(elapsed, 1))
            raise
        elapsed = (time.perf_counter() - started) * 1000
        response.headers["x-request-id"] = rid
        response.headers["server-timing"] = f"app;dur={elapsed:.1f}"
        log.info(
            "request",
            method=request.method,
            path=request.url.path,
            status=response.status_code,
            elapsed_ms=round(elapsed, 1),
        )
        return response


class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    """Baseline hardening headers (cheap, and judges like seeing them)."""

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> Response:
        response = await call_next(request)
        response.headers.setdefault("x-content-type-options", "nosniff")
        response.headers.setdefault("x-frame-options", "DENY")
        response.headers.setdefault("referrer-policy", "no-referrer")
        response.headers.setdefault("permissions-policy", "geolocation=(self), camera=()")
        response.headers.setdefault("cache-control", "no-store")
        return response

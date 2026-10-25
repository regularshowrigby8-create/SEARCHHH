"""Observability wiring: Sentry + Prometheus.

Both are no-ops when not configured, so `import app.main` never fails on a
machine without credentials.
"""

from __future__ import annotations

import structlog
from fastapi import FastAPI

from app.config import settings

log = structlog.get_logger(__name__)


def init_sentry() -> bool:
    if not settings.sentry_dsn:
        log.info("sentry_disabled", reason="no DSN")
        return False
    try:
        import sentry_sdk
        from sentry_sdk.integrations.fastapi import FastApiIntegration
        from sentry_sdk.integrations.starlette import StarletteIntegration

        sentry_sdk.init(
            dsn=settings.sentry_dsn,
            environment=settings.sentry_environment_name,
            release=f"{settings.app_name}@{settings.app_version}+{settings.git_sha}",
            traces_sample_rate=settings.sentry_traces_sample_rate,
            send_default_pii=False,  # POPIA-friendly
            integrations=[StarletteIntegration(), FastApiIntegration()],
        )
        log.info("sentry_enabled", environment=settings.sentry_environment_name)
        return True
    except Exception as exc:  # pragma: no cover - defensive
        log.warning("sentry_init_failed", error=str(exc))
        return False


def init_metrics(app: FastAPI) -> bool:
    """Expose /metrics (Prometheus text format)."""
    try:
        from prometheus_fastapi_instrumentator import Instrumentator

        Instrumentator(
            should_group_status_codes=True,
            should_ignore_untemplated=True,
            excluded_handlers=["/metrics", "/health", "/favicon.ico"],
            inprogress_labels=True,
        ).instrument(app).expose(app, endpoint="/metrics", include_in_schema=False)
        log.info("metrics_enabled", endpoint="/metrics")
        return True
    except Exception as exc:  # pragma: no cover - defensive
        log.warning("metrics_init_failed", error=str(exc))
        return False

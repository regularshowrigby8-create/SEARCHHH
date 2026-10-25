"""Structured logging (structlog) + request-id correlation.

Imported by `app.main` at startup; every module just calls
``structlog.get_logger(__name__)``.
"""

from __future__ import annotations

import logging
import sys
import uuid
from contextvars import ContextVar
from typing import Any

import structlog

from app.config import settings

request_id_ctx: ContextVar[str] = ContextVar("request_id", default="-")


def new_request_id() -> str:
    rid = uuid.uuid4().hex[:16]
    request_id_ctx.set(rid)
    return rid


def _add_request_id(_: Any, __: str, event_dict: dict[str, Any]) -> dict[str, Any]:
    event_dict["request_id"] = request_id_ctx.get()
    event_dict["app"] = settings.app_name
    event_dict["version"] = settings.app_version
    return event_dict


def configure_logging() -> None:
    """Human-readable locally, JSON in containers/CI."""
    level = getattr(logging, settings.log_level, logging.INFO)
    shared = [
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso", utc=True),
        _add_request_id,
        structlog.processors.StackInfoRenderer(),
        structlog.processors.format_exc_info,
    ]
    if settings.log_json or settings.is_production:
        renderer: structlog.types.Processor = structlog.processors.JSONRenderer()
    else:
        renderer = structlog.dev.ConsoleRenderer(colors=sys.stderr.isatty())
        shared.insert(4, structlog.processors.ExceptionRenderer())

    structlog.configure(
        processors=[*shared, renderer],
        wrapper_class=structlog.make_filtering_bound_logger(level),
        logger_factory=structlog.PrintLoggerFactory(),
        cache_logger_on_first_use=True,
    )
    logging.basicConfig(level=level, format="%(message)s", stream=sys.stdout, force=True)
    for noisy in ("httpx", "httpcore", "urllib3", "botocore"):
        logging.getLogger(noisy).setLevel(logging.WARNING)


def get_logger(name: str) -> structlog.stdlib.BoundLogger:
    return structlog.get_logger(name)  # type: ignore[no-any-return]

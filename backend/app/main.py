"""SEARCHHH API - FastAPI application factory.

Run locally:
    uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import HTMLResponse, JSONResponse
from slowapi.errors import RateLimitExceeded

from app.api.router import api_router
from app.api.routes.health import router as health_router
from app.config import settings
from app.core.errors import register_exception_handlers
from app.core.limiter import limiter
from app.core.logging import configure_logging, get_logger
from app.core.middleware import RequestContextMiddleware, SecurityHeadersMiddleware
from app.core.observability import init_metrics, init_sentry
from app.services.container import container

configure_logging()
log = get_logger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    app.state.container = container
    log.info("startup", env=settings.env, version=settings.app_version, git_sha=settings.git_sha)
    await container.startup()
    yield
    await container.shutdown()
    log.info("shutdown")


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.app_name,
        version=settings.app_version,
        description=(
            "Backend for the SEARCHHH Android app. Every integration is optional; "
            "the service is fully functional in offline/demo mode."
        ),
        docs_url="/docs",
        redoc_url="/redoc",
        openapi_url="/openapi.json",
        lifespan=lifespan,
    )
    app.state.limiter = limiter
    app.state.container = container

    app.add_middleware(RequestContextMiddleware)
    app.add_middleware(SecurityHeadersMiddleware)

    from fastapi.middleware.cors import CORSMiddleware
    from fastapi.middleware.gzip import GZipMiddleware

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origin_list,
        allow_credentials=False,
        allow_methods=["GET", "POST", "OPTIONS"],
        allow_headers=["*"],
        expose_headers=["x-request-id", "server-timing"],
    )
    app.add_middleware(GZipMiddleware, minimum_size=1024)

    register_exception_handlers(app)
    init_sentry()
    init_metrics(app)

    @app.exception_handler(RateLimitExceeded)
    async def _rate_limited(request: Request, exc: RateLimitExceeded):
        return JSONResponse(
            status_code=429,
            content={"title": "rate_limited", "status": 429, "detail": f"Limit exceeded: {exc.detail}"},
        )

    app.include_router(api_router, prefix=settings.api_v1_prefix)
    app.include_router(health_router)  # also at root: /health, /ready

    @app.get("/", response_class=HTMLResponse, include_in_schema=False)
    async def index() -> str:
        return f"""<!doctype html><html><head><meta charset="utf-8">
<title>{settings.app_name}</title>
<style>body{{font-family:system-ui;margin:3rem auto;max-width:44rem;line-height:1.55}}
code{{background:#f2f2f2;padding:.1rem .3rem;border-radius:4px}}</style></head><body>
<h1>{settings.app_name} <small>v{settings.app_version}</small></h1>
<p>Backend for the SEARCHHH Android client. Environment: <code>{settings.env}</code></p>
<ul>
<li><a href="/docs">Swagger UI</a></li>
<li><a href="/redoc">ReDoc</a></li>
<li><a href="{settings.api_v1_prefix}/search?q=coffee">Example search</a></li>
<li><a href="/health">Health</a></li>
<li><a href="{settings.api_v1_prefix}/integrations">Integration registry</a></li>
<li><a href="/metrics">Prometheus metrics</a></li>
</ul></body></html>"""

    @app.get("/version", include_in_schema=False)
    async def version() -> dict[str, str]:
        return {
            "app": settings.app_name,
            "version": settings.app_version,
            "env": settings.env,
            "git_sha": settings.git_sha,
        }

    return app


app = create_app()

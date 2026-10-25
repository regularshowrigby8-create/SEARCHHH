"""Aggregate all versioned routers under one prefix."""

from __future__ import annotations

from fastapi import APIRouter

from app.api.routes import admin, auth, feedback, health, insights, integrations, search

api_router = APIRouter()
api_router.include_router(health.router)
api_router.include_router(search.router)
api_router.include_router(insights.router)
api_router.include_router(feedback.router)
api_router.include_router(auth.router)
api_router.include_router(integrations.router)
api_router.include_router(admin.router)

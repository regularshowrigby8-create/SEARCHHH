"""Application settings.

Every integration is optional: the service boots and stays *functional* with
zero credentials (offline / demo mode). That is a hard requirement for a
hackathon submission - judges must be able to run the app without being handed
a pile of API keys.
"""

from __future__ import annotations

import functools
import os
from typing import Literal

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

Env = Literal["local", "ci", "staging", "production"]


class Settings(BaseSettings):
    """Typed, validated configuration loaded from env vars / .env file."""

    model_config = SettingsConfigDict(
        env_file=(".env", "../.env"),
        env_file_encoding="utf-8",
        env_prefix="",
        extra="ignore",
        case_sensitive=False,
    )

    # --- identity ---------------------------------------------------------
    app_name: str = "SEARCHHH API"
    app_version: str = "0.1.0"
    env: Env = "local"
    debug: bool = True
    api_v1_prefix: str = "/api/v1"
    git_sha: str = Field(default_factory=lambda: os.getenv("GIT_SHA", "dev"))

    # --- http -------------------------------------------------------------
    host: str = "0.0.0.0"
    port: int = 8000
    cors_origins: str = "*"
    log_level: str = "INFO"
    log_json: bool = False
    request_timeout_s: float = 20.0

    # --- auth -------------------------------------------------------------
    jwt_secret: str = "dev-only-insecure-secret-change-me"
    jwt_algorithm: str = "HS256"
    jwt_ttl_minutes: int = 720
    dev_auth_enabled: bool = True
    api_key: str = ""  # optional shared key for write endpoints

    # --- rate limiting ----------------------------------------------------
    rate_limit_default: str = "120/minute"
    rate_limit_search: str = "60/minute"
    rate_limit_enabled: bool = True

    # --- supabase (primary data plane) ------------------------------------
    supabase_url: str = ""
    supabase_anon_key: str = ""
    supabase_service_role_key: str = ""
    supabase_table_entries: str = "entries"
    supabase_table_feedback: str = "feedback"

    # --- database / cache -------------------------------------------------
    database_url: str = ""  # postgresql+asyncpg://...
    redis_url: str = ""

    # --- observability ----------------------------------------------------
    sentry_dsn: str = ""
    sentry_traces_sample_rate: float = 0.25
    sentry_environment: str = ""

    # --- AI providers (all optional, each has a deterministic fallback) ---
    gemini_api_key: str = ""
    gemini_model: str = "gemini-2.0-flash"
    openai_api_key: str = ""
    openai_model: str = "gpt-4o-mini"

    # --- product knobs ----------------------------------------------------
    search_max_limit: int = 50
    search_default_limit: int = 20
    fuzzy_threshold: float = 62.0

    @field_validator("log_level")
    @classmethod
    def _upper(cls, v: str) -> str:
        return v.upper()

    @property
    def cors_origin_list(self) -> list[str]:
        raw = (self.cors_origins or "").strip()
        if not raw or raw == "*":
            return ["*"]
        return [o.strip() for o in raw.split(",") if o.strip()]

    @property
    def is_production(self) -> bool:
        return self.env == "production"

    @property
    def supabase_configured(self) -> bool:
        return bool(self.supabase_url and (self.supabase_anon_key or self.supabase_service_role_key))

    @property
    def sentry_environment_name(self) -> str:
        return self.sentry_environment or self.env


@functools.lru_cache(maxsize=1)
def get_settings() -> Settings:
    """Cached settings singleton (import this, not `Settings()`)."""
    return Settings()


settings = get_settings()

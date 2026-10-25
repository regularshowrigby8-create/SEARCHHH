"""Auth primitives: JWT (PyJWT), password hashing (Argon2), API-key guard."""

from __future__ import annotations

from datetime import UTC, datetime, timedelta
from typing import Any

import jwt
from argon2 import PasswordHasher
from argon2.exceptions import VerificationError, VerifyMismatchError
from fastapi import Request

from app.config import settings
from app.core.errors import Unauthorized

_hasher = PasswordHasher()


def hash_password(raw: str) -> str:
    return _hasher.hash(raw)


def verify_password(raw: str, hashed: str) -> bool:
    try:
        return _hasher.verify(hashed, raw)
    except (VerifyMismatchError, VerificationError, Exception):
        return False


def create_access_token(subject: str, scopes: list[str] | None = None) -> tuple[str, int]:
    ttl = settings.jwt_ttl_minutes * 60
    now = datetime.now(UTC)
    payload: dict[str, Any] = {
        "sub": subject,
        "scopes": scopes or ["read"],
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(seconds=ttl)).timestamp()),
        "iss": settings.app_name,
    }
    token = jwt.encode(payload, settings.jwt_secret, algorithm=settings.jwt_algorithm)
    return token, ttl


def decode_access_token(token: str) -> dict[str, Any]:
    try:
        return jwt.decode(
            token,
            settings.jwt_secret,
            algorithms=[settings.jwt_algorithm],
            options={"require": ["exp", "sub"]},
        )
    except jwt.ExpiredSignatureError as exc:
        raise Unauthorized("Token expired") from exc
    except jwt.InvalidTokenError as exc:
        raise Unauthorized("Invalid token") from exc


def bearer_token(request: Request) -> str | None:
    header = request.headers.get("authorization", "")
    if header.lower().startswith("bearer "):
        return header[7:].strip()
    return None


async def require_token(request: Request) -> dict[str, Any]:
    """Dependency: any valid JWT (demo tokens included)."""
    token = bearer_token(request)
    if not token:
        raise Unauthorized("Missing bearer token")
    return decode_access_token(token)


async def require_api_key(request: Request) -> None:
    """Dependency for write endpoints: shared key *or* valid JWT."""
    if settings.api_key:
        supplied = request.headers.get("x-api-key", "")
        if supplied == settings.api_key:
            return
    if bearer_token(request):
        decode_access_token(bearer_token(request) or "")
        return
    if not settings.api_key:
        # Open in local/demo mode - documented in docs/SECURITY.md
        return
    raise Unauthorized("Provide x-api-key or a bearer token")

"""Demo auth. Production would delegate to Supabase Auth / OIDC - the token
shape stays identical, so the Kotlin client does not change.
"""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Depends

from app.config import settings
from app.core.errors import Unauthorized
from app.core.security import create_access_token, require_token
from app.models.schemas import TokenRequest, TokenResponse

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/token", response_model=TokenResponse, summary="Issue a demo JWT")
async def issue_token(payload: TokenRequest) -> TokenResponse:
    if not settings.dev_auth_enabled:
        raise Unauthorized("Demo token issuing is disabled on this deployment")
    token, ttl = create_access_token(payload.subject, payload.scopes)
    return TokenResponse(access_token=token, expires_in=ttl, scopes=payload.scopes)


@router.get("/me", summary="Echo the caller identity")
async def me(claims: Annotated[dict, Depends(require_token)]) -> dict[str, object]:
    return {"subject": claims.get("sub"), "scopes": claims.get("scopes", []), "issuer": claims.get("iss")}

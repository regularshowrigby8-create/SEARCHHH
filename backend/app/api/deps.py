"""Shared FastAPI dependencies."""

from __future__ import annotations

from typing import Annotated

from fastapi import Depends, Request

from app.core.security import require_api_key, require_token
from app.services.container import Container
from app.services.search import SearchEngine


def get_container(request: Request) -> Container:
    return request.app.state.container  # type: ignore[no-any-return]


def get_engine(container: Annotated[Container, Depends(get_container)]) -> SearchEngine:
    return container.engine


AuthDep = Annotated[dict, Depends(require_token)]
WriteAuthDep = Annotated[None, Depends(require_api_key)]
EngineDep = Annotated[SearchEngine, Depends(get_engine)]
ContainerDep = Annotated[Container, Depends(get_container)]

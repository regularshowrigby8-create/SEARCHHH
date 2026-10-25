"""Search endpoints - the core of the app."""

from __future__ import annotations

from typing import Annotated

from fastapi import APIRouter, Path, Query, Request, Response

from app.api.deps import ContainerDep, EngineDep
from app.core.limiter import SEARCH_LIMIT, limiter
from app.models.schemas import Entry, ScoredEntry, SearchRequest, SearchResponse

router = APIRouter(prefix="/search", tags=["search"])


@router.get("", response_model=SearchResponse, summary="Search the catalog")
@limiter.limit(SEARCH_LIMIT)
async def search_get(
    request: Request,  # required by slowapi (inspects the client address)
    response: Response,  # required by slowapi (injects rate-limit headers)
    engine: EngineDep,
    q: Annotated[str, Query(max_length=160, description="Free-text query")] = "",
    category: Annotated[str | None, Query(max_length=64)] = None,
    limit: Annotated[int, Query(ge=1, le=50)] = 20,
    offset: Annotated[int, Query(ge=0, le=5000)] = 0,
    sort: Annotated[str, Query(pattern="^(relevance|rating|price_asc|price_desc|recent)$")] = "relevance",
) -> SearchResponse:
    payload = SearchRequest(q=q, category=category, limit=limit, offset=offset, sort=sort)  # type: ignore[arg-type]
    return engine.search(payload)


@router.post("", response_model=SearchResponse, summary="Search (body flavour, used by the app)")
async def search_post(engine: EngineDep, payload: SearchRequest) -> SearchResponse:
    return engine.search(payload)


@router.get("/suggest", summary="Autocomplete suggestions")
async def suggest(
    engine: EngineDep,
    q: Annotated[str, Query(max_length=64, description="Prefix typed so far")] = "",
    limit: Annotated[int, Query(ge=1, le=10)] = 5,
) -> dict[str, object]:
    return {"query": q, "suggestions": engine.suggest(q, limit=limit)}


@router.get("/categories", summary="Facet counts")
async def categories(engine: EngineDep) -> dict[str, object]:
    facets = [{"name": name, "count": count} for name, count in engine.categories()]
    return {"count": len(facets), "categories": facets}


@router.get("/{entry_id}", response_model=ScoredEntry, summary="Fetch one entry")
async def get_entry(engine: EngineDep, entry_id: Annotated[str, Path(max_length=64)]) -> ScoredEntry:
    from app.core.errors import NotFound

    for doc in engine.docs:
        if doc.entry.id == entry_id:
            entry: Entry = doc.entry
            return ScoredEntry(**entry.model_dump())
    raise NotFound(f"No entry with id '{entry_id}'")


@router.get("/nearby/all", summary="Geo-sorted entries (demo data has coords in `location`)")
async def nearby(container: ContainerDep) -> dict[str, object]:
    entries = container.engine.entries
    located = [e for e in entries if e.location]
    return {"count": len(located), "entries": [e.model_dump() for e in located[:20]]}

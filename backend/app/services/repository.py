"""Data access layer with a Supabase backend and an in-memory fallback.

The fallback is not a toy: it is what makes the demo work on a judge's laptop
with no `.env`, and it is what the test-suite runs against. Both implementations
satisfy the same `EntryRepository` protocol, so swapping is a one-line change
(see `container.py`).
"""

from __future__ import annotations

import uuid
from datetime import UTC, datetime
from typing import Any, Protocol, runtime_checkable

from app.config import settings
from app.core.logging import get_logger
from app.models.schemas import Entry, FeedbackRequest
from app.services.catalog import load_seed_entries

log = get_logger(__name__)


@runtime_checkable
class EntryRepository(Protocol):
    name: str

    async def list_entries(self) -> list[Entry]: ...

    async def store_feedback(self, payload: FeedbackRequest) -> tuple[str, str]: ...

    async def health(self) -> tuple[bool, str]: ...


class MemoryRepository:
    """Zero-dependency repository backed by the bundled seed catalog."""

    name = "memory"

    def __init__(self) -> None:
        self._feedback: list[dict[str, Any]] = []

    async def list_entries(self) -> list[Entry]:
        return list(load_seed_entries())

    async def store_feedback(self, payload: FeedbackRequest) -> tuple[str, str]:
        record_id = uuid.uuid4().hex
        self._feedback.append({**payload.model_dump(), "id": record_id, "created_at": datetime.now(UTC).isoformat()})
        log.info("feedback_stored", repo=self.name, feedback_id=record_id, rating=payload.rating)
        return record_id, self.name

    async def health(self) -> tuple[bool, str]:
        return True, f"memory catalog: {len(load_seed_entries())} entries, {len(self._feedback)} feedback"

    # test helper
    @property
    def feedback(self) -> list[dict[str, Any]]:
        return self._feedback


class SupabaseRepository:
    """Supabase (PostgREST) implementation used when credentials are present."""

    name = "supabase"

    def __init__(self) -> None:
        self._client: Any | None = None

    async def _get_client(self) -> Any:
        if self._client is None:
            from supabase import acreate_client  # imported lazily: keeps cold start fast

            key = settings.supabase_service_role_key or settings.supabase_anon_key
            self._client = await acreate_client(settings.supabase_url, key)
        return self._client

    async def list_entries(self) -> list[Entry]:
        client = await self._get_client()
        response = await client.table(settings.supabase_table_entries).select("*").limit(1000).execute()
        rows = getattr(response, "data", None) or []
        entries: list[Entry] = []
        for row in rows:
            try:
                entries.append(Entry.model_validate(row))
            except Exception as exc:  # one bad row must not kill the whole feed
                log.warning("supabase_row_invalid", error=str(exc), row_id=row.get("id"))
        return entries

    async def store_feedback(self, payload: FeedbackRequest) -> tuple[str, str]:
        client = await self._get_client()
        response = await client.table(settings.supabase_table_feedback).insert(payload.model_dump()).execute()
        rows = getattr(response, "data", None) or []
        record_id = str(rows[0].get("id")) if rows else uuid.uuid4().hex
        log.info("feedback_stored", repo=self.name, feedback_id=record_id, rating=payload.rating)
        return record_id, self.name

    async def health(self) -> tuple[bool, str]:
        try:
            client = await self._get_client()
            await client.table(settings.supabase_table_entries).select("id").limit(1).execute()
            return True, "supabase reachable"
        except Exception as exc:
            log.warning("supabase_health_failed", error=str(exc))
            return False, f"supabase error: {type(exc).__name__}"


def build_repository() -> EntryRepository:
    if settings.supabase_configured:
        log.info("repository_selected", repo="supabase", url=settings.supabase_url)
        return SupabaseRepository()
    log.info("repository_selected", repo="memory", reason="no supabase credentials")
    return MemoryRepository()

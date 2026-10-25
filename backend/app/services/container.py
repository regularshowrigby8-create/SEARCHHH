"""Tiny service container (no DI framework needed at this size).

FastAPI dependencies pull from here; tests call `reset_container()` to get a
fresh engine over patched data.
"""

from __future__ import annotations

import threading

from app.core.logging import get_logger
from app.models.schemas import Entry
from app.services.repository import EntryRepository, build_repository
from app.services.search import SearchEngine

log = get_logger(__name__)


class Container:
    def __init__(self) -> None:
        self._repo: EntryRepository | None = None
        self._engine: SearchEngine | None = None
        self._lock = threading.Lock()

    @property
    def repo(self) -> EntryRepository:
        if self._repo is None:
            self._repo = build_repository()
        return self._repo

    @property
    def engine(self) -> SearchEngine:
        if self._engine is None:
            raise RuntimeError("Container not initialised - call await container.startup()")
        return self._engine

    @property
    def ready(self) -> bool:
        return self._engine is not None

    async def startup(self) -> None:
        with self._lock:
            entries = await self.repo.list_entries()
            if not entries:
                from app.services.catalog import load_seed_entries

                log.warning("empty_repository_falling_back_to_seed")
                entries = list(load_seed_entries())
            self._engine = SearchEngine(entries=entries)
            log.info("search_engine_ready", documents=len(entries), repository=self.repo.name)

    async def refresh(self, entries: list[Entry] | None = None) -> int:
        """Pull the catalog again (used by the /admin/refresh endpoint + cron)."""
        with self._lock:
            if entries is None:
                entries = await self.repo.list_entries()
            self._engine = SearchEngine(entries=entries)
            log.info("search_engine_refreshed", documents=len(entries))
            return len(entries)

    async def shutdown(self) -> None:
        self._engine = None
        log.info("container_shutdown")


container = Container()


def reset_container() -> None:
    global container
    container = Container()

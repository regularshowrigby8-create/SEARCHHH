"""Seed catalog loader.

Swap `backend/app/data/seed.json` for your own domain (marketplace listings,
campus services, venues, stock, whatever the app is about) - nothing else
changes, because the search engine is schema-driven.
"""

from __future__ import annotations

import json
from functools import lru_cache
from pathlib import Path

from app.models.schemas import Entry

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
SEED_FILE = DATA_DIR / "seed.json"


@lru_cache(maxsize=1)
def load_seed_entries() -> tuple[Entry, ...]:
    """Load and validate the bundled catalog. Cached for process lifetime."""
    if not SEED_FILE.exists():  # pragma: no cover - defensive
        return ()
    raw = json.loads(SEED_FILE.read_text(encoding="utf-8"))
    items = raw["entries"] if isinstance(raw, dict) else raw
    return tuple(Entry.model_validate(item) for item in items)


def reload_seed() -> int:
    """Clear the cache (used by tests / hot-reload in dev)."""
    load_seed_entries.cache_clear()
    return len(load_seed_entries())

"""Pytest fixtures. The suite runs against the in-memory repository - no
network, no credentials, no database, ~1s runtime.
"""

from __future__ import annotations

import os

os.environ.setdefault("ENV", "ci")
os.environ.setdefault("LOG_LEVEL", "WARNING")
os.environ.setdefault("RATE_LIMIT_ENABLED", "false")
os.environ.setdefault("DEV_AUTH_ENABLED", "true")
os.environ.setdefault("SENTRY_DSN", "")

import pytest
from app.main import create_app
from app.services.catalog import load_seed_entries
from app.services.search import SearchEngine
from fastapi.testclient import TestClient


@pytest.fixture(scope="session")
def entries():
    return list(load_seed_entries())


@pytest.fixture()
def engine(entries) -> SearchEngine:
    return SearchEngine(entries=entries)


@pytest.fixture()
def app():
    return create_app()


@pytest.fixture()
def client(app):
    with TestClient(app) as test_client:
        yield test_client

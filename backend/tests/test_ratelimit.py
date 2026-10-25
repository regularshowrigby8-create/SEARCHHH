"""Regression tests for the rate-limit path.

The main suite disables rate limiting for determinism, which once hid a real
bug (slowapi needs a `Response` parameter to inject headers). This module turns
the limiter back on and exercises the decorated endpoint.
"""

from __future__ import annotations

import pytest
from app.core.limiter import limiter


@pytest.fixture()
def rate_limited_client(client):
    previous = limiter.enabled
    limiter.enabled = True
    try:
        yield client
    finally:
        limiter.enabled = previous


def test_search_endpoint_works_with_limiter_enabled(rate_limited_client):
    response = rate_limited_client.get("/api/v1/search", params={"q": "coffee"})
    assert response.status_code == 200
    assert response.json()["results"]


def test_rate_limit_headers_are_injected(rate_limited_client):
    response = rate_limited_client.get("/api/v1/search", params={"q": "coffee"})
    headers = {k.lower() for k in response.headers}
    assert any(h.startswith("x-ratelimit") for h in headers)


def test_burst_eventually_throttles(rate_limited_client):
    statuses = [rate_limited_client.get("/api/v1/search", params={"q": "coffee"}).status_code for _ in range(65)]
    assert 429 in statuses, "expected the search limit to kick in during a burst"

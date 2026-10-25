"""HTTP-level tests against the real ASGI app (TestClient)."""

from __future__ import annotations


def test_root_html(client):
    response = client.get("/")
    assert response.status_code == 200
    assert "SEARCHHH" in response.text


def test_health(client):
    body = client.get("/health").json()
    assert body["status"] == "ok"
    assert body["service"]
    assert "repository" in body["checks"]


def test_ready_after_startup(client):
    body = client.get("/ready").json()
    assert body["ready"] is True
    assert body["documents"] >= 25


def test_openapi_contract(client):
    spec = client.get("/openapi.json").json()
    paths = spec["paths"]
    assert "/api/v1/search" in paths
    assert "/api/v1/insights" in paths
    assert "/api/v1/feedback" in paths


def test_search_get(client):
    response = client.get("/api/v1/search", params={"q": "nyda", "limit": 5})
    assert response.status_code == 200
    body = response.json()
    assert body["query"] == "nyda"
    assert body["results"][0]["id"] == "gl-011"


def test_search_post_matches_get(client):
    get_body = client.get("/api/v1/search", params={"q": "mentor"}).json()
    post_body = client.post("/api/v1/search", json={"q": "mentor", "limit": 20}).json()
    assert [r["id"] for r in get_body["results"]] == [r["id"] for r in post_body["results"]]


def test_search_validation(client):
    assert client.get("/api/v1/search", params={"limit": 999}).status_code == 422
    assert client.get("/api/v1/search", params={"sort": "nope"}).status_code == 422


def test_get_single_entry_and_404(client):
    assert client.get("/api/v1/search/gl-001").json()["title"] == "YES Programme Placement"
    missing = client.get("/api/v1/search/does-not-exist")
    assert missing.status_code == 404
    assert missing.json()["title"] == "not_found"


def test_suggestions_and_categories(client):
    assert client.get("/api/v1/search/suggest", params={"q": "fund"}).json()["suggestions"]
    categories = client.get("/api/v1/search/categories").json()
    assert categories["count"] >= 5


def test_insight_falls_back_to_heuristic(client):
    body = client.post("/api/v1/insights", json={"entry_id": "gl-001", "audience": "unemployed graduate"}).json()
    assert body["provider"] == "heuristic"
    assert "YES Programme Placement" in body["insight"]
    assert client.post("/api/v1/insights", json={"entry_id": "nope"}).status_code == 404


def test_insight_is_cached(client):
    payload = {"entry_id": "gl-011", "audience": "first-time founder"}
    assert client.post("/api/v1/insights", json=payload).json()["cached"] is False
    assert client.post("/api/v1/insights", json=payload).json()["cached"] is True


def test_feedback_accepted(client):
    response = client.post(
        "/api/v1/feedback",
        json={"message": "Search feels instant", "rating": 5, "platform": "android", "app_version": "1.0.0"},
    )
    assert response.status_code == 201
    assert response.json()["accepted"] is True
    assert response.json()["stored_in"] == "memory"


def test_feedback_validation(client):
    assert client.post("/api/v1/feedback", json={"message": "x"}).status_code == 422


def test_auth_token_and_me(client):
    token = client.post("/api/v1/auth/token", json={"subject": "judge", "scopes": ["read", "write"]}).json()
    assert token["token_type"] == "bearer"
    me = client.get("/api/v1/auth/me", headers={"Authorization": f"Bearer {token['access_token']}"}).json()
    assert me["subject"] == "judge"
    assert me["scopes"] == ["read", "write"]


def test_auth_missing_or_bad_token(client):
    assert client.get("/api/v1/auth/me").status_code == 401
    assert client.get("/api/v1/auth/me", headers={"Authorization": "Bearer nope"}).status_code == 401


def test_integrations_registry(client):
    body = client.get("/api/v1/integrations").json()
    assert body["count"] >= 8
    assert any(i["name"] == "supabase" for i in body["integrations"])


def test_admin_stats_and_refresh(client):
    stats = client.get("/api/v1/admin/stats").json()
    assert stats["documents"] >= 25
    assert stats["vocabulary"] > 100
    assert client.post("/api/v1/admin/refresh").json()["documents"] >= 25


def test_metrics_exposed(client):
    response = client.get("/metrics")
    assert response.status_code == 200
    assert "http_request" in response.text


def test_security_headers_and_request_id(client):
    response = client.get("/health", headers={"x-request-id": "abc123"})
    assert response.headers["x-request-id"] == "abc123"
    assert response.headers["x-content-type-options"] == "nosniff"
    assert "server-timing" in response.headers


def test_problem_details_shape(client):
    body = client.get("/api/v1/search/missing-id").json()
    assert set(body) >= {"type", "title", "status", "detail", "instance"}


def test_funding_match_endpoint(client):
    response = client.post(
        "/api/v1/funding/match",
        json={"age": 24, "stage": "startup", "registered": True, "training_completed": True},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["eligible_count"] >= 1
    assert any(m["id"] == "nyda-threshold-2" and m["eligible"] for m in body["matches"])
    assert body["next_actions"]


def test_funding_match_validation(client):
    assert client.post("/api/v1/funding/match", json={"age": 5}).status_code == 422
    assert client.post("/api/v1/funding/match", json={"stage": "series-a"}).status_code == 422


def test_funding_rules_endpoint(client):
    body = client.get("/api/v1/funding/rules").json()
    assert body["count"] >= 6
    assert all(rule["source"] for rule in body["rules"])

"""Unit tests for the ranking engine."""

from __future__ import annotations

import pytest
from app.models.schemas import SearchRequest
from app.services.search import SearchEngine, expand, normalise, tokenize


def test_seed_catalog_is_non_trivial(entries):
    assert len(entries) >= 25
    assert all(e.title for e in entries)
    assert len({e.id for e in entries}) == len(entries)


def test_tokenize_strips_stopwords_and_accents():
    assert tokenize("The Café  for  GRADUATES") == ["cafe", "graduates"]
    assert normalise("Ökonomie") == "okonomie"


def test_expand_adds_synonyms():
    expanded = expand(["funding"])
    assert "funding" in expanded and "grant" in expanded


@pytest.mark.parametrize(
    ("query", "expected_id"),
    [
        ("yes programme", "gl-001"),
        ("internship", "gl-002"),
        ("learnership software testing", "gl-003"),
        ("nyda grant", "gl-011"),
        ("sefa loan", "gl-012"),
        ("co-founder", "gl-014"),
        ("mentor", "gl-015"),
        ("cv builder", "gl-008"),
        ("interview practice", "gl-009"),
        ("salary guide", "gl-027"),
        ("women funding", "gl-029"),
    ],
)
def test_queries_hit_expected_top_result(engine, query, expected_id):
    response = engine.search(SearchRequest(q=query, limit=5))
    assert response.results, f"no results for {query!r}"
    assert response.results[0].id == expected_id


@pytest.mark.parametrize(("typo", "expected_id"), [("nyad", "gl-011"), ("intenship", "gl-002")])
def test_typo_tolerance(engine, typo, expected_id):
    response = engine.search(SearchRequest(q=typo, limit=3))
    assert response.results, f"no fuzzy match for {typo!r}"
    assert expected_id in {r.id for r in response.results}


def test_empty_query_returns_discover_feed(engine):
    response = engine.search(SearchRequest(q="", limit=5))
    assert len(response.results) == 5
    assert response.total == len(engine.entries)


def test_limit_and_offset_paginate(engine):
    first = engine.search(SearchRequest(q="", limit=5, offset=0))
    second = engine.search(SearchRequest(q="", limit=5, offset=5))
    assert {r.id for r in first.results}.isdisjoint({r.id for r in second.results})


def test_category_facet_filters(engine):
    response = engine.search(SearchRequest(q="", category="funding", limit=50))
    assert response.results
    assert {r.category for r in response.results} == {"funding"}


@pytest.mark.parametrize("sort", ["rating", "price_asc", "price_desc", "recent", "relevance"])
def test_sort_modes_return_sorted_results(engine, sort):
    response = engine.search(SearchRequest(q="student", sort=sort, limit=10))
    values = [r.rating for r in response.results]
    if sort == "rating":
        assert values == sorted(values, reverse=True)
    if sort == "price_asc":
        prices = [r.price for r in response.results if r.price is not None]
        assert prices == sorted(prices)


def test_results_carry_explanation(engine):
    response = engine.search(SearchRequest(q="nyda"))
    top = response.results[0]
    assert top.score > 0
    assert top.highlights


def test_suggestions_are_prefix_based(engine):
    suggestions = engine.suggest("fun")
    assert suggestions
    assert any(s.lower().startswith("fun") for s in suggestions)
    assert engine.suggest("x") == []


def test_categories_sorted_by_count(engine):
    categories = engine.categories()
    assert categories == sorted(categories, key=lambda kv: (-kv[1], kv[0]))


def test_search_is_fast(engine):
    response = engine.search(SearchRequest(q="graduate no experience", limit=20))
    assert response.took_ms < 100


def test_engine_handles_empty_catalog():
    empty = SearchEngine(entries=[])
    response = empty.search(SearchRequest(q="anything"))
    assert response.total == 0

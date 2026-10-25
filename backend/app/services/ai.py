"""Provider-agnostic "insight" generator.

Order of preference: Gemini -> OpenAI -> deterministic heuristic.
Every provider is optional and every failure degrades instead of raising, so the
Android UI always has something to render.
"""

from __future__ import annotations

import hashlib
from typing import Any

import httpx
from cachetools import TTLCache

from app.config import settings
from app.core.logging import get_logger
from app.models.schemas import Entry, InsightResponse

log = get_logger(__name__)

_cache: TTLCache[str, InsightResponse] = TTLCache(maxsize=512, ttl=900)

SYSTEM_PROMPT = (
    "You are a concise product assistant inside a mobile app. "
    "Given one listing, write 2 short sentences (max 45 words) that help the user decide. "
    "Be concrete, friendly, no markdown, no emoji, no invented facts."
)


def _prompt(entry: Entry, audience: str) -> str:
    bits = [
        f"Title: {entry.title}",
        f"Category: {entry.category}",
        f"Summary: {entry.subtitle or entry.description[:200]}",
        f"Tags: {', '.join(entry.tags)}",
        f"Price: {entry.price} {entry.currency}" if entry.price is not None else "Price: n/a",
        f"Rating: {entry.rating}/5",
        f"Audience: {audience}",
    ]
    return "\n".join(bits)


def _cache_key(entry: Entry, audience: str) -> str:
    raw = f"{entry.id}:{entry.title}:{entry.updated_at}:{audience}"
    return hashlib.sha256(raw.encode()).hexdigest()


def heuristic_insight(entry: Entry, audience: str) -> str:
    """Deterministic fallback - always available, no network, no key."""
    price = f"at {entry.price:.0f} {entry.currency}" if entry.price is not None else "with no listed price"
    quality = "highly rated" if entry.rating >= 4.5 else "solidly rated" if entry.rating >= 3.5 else "worth a look"
    tags = ", ".join(entry.tags[:3]) if entry.tags else entry.category
    return (
        f"{entry.title} is a {quality} {entry.category} option {price}, best suited to {audience}. "
        f"Standout themes: {tags}."
    )


async def _call_gemini(prompt: str) -> str | None:
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{settings.gemini_model}:generateContent"
    body: dict[str, Any] = {
        "system_instruction": {"parts": [{"text": SYSTEM_PROMPT}]},
        "contents": [{"role": "user", "parts": [{"text": prompt}]}],
        "generationConfig": {"temperature": 0.4, "maxOutputTokens": 160},
    }
    async with httpx.AsyncClient(timeout=settings.request_timeout_s) as client:
        resp = await client.post(url, params={"key": settings.gemini_api_key}, json=body)
        resp.raise_for_status()
        data = resp.json()
    candidates = data.get("candidates") or []
    if not candidates:
        return None
    parts = candidates[0].get("content", {}).get("parts") or []
    return "".join(p.get("text", "") for p in parts).strip() or None


async def _call_openai(prompt: str) -> str | None:
    body = {
        "model": settings.openai_model,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": prompt},
        ],
        "temperature": 0.4,
        "max_tokens": 160,
    }
    async with httpx.AsyncClient(timeout=settings.request_timeout_s) as client:
        resp = await client.post(
            "https://api.openai.com/v1/chat/completions",
            headers={"Authorization": f"Bearer {settings.openai_api_key}"},
            json=body,
        )
        resp.raise_for_status()
        data = resp.json()
    return (data["choices"][0]["message"]["content"] or "").strip() or None


async def generate_insight(entry: Entry, audience: str = "first-time buyer") -> InsightResponse:
    key = _cache_key(entry, audience)
    if key in _cache:
        cached = _cache[key]
        return cached.model_copy(update={"cached": True})

    prompt = _prompt(entry, audience)
    providers: list[tuple[str, Any]] = []
    if settings.gemini_api_key:
        providers.append(("gemini", _call_gemini))
    if settings.openai_api_key:
        providers.append(("openai", _call_openai))

    for name, fn in providers:
        try:
            text = await fn(prompt)
            if text:
                result = InsightResponse(entry_id=entry.id, insight=text, provider=name)  # type: ignore[arg-type]
                _cache[key] = result
                return result
        except Exception as exc:
            log.warning("ai_provider_failed", provider=name, error=str(exc))

    result = InsightResponse(entry_id=entry.id, insight=heuristic_insight(entry, audience), provider="heuristic")
    _cache[key] = result
    return result

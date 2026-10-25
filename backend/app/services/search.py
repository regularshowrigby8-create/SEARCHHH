"""Hybrid search engine: BM25 (lexical) + RapidFuzz (fuzzy) + facets.

Why hand-rolled BM25 on top of RapidFuzz instead of pulling in
Elasticsearch/Meilisearch? Because the hackathon demo has to run with zero
infrastructure, offline, on a laptop - and because the ranking maths is the
part you want to be able to explain to judges.

Pipeline
--------
1. Normalise  : NFKD -> strip accents -> casefold -> tokenise -> drop stopwords
2. Expand     : domain synonym map (configurable, see SYNONYMS)
3. Score      : BM25 over weighted fields + fuzzy bonus + exact-phrase bonus
4. Filter     : category / price facets, then sort (relevance | rating | price)
5. Explain    : every hit carries `score` + `highlights` so the UI can show *why*
"""

from __future__ import annotations

import math
import re
import time
import unicodedata
from collections import Counter
from collections.abc import Iterable, Sequence
from dataclasses import dataclass, field

from rapidfuzz import fuzz

from app.config import settings
from app.models.schemas import Entry, ScoredEntry, SearchRequest, SearchResponse

TOKEN_RE = re.compile(r"[a-z0-9]+(?:['-][a-z0-9]+)*")
STOPWORDS = frozenset(
    [
        "a",
        "an",
        "and",
        "are",
        "as",
        "at",
        "be",
        "by",
        "for",
        "from",
        "has",
        "have",
        "in",
        "into",
        "is",
        "it",
        "its",
        "of",
        "on",
        "or",
        "that",
        "the",
        "to",
        "was",
        "were",
        "will",
        "with",
        "you",
        "your",
        "me",
        "my",
        "we",
        "our",
        "i",
        "this",
        "these",
        "those",
    ]
)

# Field weights: a title hit means far more than a description hit.
FIELD_WEIGHTS = {"title": 3.0, "subtitle": 1.6, "tags": 2.2, "category": 1.4, "description": 1.0}

SYNONYMS: dict[str, tuple[str, ...]] = {
    # GradLink domain: two doors - employment, and building your own thing.
    "job": ("work", "career", "vacancy", "employment", "role"),
    "jobs": ("work", "career", "vacancies", "employment"),
    "internship": ("work experience", "placement", "graduate programme", "yes"),
    "experience": ("no experience", "work experience", "entry level", "placement"),
    "no": ("none", "zero"),
    "graduate": ("graduated", "qualification", "degree", "diploma", "student"),
    "unemployed": ("jobless", "no experience", "seeking", "first job"),
    "funding": ("grant", "finance", "capital", "loan", "money", "investment"),
    "grant": ("funding", "nyda", "non-repayable", "capital"),
    "money": ("funding", "capital", "grant", "finance", "cash"),
    "loan": ("sefa", "funding", "credit", "finance"),
    "startup": ("business", "venture", "entrepreneurship", "founder", "create"),
    "business": ("startup", "enterprise", "company", "entrepreneur", "trade"),
    "entrepreneur": ("founder", "startup", "business", "self-employed"),
    "mentor": ("mentorship", "advisor", "coach", "guidance", "advice"),
    "cofounder": ("co-founder", "partner", "team", "matching", "collaborate"),
    "partner": ("co-founder", "cofounder", "team", "collaborator"),
    "cv": ("resume", "curriculum vitae", "profile", "application"),
    "resume": ("cv", "curriculum vitae"),
    "interview": ("practice", "questions", "mock", "prepare"),
    "apply": ("application", "applying", "vacancy", "deadline"),
    "skill": ("upskilling", "course", "training", "learnership", "certificate"),
    "course": ("training", "bootcamp", "learnership", "certificate", "upskilling"),
    "free": ("no cost", "zero fee", "stipend", "funded"),
}

BM25_K1 = 1.5
BM25_B = 0.75


def normalise(text: str) -> str:
    decomposed = unicodedata.normalize("NFKD", text or "")
    ascii_text = "".join(ch for ch in decomposed if not unicodedata.combining(ch))
    return ascii_text.casefold()


def tokenize(text: str, *, keep_stopwords: bool = False) -> list[str]:
    tokens = TOKEN_RE.findall(normalise(text))
    if keep_stopwords:
        return tokens
    return [t for t in tokens if t not in STOPWORDS and len(t) > 1]


# What the user typed must dominate what we guessed. Literal matches are scored
# on their own; synonym matches are scored separately and discounted, otherwise
# a doc tagged "yes programme" outranks a title that literally says "internship".
SYNONYM_SCORE_WEIGHT = 0.25


def expand_weighted(tokens: Sequence[str]) -> dict[str, float]:
    """Typed terms at weight 1.0, their synonyms at a small discount weight.

    The two groups are scored separately in `search()`, which keeps recall
    (typos, vocabulary mismatch) without letting guesses beat literal hits.
    """
    weights: dict[str, float] = dict.fromkeys(tokens, 1.0)
    for token in tokens:
        for synonym in SYNONYMS.get(token, ()):
            for part in synonym.split():
                if part not in weights:
                    weights[part] = 1.0
    return weights


def expand(tokens: Sequence[str]) -> list[str]:
    """Flat term list (kept for callers that only need the vocabulary)."""
    return list(expand_weighted(tokens).keys())


@dataclass(slots=True)
class IndexedDoc:
    entry: Entry
    field_tokens: dict[str, list[str]] = field(default_factory=dict)
    length: int = 0
    bag: Counter[str] = field(default_factory=Counter)
    haystack: str = ""


@dataclass(slots=True)
class SearchEngine:
    """In-memory index over `Entry` documents."""

    entries: Sequence[Entry]
    docs: list[IndexedDoc] = field(default_factory=list)
    df: Counter[str] = field(default_factory=Counter)
    avg_len: float = 1.0

    def __post_init__(self) -> None:
        self.build()

    # ------------------------------------------------------------------ index
    def build(self) -> None:
        self.docs = []
        self.df = Counter()
        for entry in self.entries:
            field_tokens = {
                "title": tokenize(entry.title, keep_stopwords=True),
                "subtitle": tokenize(entry.subtitle),
                "description": tokenize(entry.description),
                "tags": tokenize(" ".join(entry.tags)),
                "category": tokenize(entry.category),
            }
            bag: Counter[str] = Counter()
            total_weighted_len = 0.0
            for name, tokens in field_tokens.items():
                bag.update(tokens)
                total_weighted_len += len(tokens) * FIELD_WEIGHTS[name]
            for term in bag:
                self.df[term] += 1
            haystack = normalise(
                " ".join([entry.title, entry.subtitle, entry.description, " ".join(entry.tags), entry.category])
            )
            self.docs.append(
                IndexedDoc(
                    entry=entry,
                    field_tokens=field_tokens,
                    length=max(1, int(total_weighted_len)),
                    bag=bag,
                    haystack=haystack,
                )
            )
        total_len = sum(d.length for d in self.docs) or 1
        self.avg_len = total_len / max(1, len(self.docs))

    # ------------------------------------------------------------------- idf
    def idf(self, term: str) -> float:
        n = len(self.docs)
        df = self.df.get(term, 0)
        return math.log(1 + (n - df + 0.5) / (df + 0.5))

    # ----------------------------------------------------------------- score
    def _bm25(self, doc: IndexedDoc, query_terms: Sequence[str]) -> float:
        score = 0.0
        for term in query_terms:
            tf = doc.bag.get(term, 0)
            if not tf:
                continue
            # field-weighted term frequency
            weighted_tf = sum(doc.field_tokens[name].count(term) * weight for name, weight in FIELD_WEIGHTS.items())
            denom = weighted_tf + BM25_K1 * (1 - BM25_B + BM25_B * doc.length / self.avg_len)
            score += self.idf(term) * (weighted_tf * (BM25_K1 + 1)) / (denom or 1)
        return score

    def _fuzzy(self, doc: IndexedDoc, raw_query: str) -> float:
        if not raw_query:
            return 0.0
        best = max(
            fuzz.token_set_ratio(raw_query, normalise(doc.entry.title)),
            fuzz.partial_ratio(raw_query, normalise(" ".join(doc.entry.tags))),
            fuzz.token_set_ratio(raw_query, doc.haystack[:400]),
        )
        return 0.0 if best < settings.fuzzy_threshold else (best / 100.0) * 2.5

    def _phrase_bonus(self, doc: IndexedDoc, raw_query: str) -> float:
        q = normalise(raw_query).strip()
        if len(q) < 3:
            return 0.0
        bonus = 0.0
        if q in normalise(doc.entry.title):
            bonus += 2.0
        if any(q == normalise(tag) for tag in doc.entry.tags):
            bonus += 1.0
        if doc.entry.category and q == normalise(doc.entry.category):
            bonus += 0.8
        return bonus

    def _highlights(self, doc: IndexedDoc, terms: Iterable[str]) -> list[str]:
        term_set = set(terms)
        out: list[str] = []
        for text in (doc.entry.title, doc.entry.subtitle, doc.entry.description):
            if not text:
                continue
            if term_set & set(tokenize(text)):
                snippet = text if len(text) <= 140 else text[:137].rsplit(" ", 1)[0] + "..."
                if snippet not in out:
                    out.append(snippet)
            if len(out) >= 3:
                break
        return out

    # ---------------------------------------------------------------- search
    def search(self, request: SearchRequest) -> SearchResponse:
        started = time.perf_counter()
        raw_q = (request.q or "").strip()
        typed_terms = tokenize(raw_q)
        synonyms = {term for term in expand_weighted(typed_terms) if term not in typed_terms}

        scored: list[ScoredEntry] = []
        for doc in self.docs:
            entry = doc.entry
            if request.category and normalise(entry.category) != normalise(request.category):
                continue

            if typed_terms:
                literal = self._bm25(doc, dict.fromkeys(typed_terms, 1.0))
                # Average the synonym group instead of summing it: six rare
                # synonyms at full weight would otherwise outrank the word the
                # user actually typed.
                guessed = self._bm25(doc, dict.fromkeys(synonyms, 1.0)) * SYNONYM_SCORE_WEIGHT / max(1, len(synonyms))
                fuzzy = self._fuzzy(doc, raw_q)
                bonus = self._phrase_bonus(doc, raw_q)
                score = literal + guessed + fuzzy + bonus
                if score <= 0:
                    continue
            else:
                # Empty query = "discover" feed, ranked by quality signals.
                score = entry.rating / 5.0

            scored.append(
                ScoredEntry(
                    **entry.model_dump(),
                    score=round(score, 4),
                    highlights=self._highlights(doc, typed_terms),
                )
            )

        scored.sort(key=self._sort_key(request.sort))
        total = len(scored)
        window = scored[request.offset : request.offset + request.limit]
        took_ms = (time.perf_counter() - started) * 1000

        return SearchResponse(
            query=raw_q,
            total=total,
            took_ms=round(took_ms, 2),
            source="memory",
            results=window,
            suggestions=self.suggest(raw_q, exclude=raw_q),
        )

    @staticmethod
    def _sort_key(sort: str):
        if sort == "rating":
            return lambda e: (-e.rating, -e.score)
        if sort == "price_asc":
            return lambda e: (e.price if e.price is not None else float("inf"), -e.score)
        if sort == "price_desc":
            return lambda e: (-(e.price or 0.0), -e.score)
        if sort == "recent":
            return lambda e: (-(e.updated_at.timestamp() if e.updated_at else 0.0), -e.score)
        return lambda e: -e.score

    # ------------------------------------------------------------ suggestions
    def suggest(self, prefix: str, *, exclude: str = "", limit: int = 5) -> list[str]:
        needle = normalise(prefix).strip()
        if len(needle) < 2:
            return []
        pool: set[str] = set()
        for doc in self.docs:
            pool.add(doc.entry.title)
            pool.update(doc.entry.tags)
            pool.add(doc.entry.category)
        pool.discard(exclude)

        starts = [p for p in pool if normalise(p).startswith(needle)]
        if len(starts) < limit:
            starts += [
                p
                for p in pool
                if normalise(p) not in {normalise(s) for s in starts} and fuzz.partial_ratio(needle, normalise(p)) >= 78
            ]
        return sorted(dict.fromkeys(starts), key=lambda s: (len(s), s))[:limit]

    def categories(self) -> list[tuple[str, int]]:
        counts = Counter(doc.entry.category for doc in self.docs)
        return sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))

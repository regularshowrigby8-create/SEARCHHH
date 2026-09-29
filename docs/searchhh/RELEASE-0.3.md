# Searchhh 0.3 — global portal / external review slice

Implementation date: 2026-09-29. Build and test results must be taken from the
associated GitHub Actions run, not inferred from this document.

## Implemented scope

- Android now selects from **110 global opportunity portal/publisher seed URLs**,
  rather than the 128 generic metasearch adapters. These are crawl seeds, NOT 110
  individually implemented site-search APIs or 110 proven live sources. Programme
  eligibility remains country/institution-specific; no South Africa location filter.
- Jsoup extracts RSS/Atom items and public HTML links. All meaningful query words
  must match, and quoted phrases must match literally. Navigation/footer forms are
  excluded. Optional following of one matching announcement per source per pass
  extracts Google/Microsoft Forms and other application links, with source evidence.
- Public-only DNS/URL policy, robots rules, request pacing, response size limits,
  rate-limit cooldowns and coroutine-linked OkHttp cancellation. No form submission,
  private pages, authentication bypass, or limit-evading host/key rotation.
- Searches repeat until Stop/lifecycle/resource failure, rather than automatically
  stopping at 1,000 links. A **rolling 1,000 recently checked result view** bounds
  memory/storage per session; save/export important links. Saved records are separate.
- Optional independent AI reviewer (four excerpts/request, approximately one request
  per three minutes) uses externally hosted APIs only. It cannot browse, call MCP
  tools, access profiles/cookies or generate crawler URLs. Search continues if it fails.
- OpenRouter model choices come from the public models endpoint: only `:free` text
  entries with all declared prices zero; paid fallback disabled and prompt/completion
  maximum prices set to zero. Refresh on connection and when review needs a stale
  catalog (15-minute freshness). Not arbitrary-web continuous model discovery.
- Z.AI allowlist: `glm-4.7-flash`, `glm-4.5-flash`, `glm-4.6v-flash`, listed Free in
  provider pricing on the research date. No FlashX or paid web-search tools.
- Provider-specific consent, official dashboard links, masked key import and existing
  AndroidX Security/Keystore encrypted preferences. Remove/revoke instructions.
  Two connected providers alternate review batches. One selected model per provider.
- Schema/evidence-ID/quote validation before persisting AI relevance, summary and
  deadline/eligibility excerpts. AI opinions are labelled, not certified applications.

## Honest limitations

- **No claim of 100 working hosted models, unlimited free inference or permanent
  pricing.** The user explicitly allowed free plans based on published code/docs.
  Accounts/keys/quotas/terms remain provider-specific. No keys were supplied or live
  inference calls made by the agent. Published free pricing is not a forever promise.
- 110 configured portals have NOT all been live tested. Login, redirects, JS-only,
  PDF-only, moved pages, robots denial and challenges are reported/not bypassed.
  General extraction reads bounded listings, not complete native site-search indexes.
- Periodic polling is not a guaranteed real-time or exhaustive-web service.
  Public-social connectors and multi-hop exhaustive discovery are not implemented.
- A discovered form is not automatically official, open, or appropriate for the
  user. Evidence links enable inspection. This slice does not infer a verified
  open/closed state from potentially ambiguous deadline text.
- No unattended signup/API-key retrieval, no OAuth flow yet, no locally hosted model,
  no paid fallback. Browser onboarding/profile reuse and model discovery remain later
  stages. No third-party signup tool has been silently installed.
- AI-generated summaries can still be wrong despite quote validation; confirm with
  the official source. Free-plan capacity is not appropriate for reviewing every
  single crawled result instantly. Android can stop the foreground process.
- Optional external Python/SearXNG backend remains on its legacy 128-adapter catalog;
  the Android included backend is the path updated by this slice.
- Full Material 3/theme overhaul and other previously planned UI stages are not
  included. This builds on the existing Compose UI and EinkBro results browser.

## Reuse and provenance

Existing project libraries do the implementation work: OkHttp 4.12 cancellable IO,
Jsoup 1.17 extraction, crawler-commons robots parser, Room 2.x persistence, AndroidX
Security encrypted preferences, Compose and the imported GPL EinkBro browser.
New files are integration/policy glue, not a new inference or browser framework.

Published developer/provider references used:
- https://github.com/mnfst/awesome-free-llm-apis — free API directory lead; not
  incorporated wholesale or treated as evidence of 100 distinct usable models.
- https://openrouter.ai/docs/guides/routing/model-variants/free
- https://openrouter.ai/api/v1/models
- https://docs.z.ai/guides/overview/pricing
- https://docs.z.ai/guides/overview/quick-start

## Validation plan

Existing sequential GitHub Actions gates: Python and browser framework checks →
legacy backend stack integration → Android unit/lint/build → Android 35 instrumented
checks → prerelease APK. Added tests cover global catalog integrity, query/phrase
matching, RSS and application provenance, Microsoft Forms host, zero-price filtering,
review schema/quotes/unknown IDs, HTTP cancellation/size caps and vault consent/removal.
Live portal/relay diagnostics are non-gating and must be reported independently from
fixture/device security tests. Provider inference requires a user-authorized key and
is not covered by this build's no-secret fixtures.

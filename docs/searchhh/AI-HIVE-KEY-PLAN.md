# AI hive and API-key handler — next implementation stages

Status: proposed implementation sequence following the 2026-09-29 model audit.
This document is a plan, not a claim that these features are in the released APK.
See [AI-MODEL-AUDIT.md](AI-MODEL-AUDIT.md) for pricing, availability and exclusions.

## Architecture to preserve

```
Public crawlers → bounded evidence queue → reviewer A + reviewer B
                                         ↓
                        local schema/evidence checks → agreement/disagreement
                                         ↓
                           ranked results with original application links
```

- The phone runs the vault, gateway, queue and orchestration, **not AI inference**.
- Crawlers operate independently until Stop. Models cannot crawl, follow links,
  invoke tools, submit forms, accept terms or acquire keys.
- Reuse Kotlin coroutines, OkHttp, Room, AndroidX Security and existing parsers.
  Do not add an agent framework just to inflate the agent/tool count.
- Different providers serving the same weights are alternative routes, not
  independent model families or independent corroborating evidence.

## Stage 1 — registry and key lifecycle

The audit JSON is research input, **not an executable endpoint allowlist**.
Separate these facts rather than reducing them to an `isFree` Boolean:

- provider ID, credential realm, fixed HTTPS API origin and protocol;
- exact model ID, family, capabilities, preview/retirement status;
- pricing basis: zero-token-price, shared free allowance, recurring credits,
  trial, paid, consumer-only, or unresolved;
- official evidence URL, date, verification level and expiration/recheck time;
- account-specific entitlement, live catalog availability, quota scope;
- adapter implementation and last successful authorized inference test.

Keys must be linked to provider **and account/project**. Z.AI international and
BigModel China are separate realms even when model names match. Cloudflare also
needs an account ID and a scoped Workers AI token. Never infer a provider from
key shape or try a secret against multiple hosts.

Reuse the encrypted vault. Keep secrets out of model prompts, Room evidence,
MCP results, exports, HTTP logs, crash logs, analytics and UI saved state.
Store only non-secret key labels/expiry/status in ordinary metadata. Exclude
credential storage from Android backups; test this on the target Android SDK.

Connection flow:

1. Explain provider, destination, data sent, retention/training terms, and quota.
2. Open the official external browser authorization/key page. Reuse one-time
   profile details only with provider-specific permission.
3. Prefer a documented authorization flow where available. OpenRouter PKCE needs
   state validation, verifier storage, callback matching and one-use redemption.
   Otherwise use explicit key import; do not pretend signup or key minting is
   automated. No CAPTCHA bypass, silent terms acceptance or exposed-key scraping.
4. Validate locally, then call only that provider's official catalog/key endpoint.
5. Display ready, invalid, quota-exhausted, expired, blocked-plan or needs-consent.
6. Disconnect cancels requests and deletes local credentials. Explain that local
   deletion does **not** revoke the provider-side key; offer its revocation page.

Changing settings must not erase a provider/account quota cooldown. Track expiry
and legitimate credential replacement separately from quota accounting.

## Stage 2 — provider adapters, sequentially

1. Harden the existing OpenRouter and international Z.AI adapters. OpenRouter
   selection requires a current actual `:free` entry with explicit zero prices,
   text output and a currently usable free route; retain `max_price = 0` and no
   paid fallback. Empty endpoint discovery is not an inference success and must
   not be displayed as ready. Do not apply China's GLM retirement automatically
   to the international endpoint.
2. Add Google Gemini/Gemma and current Groq text models using official request
   examples and existing HTTP libraries. Query catalogs; do not import retired
   Groq Llama/Gemma IDs from the supplied list.
3. Add Cloudflare with account-scoped authorization, model-specific eligibility
   and its native response format. Do not assume every model uses identical
   OpenAI response JSON. Its 10,000-neuron allowance is shared, not per model.
4. Offer Mistral's recurring monthly credits as a visibly separate optional
   category, PAYG disabled. Do not describe it as unlimited/credit-free.
5. Keep SiliconFlow candidates, Aion pricing ambiguities, and other unresolved
   rows disabled pending realm/catalog/plan checks. Trials, paid-only services and
   unofficial consumer-chat endpoints are not the default free pool.

A free-priced model may still incur charges when called with a paid-tier account
on some providers. A device-side request counter cannot guarantee a zero bill.
Require a free-plan account/no automatic top-up, use provider-side hard stops
where supported, and show whether billing protection is provider-enforced or
merely user-confirmed. Unknown account entitlement must not silently become
'guaranteed free'. Optional paid operation is outside this stage.

## Stage 3 — real multi-model review

- One reviewer extracts relevance, eligibility, deadline and status from supplied
  evidence; a second independently reviews the **same evidence**, without seeing
  the first answer. Use different underlying models where available.
- Aggregate with deterministic Kotlin code. Keep both verdicts and quotes; mark
  disagreements, missing evidence and uncertain dates rather than manufacture
  certainty by majority vote. Two model opinions are not two independent sources.
- Preserve unreviewed results when only one model/account is available. Display
  'single review' rather than claiming consensus. AI cannot invent application
  URLs or change authoritative crawler evidence.
- Cap per-batch reviewers, input/output size, concurrent calls and attempts.
  Persist a fair queue with per-item retry state so four malformed early records
  cannot starve all later records. Quarantine permanent/schema failures.
- Share rate budgets by account/project, not key. Honor request and token limits,
  numeric and HTTP-date Retry-After, provider reset headers, and circuit breakers.
  Count dispatched/ambiguous failures conservatively. Never rotate keys/accounts
  to bypass quotas or multiply the same shared allowance by model count.
- Consent changes, evidence changes, key removal and Stop invalidate in-flight
  work before both dispatch and persistence. Catalog refresh may run periodically
  until Stop; discovery never means automatic signup or activation.

## Acceptance gates before a new APK

1. Registry tests: numbering/provenance; pricing categories; retired/empty-route
   exclusions; duplicate family vs endpoint counts; modalities; stale evidence.
2. Mock HTTP tests: exact key-host binding, redirects blocked, secret redaction,
   invalid keys, quota and budget stops, 429/reset parsing, catalog changes,
   unsupported JSON, bounded bodies, cancellation and consent races.
3. Queue/consensus tests: no starvation, independent answers, disagreement,
   missing reviewer, duplicate families, stale evidence and zero crawler tools.
4. Android vault instrumentation: encryption, backup exclusion, expiry, remove,
   restart persistence and no secrets in MCP/export/logs.
5. Android unit tests → lint → APK build → device/instrumentation tests.
6. Authorized provider smoke tests using user-owned keys, minimal non-sensitive
   evidence and explicit consent. Report untested providers honestly; never put
   secrets in the repo/Actions output or claim doc checks are inference checks.
7. Only then publish a new APK and record the exact source, CI run and limits.

## What currently exists

The released 0.3 app has encrypted provider-bound key import, consent/revision
checks, OpenRouter zero-price catalog filtering, three international Z.AI Flash
IDs, cancellation and a single-model reviewer per batch. It does **not** yet have
multi-model consensus, automatic provider signup, this expanded provider roster,
or a durable account-level quota ledger. This audit changes none of those claims.

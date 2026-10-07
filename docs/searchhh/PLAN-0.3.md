# Searchhh 0.3 — staged implementation plan

Research date: 2026-09-29. The released 0.2 APK remains the baseline.
## Approved scope update — 2026-09-29

The user has now authorized implementation using published code/docs stating a
free API plan, without a permanent-free certification gate. This supersedes the
older pricing blocker below. Open weights alone still do not supply external
inference. Global portals replace the South Africa-oriented research examples.

First implementation slice: curated global portal catalog; robots-aware public
listing/application extraction; quoted-query matching; explicit source evidence;
encrypted provider keys; consent-gated OpenRouter free variants and documented
Z.AI free Flash models; bounded, review-only AI with no paid fallback. Reuse
OkHttp, Jsoup, Retrofit, AndroidX Security and the existing Room/service/UI stack.
No inference hosting, signup bypass, or keys supplied by this repository.

Acceptance: fixtures for query relevance, forms, provenance, output validation,
free-model filtering and cancellation; Android unit/lint/build then device tests.
Configured sources/models are not claimed as simultaneously live. Automatic signup,
100 distinct hosted models, public-social connectors, durable unbounded history,
and the full Material 3 redesign remain separate stages, not implied completion.

The remainder records the original plan. Its permanent-free exclusion is historical
and must not block the newly authorized free-plan integrations.

## 1. Requirements and boundaries

- The included Android server is a **credential vault, API gateway, scheduler and
  results store**. Model inference happens at external providers. No model hosting,
  model downloads, GPU service, or user-operated inference deployment is planned.
- Search engines and crawler workers discover relevant public web/social material.
  AI agents only review the collected evidence against the user's request.
- Search continues until Stop, subject to provider limits, Android lifecycle,
  connectivity and available storage. AI unavailability must not silently stop search.
- Target at least 100 genuinely relevant search adapters and 100 usable, distinct
  externally hosted language models. Neither count may be met with duplicates,
  irrelevant connectors, unavailable endpoints or downloadable weights alone.
- The user's latest pricing requirement excludes trials, credits, paid services and
  free tiers: **permanently free external inference**. This is an unresolved
  feasibility requirement, not permission to substitute one of those categories.
- Signup, authorization, provider terms and data sharing remain provider-specific.
  Automate only supported operations on accounts the user owns/authorizes.

## 2. Verified research: tools and their limits

| Component | Existing tool / interface | Proposed use and adoption gate |
|---|---|---|
| Agent workflows | JetBrains Koog | Candidate for a bounded review graph and provider clients. Official docs list Android. Verify licensing and a version compatible with Searchhh's Kotlin/Compose/Ktor stack before adopting. No browser/search tools granted to reviewers. |
| Alternative API client | aallam/openai-kotlin | MIT Kotlin/coroutine API client; alternative for compatible endpoints if Koog's dependency or API footprint is unsuitable. Do not install both without a demonstrated need. |
| Standard OAuth/OIDC | AppAuth-Android + browser Custom Tabs | Existing native authorization implementation with PKCE. Use only where the provider supports the protocol and required client registration. OAuth does not inherently create provider API keys. |
| Automatic key installation example | OpenRouter's documented PKCE flow | After user authorization, exchange the code at `/api/v1/auth/keys` and securely install the returned user-controlled key. Its custom exchange needs a provider adapter; it is not a universal AppAuth token exchange. Pricing does not meet the current strict requirement. |
| Secret protection | Android Keystore plus existing audited encryption/storage libraries | Keep the encryption key in Keystore; encrypt provider-token records at rest. Metadata and jobs in Room. API keys are not ordinary Room text fields and must never be exposed through MCP. Audit existing encrypted-preference migration before changing storage. |
| Search/crawl | Existing SearXNG adapters, OkHttp, Jsoup, crawler-commons | Retain and curate the current integration; make collection explicitly opportunity/phrase-driven. |
| UI/browser | Material 3, AndroidX Compose animation, AndroidX WebKit, existing EinkBro | Refine rather than replace the browser with a new home-grown engine. Provider login uses a separate trusted browser context, not an inspected result WebView. |

### Options investigated but not a solution to the pricing/signup requirement

- OpenRouter documents 25+ free models, with free-plan limits. This does not establish
  100 permanently free external models. Its PKCE flow demonstrates legitimate
  automatic key installation, **not** compliance with the required pricing policy.
- Puter provides a keyless developer interface, but its pricing explicitly uses
  user allowances and user-paid overages. It is not permanently free inference for
  the user and is not an approved substitute here.
- Browser Use is an existing browser-automation tool. Its documented cloud path
  requires a Browser Use API key and browser infrastructure. It does not grant
  permission to provision other providers' accounts, eliminate their verification,
  or supply free inference. Do not add it as an assumed universal signup service.
- A model repository/license is not a hosted API endpoint. A smaller model is not
  automatically hosted for free either.

**Research conclusion:** the gateway, vault and review-agent architecture is feasible.
A verified roster of 100 models meeting the strict permanent-free external-API
requirement has not been found. No implementation or UI should claim that roster exists.
Do not quietly relax this requirement; report the evidence and request a scope decision
if provider research cannot satisfy it.

## 3. Proposed architecture

```text
User request / explicit phrase and opportunity filters
  -> continuous search scheduler
  -> relevant search adapters + permitted public-social connectors
  -> crawler-owned evidence extraction / robots / normalization / dedup
  -> durable evidence store + per-link review queue
  -> bounded review graph
       relevance reviewer
       opportunity / eligibility / date extractor
       evidence and contradiction checker
       deterministic merge + user-facing explanation
  -> ranked results + evidence detail panel + existing WebView browser

Provider authorization -> encrypted key vault -> provider-bound API gateway
Public model catalogs -> discovery queue -> eligibility checks -> user-approved registry
```

### Trust boundaries

- Review agents receive the query and bounded evidence records, not API keys,
  onboarding profiles, browser cookies, passwords, shell access or crawling tools.
- The gateway attaches the correct key only to that provider's approved HTTPS host.
  Disable credential-bearing redirects; never accept an endpoint from model output.
- Page text and model-catalog descriptions are untrusted input, not instructions.
- Model output must pass a schema and reference supplied evidence IDs/quotes.
  Agreement between models is not verification. Unknown deadlines remain unknown.
- Agent roles, distinct models, provider endpoints and API keys are separate counts.
  One provider key can serve several models; 100 models need not mean 100 signups.
- Existing remote MCP credentials remain separate from provider credentials.
  No public-tunnel endpoint exports raw provider keys or authorizes new providers.

## 4. Connection and signup experience

### Reusable profile

A local form records only relevant non-secret details, such as display name and an
optional email. Do not pre-collect identity documents, birth dates, postal addresses,
payment data or passwords merely because a future provider might ask for them.

Before each provider connection, show:

1. The verified provider and official destination.
2. The exact fields being shared, purpose, and retention/privacy links.
3. What credential/access will be created and how to revoke it.
4. Published cost/usage conditions; block providers that fail the selected policy.
5. A provider-specific confirmation, with cancel and deletion controls.

Profile reuse is not blanket permission to send personal information to every
provider found by a discovery job.

### Connection state machine

```text
Discovered -> Evidence reviewed -> Policy eligible -> User consent
  -> Provider authorization / supported signup
  -> Awaiting user verification when required
  -> Code exchange / authorized key import
  -> Encrypted storage -> Safe connection test -> Ready

Alternative states: Unsupported automation / Declined / Expired /
Revoked / Unavailable / Pricing changed / Requires user action
```

Use official APIs or authorization flows for automated installation. Provider login,
email verification, CAPTCHA, 2FA, billing and contractual acceptance must not be
silently completed, bypassed or impersonated. Where no supported key-creation flow
exists, show the limitation rather than promise automatic retrieval.

### Optional onboarding assistant

An AI can explain fields after a suitable model connection exists. It must not
receive credentials or sensitive signup inputs. Initial connection must also work
with a deterministic form: requiring an unverified keyless AI to obtain the first
AI key creates an unnecessary dependency. No compliant permanent-free keyless
bootstrap provider has been verified in this research.

## 5. Relevant 100+ source plan

Audit the current catalog rather than retain unrelated icons, packages, music or
entertainment adapters just to preserve its count.

Every discovery connector must declare:

- Actual upstream engine/endpoint and provenance/license.
- Supported intents: opportunity, fellowship/cohort, scholarship, certification,
  public social announcement, job/internship, or explicit phrase search.
- Query templates and supported language/location/date filters.
- Public-only access boundaries, robots/rate limits and key requirements.
- Current availability and latest successful integration check.

Do not count query templates, social `site:` filters or several configurations of
one engine as separate engines. Sources appropriate only for background reference
must be labelled separately from opportunity-discovery connectors. Private social
feeds and login-walled groups are out of scope without a separately authorized API.

Search workers, not reviewers, fetch any page content needed for review. When only
an index snippet is available, the result must say `snippet-only`, not `page reviewed`.

## 6. AI review and model discovery

### Review scheduling

Start with two to four bounded review workers, not 100 simultaneous API calls.
Batch links, persist queued/running/completed/error states, apply provider backoff,
and keep unreviewed results visible. Optional second-model checks can inspect
ambiguous results; they must not cause endless model-to-model loops.

Each result stores model/version, evidence version/hash, review time, relevance,
extracted opportunity fields, supporting quotes and disagreements. Claiming that
all links were reviewed requires completion records; never infer that from a count.

### Continuous public model discovery

With its own visible control, periodically query approved public provider catalogs
and publisher/developer repositories. Deduplicate canonical model identities and
record hosted endpoint, authentication method, pricing evidence, license, limits,
regional eligibility, terms, last check and actual connectivity status.

States: discovered / awaiting review / eligible / connected / unavailable / retired.
Discovery does not automatically accept terms, create accounts, share the profile,
install executable code, activate an unknown endpoint or collect exposed credentials.
No traffic sniffing, credential hunting or scanning repositories for leaked keys.

Any claim of zero cost requires verified provider terms and fail-closed routing.
No paid fallback, plugins, top-up, billing enablement or credit-card requirement is
introduced without a separate scope decision and explicit authorization. A current
zero price must not be described as a permanent guarantee.

### Stop and resource behavior

One master Stop cancels search, crawler work, review dispatch and active model
catalog discovery; individual pause controls may also be provided. Cancellation
blocks late result commits. It cannot undo an external request already processed
or an account/key already created at the provider.

Replace the current artificial result-cap shutdown with paged storage and a durable
review backlog. Retention must be visible and configurable; no silent dropping or
marking unreviewed links as complete. Handle actual storage exhaustion explicitly.

## 7. Material 3 and result-browser plan

- Material 3 color schemes, typography, shapes, tonal surfaces and accessible focus.
- Light, dark, AMOLED, high-contrast and monochrome modes; blue, purple, emerald and
  amber accents; optional wallpaper dynamic color and non-color status indicators.
- Native short transitions for changed/new results, filters, theme changes, saved
  state and navigation. Stable list keys and reduced-motion handling.
- Stop and security warnings respond immediately, regardless of animations.
- Result detail sheet/pane with evidence, exact-phrase highlighting, review rationale,
  source dates, uncertainty, save/share actions and a clear original-page link.
- Existing browser/WebView with visible origin, progress, back/forward/reload,
  find-in-page and recoverable errors. Never put key-vault credentials in its JS bridge.
- Keep provider authorization in Custom Tabs / an external browser context.
- Map the requested toolbox to actual needs. Lottie, Rive, MotionLayout, particles,
  sound and community helpers are optional, not a checklist to add indiscriminately.
  Assess maintained native navigation rather than automatically adding old helpers.

## 8. Sequential implementation gates — only after plan review

| Phase | Deliverable | Required acceptance evidence |
|---|---|---|
| 0. Feasibility | Provider/model inventory and library compatibility/license audit | Count eligible distinct hosted models honestly; resolve strict permanent-free requirement before promising AI availability. |
| 1. Search focus | Curated source registry, query policies, crawl/review separation | Relevant fixtures per connector class; real loaded-source checks; robots/private-network tests; continuous/Stop behavior. |
| 2. Vault and consent | Encrypted provider records, reusable profile, consent ledger | Key redaction, host binding, backup/rotation/deletion, no MCP/WebView export, no sharing without provider-specific consent. |
| 3. One provider connection | One supported authorization + key-install flow | PKCE/state/expiry/replay tests; user cancellation; real account test only with explicit user authorization and policy-eligible provider. |
| 4. Review hive | Multi-role review graph and durable evidence queue | No reviewer browsing/network tools; schema and injection tests; disagreement/unknown handling; slow-provider/backlog/Stop tests. |
| 5. Registry expansion | Continuous public model discovery and connector expansion | Real endpoints, distinct-model counts, transparent unavailable states, no automatic signup or spending. |
| 6. UX | Material 3, result detail/browser integration, targeted animation | Accessibility/reduced motion, URL/security states, stable lists, low-end profiling and browser regressions. |
| 7. Release | Versioned APK and exact-source validation report | Existing sequential CI plus new vault/authorization/hive/browser tests; emulator and physical-device testing; observed limits disclosed. |

## 9. Decisions that are not being silently made

1. No on-device model runtime or model hosting: rejected by the user.
2. No free-tier/credit-based provider substituted for permanent-free inference.
3. No claim that profile reuse enables unattended signup at every provider.
4. No claim of 100 eligible hosted models until a verified roster exists.
5. No account provisioning or key collection during this planning stage.

## Primary sources inspected

- Koog Android/platform and agent documentation: https://docs.koog.ai/
- API client alternative: https://github.com/aallam/openai-kotlin
- AppAuth-Android / native OAuth / Custom Tabs / PKCE: https://github.com/openid/AppAuth-Android
- OpenRouter authorized key installation: https://openrouter.ai/docs/guides/overview/auth/oauth
- OpenRouter published pricing: https://openrouter.ai/pricing
- OpenRouter free-model and account limits: https://openrouter.ai/docs/api_reference/limits
- Puter user-pays pricing: https://developer.puter.com/pricing/
- Browser Use requirements: https://docs.browser-use.com/cloud/quickstart
- Android Keystore: https://developer.android.com/privacy-and-security/keystore

# Sequential criticism and validation

## 1. Reset and provenance — passed

Removed the earlier assistant-authored React/TypeScript prototype. Imported the
pinned EinkBro release with its source and notices. New Searchhh framework code is
explicitly separated from upstream, following the user's revised reuse-first scope.

## 2. Browser JavaScript — passed after harness correction

Initial upstream result: 5 suites failed; 9 failures, 4 passes. Inspection found the
upstream test bridge still used the old three-argument translation API, omitted
jsdom hit testing/visibility changes and did not disconnect observers on teardown.
Only test harness code was corrected; production browser JS was not changed.
Re-run: **5 suites, 13 tests passed**. No tests skipped and assertions retained.

Locale check: passed, no stale keys. Upstream translations have missing keys and
fall back to the default locale; this is not a claim of complete translations.

## 3. Framework unit/contract tests — passed locally

**24 tests passed** covering authentication, start/stop, duplicate merging,
33-source catalog consistency, private URL rejection, form ID preservation,
unknown/future dates, Scrapy fixture extraction and Stop during a source request.
API/worker unit tests use mocked SearXNG/Redis plus SQLite; they do **not** prove
real PostgreSQL/Redis/provider availability. That is a separate CI stage.

## 4. Real service integration — passed on initial CI run

Run `36497202034` passed this stage. The CI smoke test starts actual PostgreSQL, Redis, RQ and SearXNG. It checks loaded
engine configuration, executes a source pass and confirms Stop. A provider rate
limit is reported separately from infrastructure failure; zero hits is not proof
that a source is broken or that live opportunity quality is good.

## 5. Android compile, lint, unit tests — pending CI

Local sandbox has no JDK/Android SDK; direct SDK/package downloads failed. GitHub
Actions uses an Android-capable runner. A configured workflow is not a completed
build, and no APK download is claimed before an artifact exists.

## 6. Device check — pending CI

Emulator install/launch, UI presence, crash-buffer and screenshot checks are gated
after compilation. This is only a smoke test, not broad physical-device testing or
end-to-end opportunity verification on an Android phone.

## Remaining criticism / release limits

- AI mode, Go and OpenSearch are not implemented.
- Requires deployment; an APK is not a hosted server. HTTPS proxy setup is manual.
- Some engines can break or block a deployment. No bypass, IP rotation or CAPTCHA solving.
- Backend schemas currently use `create_all`; add migrations before changing a
  deployed schema. Add retention/pruning and operator recovery for abandoned jobs.
- Redis/RQ outages or a killed worker may leave a job marked running; Stop then
  restart the session. WorkManager refreshes state, it does not repair server jobs.
- Single-owner token, not multi-user isolation. No tenant/public-hosting claim.
- DNS filtering uses Scrapy's resolver at actual connection time; redirects are
  disabled for crawls. Deployment egress controls remain advisable.
- No live accuracy benchmark, deadline extraction, or confirmation of open cohorts.
- Debug signing is for testing; establish a private release signing key before updates.
- Preserve all GPL/AGPL notices and exact corresponding sources when distributing.

## Dependency review

An additional `pip-audit` check flagged the initially pinned older Scrapy,
Starlette and pytest versions. Updated FastAPI to 0.141.1, Starlette to 1.7.0,
Scrapy to 2.19.0 and pytest to 9.1.1. The resolver audit of the updated requirements
reported no known vulnerabilities; this is not a guarantee of security. The updated
stack must also pass CI. Framework tests were rerun (24 passed).

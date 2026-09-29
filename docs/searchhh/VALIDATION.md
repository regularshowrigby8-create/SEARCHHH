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

**25 tests passed** covering authentication, start/stop, duplicate merging,
33-source catalog consistency, private URL rejection, form ID preservation,
unknown/future dates, Scrapy fixture extraction and Stop during a source request.
API/worker unit tests use mocked SearXNG/Redis plus SQLite; they do **not** prove
real PostgreSQL/Redis/provider availability. That is a separate CI stage.

## 4. Real service integration — passed on final CI run

Final run `36499566556` passed this stage. The CI smoke test starts actual PostgreSQL, Redis, RQ and SearXNG. It checks loaded
engine configuration, executes a source pass and confirms Stop. A provider rate
limit is reported separately from infrastructure failure; zero hits is not proof
that a source is broken or that live opportunity quality is good.

## 5. Android compile, lint, unit tests — passed on final CI run 36499566556

Local sandbox has no JDK/Android SDK; direct SDK/package downloads failed. GitHub
Actions uses an Android-capable runner. Final run 36499566556 passed Android unit tests,
lint and universal debug APK assembly after the Room/Retrofit import correction.
Later changes must pass the same gates again before being published.

## 6. Device check — passed on final CI run 36499566556

Android 35 emulator installation, launch, UI-text presence, an empty crash buffer
and screenshot capture all passed after compilation. This is only a smoke test, not broad physical-device testing or
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
stack must also pass CI. Framework tests were rerun (25 passed).

## Android integration failure diagnosed

The first builds failed at Room KSP. Both Retrofit and Room define `Query`;
wildcard imports made the DAO annotation unresolved. The Retrofit imports were
made explicit. CI's diagnostic annotation was also shortened below GitHub's 4 KB
limit, and an explicit Bash `pipefail` prevents `tee` from hiding Gradle failures.
This is a framework integration bug, not an upstream browser failure. No failed
build was published as an APK.

## Published build

- Release: https://github.com/regularshowrigby8-create/SEARCHHH/releases/tag/v0.1.0-test-6
- Green pipeline: https://github.com/regularshowrigby8-create/SEARCHHH/actions/runs/36499566556
- Source commit: `f9fc2e45022cbf1d57c80810738caf4b4166b84b`
- APK: `searchhh-0.1.0-test.apk`; 23,797,051 bytes.
- SHA-256 reported by GitHub's release-asset API:
  `05b10b9ba50df162e8a9ec821ee15f2945d361aba0d28642d54e9e137045f156`
- Release asset API confirms `uploaded`; release is public and marked prerelease.
- Screenshot and checksum are attached to the release.

The first passing APK's release step failed after emulator validation. For the final
run, the exact source tag was prepared through authenticated GitHub CLI; the gated
workflow then successfully published the release and uploaded the APK. No build or
device test was disabled to obtain the download.

The sandbox could read release metadata but its network blocked downloading the
release binaries. Therefore the hash above is GitHub-reported, not an independently
recomputed local download hash. Installation/launch validation was performed on
the actual APK inside GitHub Actions before publication.

**Outstanding:** production signing, hosted deployment, broader device/interaction
testing, a live opportunity-quality benchmark, AI/BYOK mode, Go and OpenSearch.
Debug keys can differ between CI runs; in-place upgrades may require uninstalling
the earlier debug app (export saved data first). This build is not a production
release or an assertion that every provider is always available.

## 0.2 upgrade validation (in progress; historical results above describe 0.1)

- 128-entry Python/app/SearXNG catalogs synchronized; 25 Python and 13 JavaScript
  tests pass. Real PostgreSQL/Redis/RQ/SearXNG integration confirms all 128 IDs load.
- Android compile, unit tests, lint and both application/instrumentation APK assembly
  passed at `f3b4955` in run `36502783543`.
- The first Android compile caught two integration bugs (Ktor receiver shadowing the
  Android Context, and OkHttp's Kotlin Dns interface not being a SAM constructor).
  Both were fixed, not worked around by skipping compilation.
- The first instrumented run passed Room 1→2 migration and isolated-store identity
  checks, but rejected one test method because its inferred return type was not void.
  Corrected the harness to explicit Unit. MCP protocol/Stop assertions must now run;
  the earlier build is not called device-verified or released.
- Public search and relay probes are separate non-gating diagnostics. A successful
  diagnostic test execution alone does not establish provider availability; inspect
  its AVAILABLE/UNAVAILABLE result. Tests never substitute fixtures for a live probe.
- Follow-up fixes retain SearXNG date formats accurately, tolerate null directory
  fields, and serialize service readiness with Android lifecycle transitions.

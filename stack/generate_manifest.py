#!/usr/bin/env python3
"""Generates stack/dependencies.yml from curated tables.

The manifest is the auditable answer to "where are your 100+ tools?".
`scripts/scan_stack.py` then verifies every entry against the repo and the
installed environment, and renders STACK.md.

Usage:
    python stack/generate_manifest.py     # rewrite the manifest
    python scripts/scan_stack.py          # verify + render STACK.md
"""

from __future__ import annotations

from collections import Counter
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "stack" / "dependencies.yml"
TOML = "android/gradle/libs.versions.toml"


def item(name, kind, category, purpose, version="", license_="", repo="", coordinate="", declared_in="", **kw):
    entry = {"name": name, "kind": kind, "category": category, "purpose": purpose}
    if version:
        entry["version"] = version
    if coordinate:
        entry["coordinate"] = coordinate
    if license_:
        entry["license"] = license_
    if repo:
        entry["repo"] = repo
    if declared_in:
        entry["declared_in"] = declared_in
    entry.update(kw)
    return entry


items: list[dict] = []


def gradle(coord, purpose, ver="", lic="Apache-2.0", repo="https://github.com/androidx/androidx", cat="android-ui"):
    items.append(
        item(coord, "gradle", cat, purpose, version=ver, license_=lic, repo=repo,
             coordinate=coord, declared_in=TOML)
    )


# --------------------------------------------------------------------------
# Android platform
# --------------------------------------------------------------------------
gradle("androidx.core:core-ktx", "Backwards-compatible core APIs, KTX extensions", "1.13.1")
gradle("androidx.appcompat:appcompat", "AppCompat resources + theme base for the splash theme", "1.7.0")
gradle("androidx.activity:activity-compose", "setContent {}, edge-to-edge, activity result APIs", "1.9.3")
gradle("androidx.core:core-splashscreen", "Android 12+ splash screen API with backwards compatibility", "1.0.1")
gradle("androidx.profileinstaller:profileinstaller", "Baseline profile installation for faster cold start", "1.4.0")
gradle("androidx.security:security-crypto", "EncryptedSharedPreferences for tokens at rest", "1.1.0-alpha06")

# --------------------------------------------------------------------------
# Compose
# --------------------------------------------------------------------------
BOM = "2024.10.01"
gradle("androidx.compose:compose-bom", "Aligns every Compose artifact to one tested version set", BOM, cat="compose")
for coord, purpose in [
    ("androidx.compose.ui:ui", "Declarative UI runtime and layout primitives"),
    ("androidx.compose.ui:ui-graphics", "Canvas, brush and vector drawing"),
    ("androidx.compose.ui:ui-tooling", "Layout inspector + @Preview rendering (debug)"),
    ("androidx.compose.ui:ui-tooling-preview", "@Preview annotations"),
    ("androidx.compose.ui:ui-test-junit4", "Compose UI testing (instrumented)"),
    ("androidx.compose.ui:ui-test-manifest", "Test activity for Compose UI tests (debug)"),
    ("androidx.compose.material3:material3", "Material 3 components (cards, chips, nav bar, dialogs)"),
    ("androidx.compose.material3:material3-window-size-class", "Responsive breakpoints for tablets/foldables"),
    ("androidx.compose.material:material-icons-extended", "Extended icon set (bolt, cloud-off, bookmark)"),
    ("androidx.compose.foundation:foundation", "LazyColumn, LazyRow, clickable, FlowRow"),
    ("androidx.compose.runtime:runtime", "State, composition and snapshot system"),
]:
    gradle(coord, purpose, BOM, cat="compose")

# --------------------------------------------------------------------------
# Architecture: lifecycle + navigation
# --------------------------------------------------------------------------
for coord, ver, purpose in [
    ("androidx.lifecycle:lifecycle-runtime-ktx", "2.8.7", "Lifecycle-aware coroutine scopes"),
    ("androidx.lifecycle:lifecycle-runtime-compose", "2.8.7", "collectAsStateWithLifecycle()"),
    ("androidx.lifecycle:lifecycle-viewmodel-compose", "2.8.7", "viewModel() and viewModelFactory {} in Compose"),
    ("androidx.lifecycle:lifecycle-viewmodel-ktx", "2.8.7", "viewModelScope"),
    ("androidx.lifecycle:lifecycle-process", "2.8.7", "ProcessLifecycleOwner for app-level reactions"),
    ("androidx.navigation:navigation-compose", "2.8.4", "NavHost, typed route arguments, deep links"),
]:
    gradle(coord, purpose, ver, cat="architecture")

# --------------------------------------------------------------------------
# Persistence
# --------------------------------------------------------------------------
for coord, ver, purpose in [
    ("androidx.room:room-runtime", "2.6.1", "SQLite ORM: saved entries + recent queries"),
    ("androidx.room:room-ktx", "2.6.1", "Flow/suspend support for Room DAOs"),
    ("androidx.room:room-compiler", "2.6.1", "KSP annotation processor that generates DAO code"),
    ("androidx.datastore:datastore-preferences", "1.1.1", "Typed key-value settings without SharedPreferences"),
    ("androidx.work:work-runtime-ktx", "2.9.1", "Periodic background sync with constraints"),
]:
    gradle(coord, purpose, ver, cat="persistence")

# --------------------------------------------------------------------------
# Network, images, logging
# --------------------------------------------------------------------------
for coord, ver, lic, repo, purpose in [
    ("com.squareup.retrofit2:retrofit", "2.11.0", "Apache-2.0", "https://github.com/square/retrofit", "HTTP client with suspend functions"),
    ("com.squareup.retrofit2:converter-kotlinx-serialization", "2.11.0", "Apache-2.0", "https://github.com/square/retrofit", "JSON converter (no reflection)"),
    ("com.squareup.okhttp3:okhttp", "4.12.0", "Apache-2.0", "https://github.com/square/okhttp", "Connection pooling, timeouts, interceptors"),
    ("com.squareup.okhttp3:logging-interceptor", "4.12.0", "Apache-2.0", "https://github.com/square/okhttp", "Request/response logging in debug only"),
    ("com.squareup.okhttp3:mockwebserver", "4.12.0", "Apache-2.0", "https://github.com/square/okhttp", "Real HTTP server for unit tests"),
    ("org.jetbrains.kotlinx:kotlinx-serialization-json", "1.7.3", "Apache-2.0", "https://github.com/Kotlin/kotlinx.serialization", "Compile-time JSON codec"),
    ("org.jetbrains.kotlinx:kotlinx-coroutines-android", "1.9.0", "Apache-2.0", "https://github.com/Kotlin/kotlinx.coroutines", "Structured concurrency + Main dispatcher"),
    ("org.jetbrains.kotlinx:kotlinx-coroutines-test", "1.9.0", "Apache-2.0", "https://github.com/Kotlin/kotlinx.coroutines", "runTest, TestDispatcher, setMain"),
    ("org.jetbrains.kotlinx:kotlinx-datetime", "0.6.1", "Apache-2.0", "https://github.com/Kotlin/kotlinx-datetime", "Cross-platform date/time types"),
    ("io.coil-kt:coil-compose", "2.7.0", "Apache-2.0", "https://github.com/coil-kt/coil", "Async image loading in Compose"),
    ("com.jakewharton.timber:timber", "5.0.1", "Apache-2.0", "https://github.com/JakeWharton/timber", "Structured logging facade"),
]:
    gradle(coord, purpose, ver, lic, repo, cat="network")

# --------------------------------------------------------------------------
# Firebase
# --------------------------------------------------------------------------
FB = "33.5.1"
for coord, purpose in [
    ("com.google.firebase:firebase-bom", "Aligns Firebase SDK versions"),
    ("com.google.firebase:firebase-messaging", "Push notifications + token lifecycle"),
    ("com.google.firebase:firebase-crashlytics", "Crash reports with deobfuscated stack traces"),
    ("com.google.firebase:firebase-analytics", "Product analytics events"),
    ("com.google.firebase:firebase-perf", "Startup/network performance traces"),
]:
    items.append(item(coord, "gradle", "firebase", purpose, version=FB, license_="Apache-2.0",
                      repo="https://github.com/firebase/firebase-android-sdk", coordinate=coord, declared_in=TOML))

# --------------------------------------------------------------------------
# Observability
# --------------------------------------------------------------------------
for coord, ver, purpose in [
    ("io.sentry:sentry-android", "7.14.0", "Crash + ANR + performance monitoring, no PII"),
    ("io.sentry:sentry-okhttp", "7.14.0", "HTTP spans with request breadcrumbs"),
]:
    gradle(coord, purpose, ver, "see repo", "https://github.com/getsentry/sentry-java", cat="observability")

# --------------------------------------------------------------------------
# Testing
# --------------------------------------------------------------------------
for coord, ver, lic, repo, purpose in [
    ("junit:junit", "4.13.2", "EPL-1.0", "https://github.com/junit-team/junit4", "JVM unit test runner"),
    ("androidx.test.ext:junit", "1.2.1", "Apache-2.0", "https://github.com/androidx/androidx", "AndroidJUnit4 runner"),
    ("androidx.test:core", "1.6.1", "Apache-2.0", "https://github.com/androidx/androidx", "Test application + instrumentation plumbing"),
    ("androidx.test.espresso:espresso-core", "3.6.1", "Apache-2.0", "https://github.com/androidx/androidx", "View assertions alongside Compose tests"),
]:
    gradle(coord, purpose, ver, lic, repo, cat="testing")

# --------------------------------------------------------------------------
# Gradle plugins + JDK
# --------------------------------------------------------------------------
for name, ver, repo, purpose, coord in [
    ("com.android.application", "8.7.2", "https://github.com/google/android-gradle-plugin", "Android application packaging, R8, signing", "com.android.tools.build:gradle"),
    ("org.jetbrains.kotlin.android", "2.0.21", "https://github.com/JetBrains/kotlin", "Kotlin compiler for Android", "org.jetbrains.kotlin:kotlin-gradle-plugin"),
    ("org.jetbrains.kotlin.plugin.compose", "2.0.21", "https://github.com/JetBrains/kotlin", "Compose compiler (Kotlin 2.0 plugin)", "org.jetbrains.kotlin:compose-compiler-gradle-plugin"),
    ("org.jetbrains.kotlin.plugin.serialization", "2.0.21", "https://github.com/JetBrains/kotlin", "Generates @Serializable codecs", "org.jetbrains.kotlin:kotlin-serialization"),
    ("com.google.devtools.ksp", "2.0.21-1.0.28", "https://github.com/google/ksp", "Symbol processing for Room", "com.google.devtools.ksp:symbol-processing-gradle-plugin"),
    ("com.google.gms.google-services", "4.4.2", "https://github.com/google/gradle", "Injects google-services.json (conditional)", "com.google.gms:google-services"),
    ("com.google.firebase.crashlytics", "3.0.2", "https://github.com/firebase/firebase-android-sdk", "Uploads mapping file for readable crashes (conditional)", "com.google.firebase:firebase-crashlytics-gradle"),
]:
    items.append(item(name, "gradle-plugin", "build", purpose, version=ver, license_="Apache-2.0",
                      repo=repo, coordinate=coord, declared_in="android/build.gradle.kts"))

items.append(item("gradle", "tool", "build", "Build system + wrapper", version="8.10.2", license_="Apache-2.0",
                  repo="https://github.com/gradle/gradle",
                  declared_in="android/gradle/wrapper/gradle-wrapper.properties",
                  verify={"file": "android/gradle/wrapper/gradle-wrapper.properties", "contains": "gradle-8.10.2"}))
items.append(item("openjdk", "tool", "build", "JDK 17 toolchain for AGP/Kotlin", version="17",
                  license_="GPL-2.0 with Classpath Exception", repo="https://github.com/openjdk/jdk",
                  declared_in=".github/workflows/ci.yml",
                  verify={"file": ".github/workflows/ci.yml", "contains": "java-version: '17'"}))

# --------------------------------------------------------------------------
# Python runtime
# --------------------------------------------------------------------------
PY = "backend/requirements.txt"
for name, ver, lic, purpose, repo in [
    ("fastapi", "0.162.0", "MIT", "ASGI framework: routing, validation, OpenAPI", "https://github.com/fastapi/fastapi"),
    ("uvicorn", "0.54.0", "BSD-3-Clause", "ASGI server with uvloop/httptools extras", "https://github.com/encode/uvicorn"),
    ("pydantic", "2.14.1", "MIT", "Runtime validation and the API schema", "https://github.com/pydantic/pydantic"),
    ("pydantic-settings", "2.16.1", "MIT", "12-factor config from env/.env", "https://github.com/pydantic/pydantic-settings"),
    ("python-dotenv", "1.2.4", "BSD-3-Clause", "Dotenv loading for local dev", "https://github.com/theskumar/python-dotenv"),
    ("python-multipart", "0.0.32", "Apache-2.0", "Multipart form parsing", "https://github.com/Kludex/python-multipart"),
    ("rapidfuzz", "3.14.6", "MIT", "Fuzzy + partial ratio scoring", "https://github.com/rapidfuzz/RapidFuzz"),
    ("supabase", "2.33.0", "MIT", "PostgREST client, optional data plane", "https://github.com/supabase/supabase-py"),
    ("httpx", "0.28.1", "BSD-3-Clause", "Async HTTP client for AI providers", "https://github.com/encode/httpx"),
    ("cachetools", "7.2.5", "MIT", "TTL cache for AI insights", "https://github.com/tkem/cachetools"),
    ("tenacity", "9.2.1", "Apache-2.0", "Retry policy helpers", "https://github.com/jd/tenacity"),
    ("pyjwt", "2.15.1", "MIT", "JWT encode/decode (HS256)", "https://github.com/jpadilla/pyjwt"),
    ("argon2-cffi", "25.1.0", "MIT", "Password hashing", "https://github.com/hynek/argon2-cffi"),
    ("slowapi", "0.1.10", "MIT", "Per-route rate limiting", "https://github.com/laurents/slowapi"),
    ("structlog", "26.1.0", "Apache-2.0 / MIT", "Structured JSON logging", "https://github.com/hynek/structlog"),
    ("sentry-sdk", "2.73.0", "see repo", "Backend error + performance reporting", "https://github.com/getsentry/sentry-python"),
    ("prometheus-fastapi-instrumentator", "8.1.0", "ISC", "/metrics endpoint", "https://github.com/trallnag/prometheus-fastapi-instrumentator"),
    ("sqlalchemy", "2.1.6", "MIT", "Async ORM (optional Postgres)", "https://github.com/sqlalchemy/sqlalchemy"),
    ("alembic", "1.20.1", "MIT", "Schema migrations", "https://github.com/sqlalchemy/alembic"),
    ("asyncpg", "0.32.0", "Apache-2.0", "Postgres driver", "https://github.com/MagicStack/asyncpg"),
    ("redis", "8.1.0", "MIT", "Shared rate-limit storage", "https://github.com/redis/redis-py"),
    ("qrcode", "8.2", "BSD-3-Clause", "QR generation for share links", "https://github.com/lincolnloop/python-qrcode"),
    ("pillow", "13.0.0", "HPND", "Image processing for QR output", "https://github.com/python-pillow/Pillow"),
    ("geopy", "2.5.0", "MIT", "Geocoding helpers for nearby search", "https://github.com/geopy/geopy"),
    ("jinja2", "3.1.6", "BSD-3-Clause", "HTML template for the landing page", "https://github.com/pallets/jinja"),
    ("pyyaml", "6.0.3", "MIT", "YAML parsing (stack manifest, configs)", "https://github.com/yaml/pyyaml"),
]:
    items.append(item(name, "pip", "backend", purpose, version=ver, license_=lic, repo=repo, declared_in=PY))

# --------------------------------------------------------------------------
# Python dev tooling
# --------------------------------------------------------------------------
DEV = "backend/requirements-dev.txt"
for name, ver, lic, purpose, repo in [
    ("pytest", "9.1.1", "MIT", "Test runner", "https://github.com/pytest-dev/pytest"),
    ("pytest-asyncio", "1.4.0", "Apache-2.0", "async test support", "https://github.com/pytest-dev/pytest-asyncio"),
    ("pytest-cov", "7.1.0", "MIT", "Coverage plugin", "https://github.com/pytest-dev/pytest-cov"),
    ("respx", "0.23.1", "BSD-3-Clause", "Mock httpx transports", "https://github.com/lundberg/respx"),
    ("ruff", "0.17.2", "MIT", "Lint + format (Rust)", "https://github.com/astral-sh/ruff"),
    ("mypy", "2.4.0", "MIT", "Static type checking", "https://github.com/python/mypy"),
    ("pre-commit", "4.7.0", "MIT", "Git hooks for lint/format/secret scanning", "https://github.com/pre-commit/pre-commit"),
    ("faker", "40.43.0", "MIT", "Synthetic demo data", "https://github.com/joke2k/faker"),
    ("freezegun", "1.5.5", "Apache-2.0", "Deterministic timestamps in tests", "https://github.com/spulec/freezegun"),
    ("pip-audit", "2.10.1", "Apache-2.0", "Known-vulnerability scan", "https://github.com/pypa/pip-audit"),
    ("pip-licenses", "5.5.5", "MIT", "Authoritative license report", "https://github.com/raimon49/pip-licenses"),
    ("bandit", "1.9.4", "Apache-2.0", "Security linting", "https://github.com/PyCQA/bandit"),
]:
    items.append(item(name, "pip", "tooling", purpose, version=ver, license_=lic, repo=repo, declared_in=DEV))

# --------------------------------------------------------------------------
# Transitives that ship in the image (verified against the installed venv)
# --------------------------------------------------------------------------
for name, purpose in [
    ("starlette", "ASGI toolkit underneath FastAPI"),
    ("anyio", "Async I/O abstraction"),
    ("h11", "HTTP/1.1 state machine"),
    ("httpcore", "Sync/async HTTP transports for httpx"),
    ("certifi", "CA bundle for TLS verification"),
    ("idna", "Internationalised domain names"),
    ("click", "CLI parsing (uvicorn)"),
    ("uvloop", "Fast event loop (uvicorn[standard])"),
    ("httptools", "HTTP parser (uvicorn[standard])"),
    ("watchfiles", "Reloader for --reload"),
    ("websockets", "WS support in the ASGI server"),
    ("typing-extensions", "Backported typing features used by pydantic/FastAPI"),
    ("annotated-types", "Typed metadata for pydantic field constraints"),
    ("packaging", "Version parsing shared by tooling"),
    ("urllib3", "HTTP transport used by tooling and SDKs"),
]:
    items.append(item(name, "pip", "transitive", purpose, license_="see repo", declared_in=PY))

# --------------------------------------------------------------------------
# CI actions
# --------------------------------------------------------------------------
for action, ver, purpose, lic in [
    ("actions/checkout", "v4", "Clone the repo in CI", "MIT"),
    ("actions/setup-python", "v5", "Provision Python 3.11 + pip cache", "MIT"),
    ("actions/setup-java", "v4", "Provision Temurin JDK 17", "MIT"),
    ("gradle/actions/setup-gradle", "v4", "Gradle provisioning + build cache", "MIT"),
    ("actions/upload-artifact", "v4", "Publish APK + coverage artifacts", "MIT"),
    ("android-actions/setup-android", "v3", "Android SDK + licenses on the runner", "MIT"),
    ("softprops/action-gh-release", "v2", "Attach the APK to a GitHub Release", "MIT"),
    ("github/codeql-action", "v3", "Static analysis (security-extended)", "MIT"),
]:
    items.append(item(action, "action", "ci", purpose, version=ver, license_=lic,
                      repo=f"https://github.com/{action}", declared_in=".github/workflows/"))

# de-duplicate by name, keep insertion order
seen: set[str] = set()
unique: list[dict] = []
for entry in items:
    if entry["name"] in seen:
        continue
    seen.add(entry["name"])
    unique.append(entry)

document = {
    "version": 1,
    "policy": {
        "min_total": 100,
        "rule": "Every entry must be verifiable against the repo or the installed environment.",
    },
    "counts": dict(Counter(entry["category"] for entry in unique)),
    "items": unique,
}

OUT.write_text(yaml.safe_dump(document, sort_keys=False, allow_unicode=True, width=120), encoding="utf-8")
print(f"wrote {OUT} with {len(unique)} items")
print("by category:", document["counts"])

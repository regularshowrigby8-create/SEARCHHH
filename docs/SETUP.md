# Setup

## Requirements

| Tool | Version | Notes |
| --- | --- | --- |
| Python | 3.11+ | 3.11 and 3.12 tested |
| JDK | 17 | AGP 8.7 requires 17; 21 works too |
| Android SDK | Platform 35, Build-Tools 35.0.0 | Android Studio Ladybug or `sdkmanager` |
| Gradle | 8.10.2 | provided by the wrapper / CI |
| Device | Android 8.0 (API 26)+ | emulator or phone |

## 1. Backend

```bash
make install                       # .venv + pinned requirements-dev.txt
cp .env.example backend/.env       # optional; every value has a working default
make dev                           # uvicorn --reload on 0.0.0.0:8000
```

Check it:

```bash
curl -s localhost:8000/health | jq
curl -s "localhost:8000/api/v1/search?q=cofeee&limit=2" | jq '.results[0].title'
open http://localhost:8000/docs
```

### Making Supabase real (optional)

```bash
# backend/.env
SUPABASE_URL=https://<project>.supabase.co
SUPABASE_SERVICE_ROLE_KEY=<service-role-key>
```

Create both tables:

```sql
create table entries (
  id text primary key,
  title text not null,
  subtitle text default '',
  description text default '',
  category text default 'general',
  tags text[] default '{}',
  price numeric,
  currency text default 'ZAR',
  rating numeric default 0,
  image_url text,
  location text,
  url text,
  updated_at timestamptz default now()
);

create table feedback (
  id uuid primary key default gen_random_uuid(),
  message text not null,
  rating int check (rating between 1 and 5),
  entry_id text,
  contact text,
  platform text default 'android',
  app_version text,
  created_at timestamptz default now()
);

alter table entries enable row level security;
alter table feedback enable row level security;
create policy "public read" on entries for select using (true);
create policy "anyone may send feedback" on feedback for insert with check (true);
```

Restart the API; `GET /api/v1/integrations` should now show `supabase: configured`.

### Firebase (optional)

1. Create a Firebase project → add Android app `com.searchhh.app`.
2. Download `google-services.json` into `android/app/`.
3. Rebuild. The `com.google.gms.google-services` and Crashlytics Gradle plugins are
   applied automatically **only when that file exists**; without it the app
   compiles and runs, and every Firebase call is skipped at runtime.

### Sentry (optional)

```bash
# backend/.env
SENTRY_DSN=https://<key>@o000.ing. Ingest…   # your DSN
```

For the app, pass it at build time:

```bash
./gradlew assembleRelease -Psearchhh.sentryDsn="https://<key>@o0.ingest.sentry.io/0"
```

## 2. Android

```bash
cd android
./gradlew :app:assembleDebug        # or open the folder in Android Studio
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Pointing the app at your backend

| Where the app runs | Base URL | How |
| --- | --- | --- |
| Emulator | `http://10.0.2.2:8000/` | default, no action |
| Phone on same Wi-Fi | `http://<laptop-lan-ip>:8000/` | Settings → Change URL |
| USB-only phone | `adb reverse tcp:8000 tcp:8000` then `http://127.0.0.1:8000/` | run the adb command, set the URL |
| Deployed API | `https://api.example.com/` | Settings → Change URL |

Release builds only permit HTTPS (`res/xml/network_security_config.xml`); debug
builds also allow cleartext to loopback and private ranges so you can test against
a laptop.

### Build variants

| Variant | applicationId | Minified | Telemetry | Cleartext |
| --- | --- | --- | --- | --- |
| `debug` | `com.searchhh.app.debug` | no | off | allowed for dev hosts |
| `release` | `com.searchhh.app` | R8 + resource shrinking | on | blocked |

Both variants can be installed side by side, which is handy for comparing a
release build against a debug build on one phone.

## 3. Everyday commands

```bash
make dev        # API with reload
make test       # backend tests
make cov        # coverage gate
make lint       # ruff
make stack      # verify the 121-tool inventory + regenerate STACK.md
make apk-debug  # gradle :app:assembleDebug
make apk        # gradle :app:assembleRelease (needs keystore.properties)
```

Android-specific:

```bash
cd android
./gradlew :app:testDebugUnitTest      # 22 JVM tests
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest   # needs an emulator/device
./gradlew :app:dependencies           # full resolved graph
```

## 4. Troubleshooting

| Symptom | Fix |
| --- | --- |
| App shows "Backend unreachable" | Confirm `curl localhost:8000/health`; on a phone use the LAN IP, not `localhost` |
| `Cleartext HTTP traffic not permitted` | You are on a release build - use HTTPS, or test with the debug APK |
| Gradle fails resolving `2.0.21-1.0.28` | Your mirror lacks that KSP build; check `libs.versions.toml` against the KSP release page |
| Room "schema export" warning | `exportSchema = false` is intentional; schemas are not committed |
| Firebase throws at startup | Delete `google-services.json` or complete the Firebase setup - the app is designed to run without it |
| `./gradlew: Permission denied` | `chmod +x android/gradlew` |
| Emulator cannot reach host | Use `10.0.2.2`, or `adb reverse tcp:8000 tcp:8000` |

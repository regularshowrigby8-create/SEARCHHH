# Searchhh 0.2 included backend — design and validation boundary

## No deployment by the app user

Onboarding requests a display name only. The app generates a random UUID installation
identifier and an independent 256-bit bearer credential with `SecureRandom`. Both
are stored in AndroidX encrypted preferences backed by the Android Keystore. Names
are never passwords or unique identifiers. Backup is disabled. Reinstallation/data
clear creates a new identity. Changing the name preserves the installation identity.

The backend is Kotlin + Room + coroutines, packaged inside the APK. The Compose UI
calls its API directly in process, avoiding localhost HTTP connection dependencies.
A user-started foreground service owns searches and a Ktor CIO server running the
**official MCP Kotlin SDK**. No operator deployment, Docker or database server is
required for this Android path. The existing FastAPI/PostgreSQL/Redis deployment is
retained as an optional separate architecture, not smuggled in as a prerequisite.

## What is reused

| Existing tool | Purpose | Source |
|---|---|---|
| EinkBro | Existing WebView browser | https://github.com/plateaukao/einkbro |
| AndroidX Room, Security, foreground-service APIs | On-device persistence and credentials | https://github.com/androidx/androidx |
| Ktor CIO | Embedded HTTP server | https://github.com/ktorio/ktor |
| Official MCP Kotlin SDK 0.4.0 | MCP negotiation, SSE sessions and tool dispatch | https://github.com/modelcontextprotocol/kotlin-sdk/tree/0.4.0 |
| JSch (mwiede fork) | Outbound SSH reverse forwarding | https://github.com/mwiede/jsch |
| localhost.run | Accountless, temporary public HTTPS forwarding | https://localhost.run/docs/ |
| OkHttp and Jsoup | Public HTTP and HTML parsing | https://github.com/square/okhttp / https://github.com/jhy/jsoup |
| crawler-commons | robots.txt rules | https://github.com/crawler-commons/crawler-commons |
| SearXNG / searx.space | Existing metasearch adapters and public-instance discovery | https://github.com/searxng/searxng / https://searx.space |

The SDK version uses **MCP SSE**, not the newer Streamable HTTP transport. Compatible
clients need support for SSE and Authorization headers. Cloudflare Quick Tunnels
were rejected for this transport because their documentation excludes SSE.

## Connectivity without fake hosting

- Ordinary in-app calls do not depend on a tunnel or loopback HTTP at all.
- MCP listens on a random loopback port; JSch initiates an outbound SSH connection
  to localhost.run using its documented `nokey` accountless mode.
- Remote exposure requires the generated bearer credential on every request. Browser
  Origin requests are rejected; request bodies are bounded. No shell execution,
  arbitrary device controls, credential-reading or form-submission tool is exposed.
- SSH host keys use explicit **trust on first use**, stored per installation.
  Changed keys fail closed. This is not an independently certified first-use pin.
- localhost.run terminates HTTPS and can see forwarded traffic. Its free URL can
  change and it has no reliability promise here. Pairing is an explicit share action;
  credentials are never derived from the public URL or the name.
- Disable remote access in Settings, stop the server via the notification, or rotate
  the credential to revoke future calls from a paired client.
- Android may stop the process. Running a foreground service does not override OS,
  battery or network restrictions. Interrupted searches are marked, not called live.

## 128 sources: exactly what the number means

128 catalog entries are derived from existing SearXNG adapters; they are not 128
new original engines or a claim of 128 simultaneously reachable indexes. General,
developer, research, reference, community, news and specialist sources are included.

GitHub repository search and Hacker News search have direct keyless connectors.
Other selected sources are queried through an automatically discovered **public
SearXNG service**, using its regular HTML search interface. This avoids requiring
operators to enable its often-disabled JSON format. The service's `/config` response
is checked and missing adapters are reported. Presence in `/config` does not prove
successful results. No public instance is secretly owned or deployed by Searchhh.

Searches run in batches of eight with 75-second cool-downs (150 seconds on errors).
No IP rotation, challenge solving or instance rotation after a blocked search.
Discovery failure leaves direct sources available and surfaces an error. Public
services remain third-party dependencies, can see queries, and can block access.
The display name, installation ID and agent credential are not sent to search providers.

## MCP tools

`server_status`, `list_sources`, `start_search`, `get_results`, `stop_search`,
`list_saved`, `save_result`.

MCP is a tool interface, **not an included AI model**. An external, separately chosen
MCP-compatible AI client may use these tools. No free unlimited AI service is claimed.

## Local limits and persistence

One active session per installation; 1,000 results per phone session; bounded response
sizes and deadlines. Results persist in Room and saves remain local. Stop prevents
later result commits, even if a request finishes in flight. Robots parsing is reused
from crawler-commons. Forms are discovered but not fetched/submitted by the crawler.

## Validation status

Implementation is under test. Do not treat the prior 0.1 APK as including these changes.
CI must compile, lint, test Room/identity/MCP authentication and tools on Android,
then publish a new APK. Public tunnel connectivity and search accuracy are separate
from passing local device/protocol tests and must be reported honestly.

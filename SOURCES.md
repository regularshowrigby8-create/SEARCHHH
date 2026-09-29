
## Searchhh 0.3 integration slice

The Android global portal seed catalog (`searchhh-portals.json`) lists public
publisher/programme URLs; it does not redistribute page content or count them as
110 bespoke search engines. The optional external legacy SearXNG catalog is unchanged.

`PortalParser`, `PortalCrawler`, `PublicHttp`, and `searchhh/ai/*` are minimal
integration/policy glue over existing Jsoup, crawler-commons, OkHttp, AndroidX
Security, Room and Compose dependencies. No additional model runtime or SDK was
bundled. See `docs/searchhh/RELEASE-0.3.md` for published API references and limits.
The developer directory https://github.com/mnfst/awesome-free-llm-apis was a research
lead, not copied source or a certified model inventory.

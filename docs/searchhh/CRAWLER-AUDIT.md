# Crawler codebase audit — 2026-09-29

**All original entries #1–100 are retained, plus 20 additions (#101–120).** Original names and URLs are preserved, including unresolved entries. The existing **110 portal seeds are unchanged**. Portal sources, software codebases, dependencies and working adapters are separate counts.

## What was actually checked

GitHub repository API metadata, canonical redirects, source revisions and detected license metadata were fetched with `gh`. Non-GitHub source references were checked against project/SourceForge/GNU/Apache pages. Repository metadata is not a build test; archived=false is not a maintenance/security guarantee; an SPDX detector result is not a complete licensing review.

Lookup outcomes: **106 github metadata verified**, **11 upstream source link verified**, **2 not found**, **1 packaging repository verified**.

No arbitrary project code was executed during the audit. The Android app ships the reference registry and its own existing library-based collection path, not 120 third-party executables.

## All five requested ignore options are retained

`requestedIgnoreProfile` preserves robots.txt, rate limits, politeness delays, max depth and domain scope as requested. **`requestedProfileIsRuntimeConfig` is false.** These are recorded requirements, not secretly enabled switches. Each entry has five `configurationSupport` fields. Unverified stays unverified; non-crawler libraries do not magically acquire crawler options.

Code-level checks confirm configurable client settings in Scrapy, Colly, Katana and GoSpider. In particular, GoSpider’s `--robots` means discover URLs from robots.txt, not necessarily obey/ignore robots rules. Changing client pacing cannot disable a remote server’s enforced quotas. Public runtime retains robots, server cooldowns, resource bounds, host-scoped follow-up and private-IP protection.

## Exact unresolved identities — retained, not omitted

- **#50 nicmart/PHPCrawler:** GitHub 404; original identity not resolved. A PHPCrawl alternative is recorded without claiming it is the same project.
- **#78 s0md3v/urlscan:** GitHub 404; no matching repository found under that owner. Not silently replaced by urlscan.io.
- **#92 spelling correction:** same-owner `z0m31en7/Uscrapper` was subsequently verified by GitHub metadata and README. It is recorded as the probable intended project, with the malformed original URL preserved.

## Important corrections

- #13 StormCrawler resolves to `apache/stormcrawler`; #17 Scrapy-Splash resolves to `scrapy-plugins/scrapy-splash`.
- #21 BUbiNG source is `LAW-Unimi/BUbiNG`, linked by the official LAW software page. That page says GPL while GitHub detects Apache-2.0: inspect the chosen revision before importing.
- #36 corrected `madeindjs/spider` resolves to `spider-rs/spider`; the submitted `spider_rust` path did not resolve.
- #40 DataparkSearch source located under `Maxime2/dataparksearch`; #41/42 historical sources located on SourceForge.
- #49 Droids retired on 2015-11-01; Apache documents its historical SVN URL, not the submitted GitHub path.
- #83 is a Kali **packaging repository**, not proof that original Java source is present. The verified tree includes a bundled JAR and an upstream branch.
- #117 Norconex’s old repository redirects to `Norconex/crawler`.
- Xapian, Meilisearch, Tinysearch, Tantivy, Lucene, Solr and Sphinx are primarily search/index components; parsers, downloaders, asset discovery and secret scanners have separate roles. All remain listed.

## Original list and additions

“Source reference” means a repository/source archive or an upstream-linked source location. Downloads/checkout/compilation were not performed for every project. See JSON for exact revisions, submitted language, descriptions and field-level configuration evidence.

| # | Project | Role | Source reference / outcome | License metadata | Integration |
|---|---|---|---|---|---|
| 1 | Scrapy | crawler | [Source](https://github.com/scrapy/scrapy/tree/5d789b24f994bcdfca97c34d976c73739e064849) | BSD-3-Clause | existing_optional_backend |
| 2 | Colly | crawler | [Source](https://github.com/gocolly/colly/tree/3fd3b23421a1a0de92995a8737c05ffb1115facb) | Apache-2.0 | catalog_only |
| 3 | Katana | crawler | [Source](https://github.com/projectdiscovery/katana/tree/09c83e9a8b12b3eda90bd2b5d08b88787cadb62e) | MIT | catalog_only |
| 4 | Hakrawler | crawler | [Source](https://github.com/hakluke/hakrawler/tree/52a16fe61bd18b8f431b9d66058cba12e96e4451) | GPL-3.0 | catalog_only |
| 5 | Gospider | crawler | [Source](https://github.com/jaeles-project/gospider/tree/f6cc9a78d709e088e55ce62e5e92ab063f1a184b) | MIT | catalog_only |
| 6 | crawler4j | crawler | [Source](https://github.com/yasserg/crawler4j/tree/68f5c1e4fb86542e74d31c0bcb4b1ae14ba2ea71) | Apache-2.0 | catalog_only |
| 7 | Apache Nutch | crawler | [Source](https://github.com/apache/nutch/tree/81c25c16eded2e7799b0c4f8eb0a66421e782f8f) | Apache-2.0 | catalog_only |
| 8 | Crawlee | crawler | [Source](https://github.com/apify/crawlee/tree/cc99c7e9c6afc4174880a3ab45d481372cc8bc01) | Apache-2.0 | catalog_only |
| 9 | Heritrix3 | archiver | [Source](https://github.com/internetarchive/heritrix3/tree/16deaaf4ebda678ebdcf59e62001abe0674c42b2) | NOASSERTION | catalog_only |
| 10 | WebMagic | crawler | [Source](https://github.com/code4craft/webmagic/tree/67816a19d68a4fec4657bf1336227e046e251df2) | Apache-2.0 | catalog_only |
| 11 | Node-Crawler | crawler | [Source](https://github.com/bda-research/node-crawler/tree/29d547e3423821088968cf44a784ebd68bde86d3) | MIT | catalog_only |
| 12 | pyspider | crawler | [Source](https://github.com/binux/pyspider/tree/897891cafb21ea5b4ac08e728ad2ea212879f7fa) · archived | Apache-2.0 | catalog_only |
| 13 | StormCrawler | crawler | [Source](https://github.com/apache/stormcrawler/tree/e94fed1efbaf93fb665ca186cd161261922d6ca0) | Apache-2.0 | catalog_only |
| 14 | YaCy | crawler_indexer | [Source](https://github.com/yacy/yacy_search_server/tree/b50b76bd552a851f737b98683f20d8b861e137ae) | NOASSERTION | catalog_only |
| 15 | Frontera | frontier | [Source](https://github.com/scrapinghub/frontera/tree/c94beae2f438492139d24759729d0201779ccf1c) | BSD-3-Clause | catalog_only |
| 16 | Crawl4AI | crawler | [Source](https://github.com/unclecode/crawl4ai/tree/e5d2e786d1a101225f3f6a3e6fd344d76eeb13af) | Apache-2.0 | catalog_only |
| 17 | Scrapy-Splash | browser_adapter | [Source](https://github.com/scrapy-plugins/scrapy-splash/tree/72a8788212746b938e1e4d45aad56ff27857924a) | BSD-3-Clause | catalog_only |
| 18 | ScrapyRT | service | [Source](https://github.com/scrapinghub/scrapyrt/tree/6d00a0ad55c310c268fb13a151a3f3c98f0cabb4) | BSD-3-Clause | catalog_only |
| 19 | Scrapyd | service | [Source](https://github.com/scrapy/scrapyd/tree/7e8b2533c647c3bab06c2ac683f97b881dd9228b) | BSD-3-Clause | catalog_only |
| 20 | ACHE Crawler | crawler | [Source](https://github.com/VIDA-NYU/ache/tree/81b29c09bd6cde03e97cad174959a660620bff3c) | Apache-2.0 | catalog_only |
| 21 | BUbiNG | crawler | [Source](https://github.com/LAW-Unimi/BUbiNG/tree/50769f87a1ce13bc2f7335866eb9b8b79cd668c2) | Apache-2.0 | catalog_only |
| 22 | Xapian | indexer | [Source](https://github.com/xapian/xapian/tree/7c4dbe989f97671c226c341eec283639686e35be) | unverified | catalog_only |
| 23 | Playwright | browser_automation | [Source](https://github.com/microsoft/playwright/tree/d67c16ec4e210d4d8652da7bb25dfe11b72deb97) | Apache-2.0 | catalog_only |
| 24 | Puppeteer | browser_automation | [Source](https://github.com/puppeteer/puppeteer/tree/6cfe4df6db196c107ce94118fe40ec001a11de72) | Apache-2.0 | catalog_only |
| 25 | Selenium | browser_automation | [Source](https://github.com/SeleniumHQ/selenium/tree/4732af16c58c8feea0fa59cc7f36ff95aba4bcf9) | Apache-2.0 | catalog_only |
| 26 | BeautifulSoup | parser | [Source](https://code.launchpad.net/beautifulsoup) | MIT | catalog_only |
| 27 | Guzzle | http_client | [Source](https://github.com/guzzle/guzzle/tree/93939470950a9b11e2e84204166ef5e048c55fe4) | MIT | catalog_only |
| 28 | Goutte | http_parser | [Source](https://github.com/FriendsOfPHP/Goutte/tree/1e6989df37b3a4a74b29c8369db3b42d8e9a1c6b) · archived | MIT | catalog_only |
| 29 | Symfony Panther | browser_automation | [Source](https://github.com/symfony/panther/tree/150f38fc00441a3fa5dd7d5c913f87e2be3872ac) | MIT | catalog_only |
| 30 | Roach | crawler | [Source](https://github.com/roach-php/core/tree/725955e04cd48264fe1cbe373bf6be122ad32dec) | unverified | catalog_only |
| 31 | Spatie Crawler | crawler | [Source](https://github.com/spatie/crawler/tree/06c4ddc4839494bb6b872ff550f7828a129c7eef) | MIT | catalog_only |
| 32 | DomCrawler | parser | [Source](https://github.com/symfony/dom-crawler/tree/0a1832b64f19951888d9d642086195a67146e904) | MIT | catalog_only |
| 33 | Meilisearch | indexer | [Source](https://github.com/meilisearch/meilisearch/tree/b4a2e34f2a99cb92bc648430d77ec5ca0f540d7b) | NOASSERTION | catalog_only |
| 34 | Tinysearch | indexer | [Source](https://github.com/tinysearch/tinysearch/tree/d6c92d5a62cec0a09cb153b4ec55365b20c5aa0b) | Apache-2.0 | catalog_only |
| 35 | Tantivy | indexer | [Source](https://github.com/quickwit-oss/tantivy/tree/047464cf92e5a31d02a696f5158e45f7d34c67eb) | MIT | catalog_only |
| 36 | SpiderRust | crawler | [Source](https://github.com/spider-rs/spider/tree/2e39b2db3eff15c3aaa90e3edcd1d7ba30a5bb2a) | MIT | catalog_only |
| 37 | WebSPHINX | crawler | [Source](https://www.cs.cmu.edu/~rcm/websphinx/websphinx.zip) | CMU permissive plus bundled Apache library; review files | catalog_only |
| 38 | JSpider | crawler | [Source](https://sourceforge.net/projects/j-spider/files/) | LGPL (version to verify) | catalog_only |
| 39 | Arachnid | crawler | [Source](https://sourceforge.net/projects/arachnid/files/) | GPL-2.0 | catalog_only |
| 40 | DataparkSearch | crawler_indexer | [Source](https://github.com/Maxime2/dataparksearch/tree/a6118b93bd73dbfe39311f77b7e1126e07231c24) | GPL-2.0 | catalog_only |
| 41 | mnoGoSearch | crawler_indexer | [Source](https://sourceforge.net/projects/mnogosearch/files/) | GPL-2.0 / other; version-specific | catalog_only |
| 42 | ht://Dig | crawler_indexer | [Source](https://sourceforge.net/projects/htdig/files/) | LGPL-2.0 (SourceForge metadata) | catalog_only |
| 43 | Swish-e | crawler_indexer | [Source](https://github.com/swish-e/swish-e/tree/89e839f4a9748e3956ef8b1f3923ab0ad4ea859d) · archived | NOASSERTION | catalog_only |
| 44 | Egothor | crawler_indexer | [Source](https://sourceforge.net/p/egothor/code/) | Other License; review source | catalog_only |
| 45 | Apache Lucene | indexer | [Source](https://github.com/apache/lucene/tree/b304eeae0243892c2e795f7d0a2a9dd4da1b46cb) | Apache-2.0 | catalog_only |
| 46 | Apache Solr | indexer | [Source](https://github.com/apache/solr/tree/634be2a1c1358363fd6110b6502e094027a704e4) | Apache-2.0 | catalog_only |
| 47 | OpenSearchServer | crawler_indexer | [Source](https://github.com/jaeksoft/opensearchserver/tree/a83a7b872c2085bde45cb2d5d13eccd318396ad2) | Apache-2.0 | catalog_only |
| 48 | Sphinx | indexer | [Source](https://github.com/sphinxsearch/sphinx/tree/409f2c2b5b2ff70b04e38f92b6b1a890326bad65) | GPL-2.0 | catalog_only |
| 49 | Apache Droids | crawler | [Source](https://svn.apache.org/repos/asf/incubator/droids/) | Apache project; exact historical files to review | catalog_only |
| 50 | PHP-Crawler | crawler | Unresolved — submitted URL retained in JSON | unverified | catalog_only |
| 51 | PHPCrawl | crawler | [Source](https://sourceforge.net/projects/phpcrawl/files/) | GPL-2.0 | catalog_only |
| 52 | SimpleHTMLDOM | parser | [Source](https://sourceforge.net/p/simplehtmldom/repository/) | MIT | catalog_only |
| 53 | QueryPath | parser | [Source](https://github.com/technosophos/querypath/tree/b0279de7837d199e10b779ed98e2201d8a10e61f) | NOASSERTION | catalog_only |
| 54 | DiDOM | parser | [Source](https://github.com/Imangazaliev/DiDOM/tree/50fa6595d14f22c0c984efed5c818485cf548136) | MIT | catalog_only |
| 55 | Requests-HTML | http_parser | [Source](https://github.com/psf/requests-html/tree/075ac162dc62fc532037df0d98954ab840a97516) | MIT | catalog_only |
| 56 | MechanicalSoup | browser_automation | [Source](https://github.com/MechanicalSoup/MechanicalSoup/tree/5eb3bd4a7736f3985883c08fb1707627457b1ba8) | MIT | catalog_only |
| 57 | RoboBrowser | browser_automation | [Source](https://github.com/jmcarp/robobrowser/tree/4284c11d00ae1397983e269aa180e5cf7ee5f4cf) | BSD-3-Clause | catalog_only |
| 58 | Grab | crawler | [Source](https://github.com/lorien/grab/tree/d94671abb4f35d7a8f15fed4b07fe8932581ee04) | MIT | catalog_only |
| 59 | Portia | crawler | [Source](https://github.com/scrapinghub/portia/tree/606467d278eab2236afcb3d260cb03bf6fb906a0) · archived | BSD-3-Clause | catalog_only |
| 60 | Helium | browser_automation | [Source](https://github.com/mherrmann/helium/tree/624f729746f7a742df1c407accbd0d4a30f49167) | MIT | catalog_only |
| 61 | Autoscraper | extractor | [Source](https://github.com/alirezamika/autoscraper/tree/68a818158c673bf320a8569da10a8b979c1d23fe) | MIT | catalog_only |
| 62 | Newspaper3k | extractor | [Source](https://github.com/codelucas/newspaper/tree/f8e3cb63c87ff53080fab77f4bafef2ecf8179f7) | MIT | catalog_only |
| 63 | Readability | extractor | [Source](https://github.com/buriy/python-readability/tree/3e7a8321383b9f5e2c5a5a887f940f864c5c73d0) | Apache-2.0 | catalog_only |
| 64 | Goose3 | extractor | [Source](https://github.com/goose3/goose3/tree/87b6003d740e1be591ede6a2c328e02ed7c1d84c) | Apache-2.0 | catalog_only |
| 65 | Trafilatura | extractor_crawler | [Source](https://github.com/adbar/trafilatura/tree/1e31e3e9eb2e4f6fbfd4bc04355bc74005a780e6) | Apache-2.0 | new_optional_backend |
| 66 | Extruct | structured_extractor | [Source](https://github.com/scrapinghub/extruct/tree/dc3bf7d2209ecf421222afc90d3d2dd8c6fd23cf) | BSD-3-Clause | new_optional_backend |
| 67 | Parsel | parser | [Source](https://github.com/scrapy/parsel/tree/dcbecf66430e2b0b84e9a29a8d605f9166713596) | BSD-3-Clause | existing_backend_dependency |
| 68 | Wget | recursive_downloader | [Source](https://ftp.gnu.org/gnu/wget/) | GPL (check chosen release) | catalog_only |
| 69 | HTTrack | archiver | [Source](https://github.com/xroche/httrack/tree/0b84a75ef2f7bb101e29c2334be1e399f94047a5) | GPL-3.0 | catalog_only |
| 70 | Curl | http_client | [Source](https://github.com/curl/curl/tree/5c58f89fff655f275dfac80ee47eda10257f9154) | NOASSERTION | catalog_only |
| 71 | Axel | downloader | [Source](https://github.com/axel-download-accelerator/axel/tree/4679ed7657abeebfc92f083692f9f666a129db14) | GPL-2.0 | catalog_only |
| 72 | Aria2 | downloader | [Source](https://github.com/aria2/aria2/tree/9e7273583f83e881e3ec067b523ba88724088d2f) | GPL-2.0 | catalog_only |
| 73 | Waybackpy | archive_client | [Source](https://github.com/akamhy/waybackpy/tree/3b3e78d901a600bb22943202c6a8981ca04a5e48) | MIT | catalog_only |
| 74 | Wayback Machine Scraper | archive_client | [Source](https://github.com/sangaline/wayback-machine-scraper/tree/32ba9503fa8438ee75d16909911821d6ca336e8f) | ISC | catalog_only |
| 75 | Gau (GetAllURLs) | passive_url_discovery | [Source](https://github.com/lc/gau/tree/8201b9d1febadba98e9cb81e3f253284e8a4a88b) | MIT | catalog_only |
| 76 | Gauplus | passive_url_discovery | [Source](https://github.com/bp0lr/gauplus/tree/15a2900b88092da32ba45860e76527985421ec8e) · archived | MIT | catalog_only |
| 77 | Waybackurls | passive_url_discovery | [Source](https://github.com/tomnomnom/waybackurls/tree/8d27cf3e3031de01179e8ba9127e968eb01008e9) | unverified | catalog_only |
| 78 | Urlscan | identity_unresolved | Unresolved — submitted URL retained in JSON | unverified | catalog_only |
| 79 | Cariddi | security_crawler | [Source](https://github.com/edoardottt/cariddi/tree/5ae392d2f064bc6e3a59ed999e337c4a03f4e9e4) | GPL-3.0 | catalog_only |
| 80 | GoBuster | content_enumeration | [Source](https://github.com/OJ/gobuster/tree/c77583fbb8824058d74d38240896eff610683c8e) | Apache-2.0 | catalog_only |
| 81 | FFuF | content_enumeration | [Source](https://github.com/ffuf/ffuf/tree/8d65e0373e5f223530174e5694285af849770078) | MIT | catalog_only |
| 82 | Dirsearch | content_enumeration | [Source](https://github.com/maurosoria/dirsearch/tree/902b89182810f6b5f7613472a0383b03ee3423c5) | unverified | catalog_only |
| 83 | Dirbuster | content_enumeration | [Source](https://gitlab.com/kalilinux/packages/dirbuster/-/tree/upstream) · packaging, not original source certification | unverified | catalog_only |
| 84 | Feroxbuster | content_enumeration | [Source](https://github.com/epi052/feroxbuster/tree/1f595dab5c76858d5a14fbc47dabf2563d729c62) | MIT | catalog_only |
| 85 | Rustbuster | content_enumeration | [Source](https://github.com/phra/rustbuster/tree/4a243d4a5b943c88adcc1b2e55201234cb456900) | GPL-3.0 | catalog_only |
| 86 | LinkFinder | javascript_link_extractor | [Source](https://github.com/GerbenJavado/LinkFinder/tree/1debac5dace4724fd6187c06f133578dae51c86f) | MIT | catalog_only |
| 87 | JSFinder | javascript_link_extractor | [Source](https://github.com/Threezh1/JSFinder/tree/d70ab9bc5221e016c08cffaf0d9ac79646c90645) | unverified | catalog_only |
| 88 | SecretFinder | secret_scanner | [Source](https://github.com/m4ll0k/SecretFinder/tree/d06119dedd9c1505137d1ec4792d5d5b65c7425d) | GPL-3.0 | catalog_only |
| 89 | Mantra | secret_scanner | [Source](https://github.com/brosck/mantra/tree/6026816210df756f8cc8e9d637b9f49fb277a5f0) | GPL-3.0 | catalog_only |
| 90 | Crawlergo | security_crawler | [Source](https://github.com/Qianlitp/crawlergo/tree/38b6364285b05eac18b4f9a23062f64ad35826f4) | GPL-3.0 | catalog_only |
| 91 | Rad | security_crawler | [Source](https://github.com/chaitin/rad/tree/7238020cdd1fca2c6996c85431ecf9f47d236a69) | unverified | catalog_only |
| 92 | Uscraper | security_crawler | [Source](https://github.com/z0m31en7/Uscrapper/tree/da58cba891e07c3003d874924dd4b39d393ddd2c) | MIT | catalog_only |
| 93 | Photon | security_crawler | [Source](https://github.com/s0md3v/Photon/tree/635c25a36b10bc9973eab65cc90f5361483b6603) | GPL-3.0 | catalog_only |
| 94 | Sn1per | security_suite | [Source](https://github.com/1N3/Sn1per/tree/a5450bc496a1af04f8368888f86d086a9fc046b8) | NOASSERTION | catalog_only |
| 95 | Osmedeus | security_suite | [Source](https://github.com/j3ssie/osmedeus/tree/9a02ed0f9dbd3d9fd7f006610264016058fd8a0e) | MIT | catalog_only |
| 96 | Amass | asset_discovery | [Source](https://github.com/owasp-amass/amass/tree/79299dce87b0085db0f2f4ef3e9c52cccb49f514) | NOASSERTION | catalog_only |
| 97 | Subfinder | asset_discovery | [Source](https://github.com/projectdiscovery/subfinder/tree/4debdd5fdba0278931421239fe00167b29fa8d7d) | MIT | catalog_only |
| 98 | Assetfinder | asset_discovery | [Source](https://github.com/tomnomnom/assetfinder/tree/4e95d8701aae8cff1c27af2626eb22ba110ad583) | MIT | catalog_only |
| 99 | Findomain | asset_discovery | [Source](https://github.com/Findomain/Findomain/tree/cc5f40c03fcc20d8b31e263dc5d4c0b22edfaad7) | GPL-3.0 | catalog_only |
| 100 | Chaos | asset_discovery | [Source](https://github.com/projectdiscovery/chaos-client/tree/17a19d75e790394de3b8efbafcb8157f455a5a0b) | MIT | catalog_only |
| 101 | Crawlee Python | crawler | [Source](https://github.com/apify/crawlee-python/tree/c6fba871dc9b9e7cde2b14fbc30b187dee771e85) | Apache-2.0 | catalog_only |
| 102 | Browsertrix Crawler | archiver | [Source](https://github.com/webrecorder/browsertrix-crawler/tree/0c1bb81698f4a700f8060988f082229b84fb05f1) | AGPL-3.0 | catalog_only |
| 103 | Brozzler | archiver | [Source](https://github.com/internetarchive/brozzler/tree/c80dbc7ade89bf600535443a36055517983a8331) | Apache-2.0 | catalog_only |
| 104 | Scrapling | crawler | [Source](https://github.com/D4Vinci/Scrapling/tree/b980c83516ba030a3f0f3fffe3f6366f00869c54) | BSD-3-Clause | catalog_only |
| 105 | Scrapy Playwright | browser_adapter | [Source](https://github.com/scrapy-plugins/scrapy-playwright/tree/d99f38d3483118881add4e2bcbc595d45091f196) | BSD-3-Clause | catalog_only |
| 106 | Scrapy Redis | frontier | [Source](https://github.com/rmax/scrapy-redis/tree/b4f333aec34e88bdd6cb7f563635c49f1b5fb122) | MIT | catalog_only |
| 107 | Scrapy Cluster | crawler | [Source](https://github.com/istresearch/scrapy-cluster/tree/01861c2dca1563aab740417d315cc4ebf9b73f72) · archived | MIT | catalog_only |
| 108 | OpenWPM | browser_crawler | [Source](https://github.com/openwpm/OpenWPM/tree/f655dfa1a68cd9f00eb52e68e2fed57d27b8c0aa) | NOASSERTION | catalog_only |
| 109 | Firecrawl | crawler | [Source](https://github.com/firecrawl/firecrawl/tree/24b827e7d948c72fac1f9d6dd434e0b8cd76e812) | AGPL-3.0 | catalog_only |
| 110 | Crawlab | orchestrator | [Source](https://github.com/crawlab-team/crawlab/tree/0485310def8b4f31ea20997846a8d5e7dfc681e5) | BSD-3-Clause | catalog_only |
| 111 | Elastic Open Web Crawler | crawler | [Source](https://github.com/elastic/crawler/tree/2a967462ec5f14a2320ac3047e5f850d2bfe940a) | NOASSERTION | catalog_only |
| 112 | Ferret | browser_crawler | [Source](https://github.com/MontFerret/ferret/tree/5238ed795fe83ee76ca359b9e7bec6b9db2ffc05) | Apache-2.0 | catalog_only |
| 113 | Geziyor | crawler | [Source](https://github.com/geziyor/geziyor/tree/229b8ca83ac1ff9bd17ce533622a5d915ce36f56) | MPL-2.0 | catalog_only |
| 114 | LinkChecker | crawler | [Source](https://github.com/linkchecker/linkchecker/tree/9b178bdafba75334626c0c8c32a5f544cdbe7ffd) | GPL-2.0 | catalog_only |
| 115 | Simplecrawler | crawler | [Source](https://github.com/simplecrawler/simplecrawler/tree/7395d2111f1ddb62ab6478cabc540ceb0e1dc9ff) · archived | BSD-2-Clause | catalog_only |
| 116 | Osmosis | crawler | [Source](https://github.com/rchipka/node-osmosis/tree/baed7239fc5c22ea8d00a5d2dc45f97b2d64b5c5) | unverified | catalog_only |
| 117 | Norconex HTTP Collector | crawler | [Source](https://github.com/Norconex/crawler/tree/4a602f49cc39d6fa928618347abeace195c8188f) | Apache-2.0 | catalog_only |
| 118 | Apache ManifoldCF | connector_framework | [Source](https://github.com/apache/manifoldcf/tree/35b3ce683c5afda3de741b0253809f7e73c09d04) | Apache-2.0 | catalog_only |
| 119 | ArchiveBox | archiver | [Source](https://github.com/ArchiveBox/ArchiveBox/tree/09ec9e1f035ce410b4f36bb0b3b142468bc624ec) | MIT | catalog_only |
| 120 | Crawly | crawler | [Source](https://github.com/elixir-crawly/crawly/tree/89db0d6d12d7c0e97961692ce0d07989c7b988c1) | Apache-2.0 | catalog_only |

## Runtime construction in 0.4

- **Android:** searchable/exportable complete registry in Sources → Codebases; a read-only authenticated `list_codebases` MCP tool. It does not accept codebase IDs as portal IDs or execute catalogue entries.
- **Phone-native collection:** existing OkHttp + Jsoup + crawler-commons, extended with bounded JSON-LD opportunity extraction, relative Atom links, same-host sitemap discovery (one index child and one discovered detail per source/pass), quoted-query matching and provenance. No AI crawling or on-device inference.
- **Optional Python backend:** existing Scrapy + Parsel selectors now reuse **Extruct 0.18.0** and **Trafilatura 2.2.0** for structured metadata and article text from already-fetched HTML. These are real integrations, but not Android-hosted Python runtimes and not additional independent crawlers.
- **Not constructed:** automatic execution adapters for all 120 projects, a multi-language runtime farm, arbitrary deep/global crawling, challenge/login bypass, secret harvesting or security scans of opportunity sites. Catalogue inclusion is not a claim those features exist.

## Reproduction and limits

- `tools/crawlers/codebases.tsv` preserves all submitted URLs and addition identities.
- `tools/crawlers/audit_codebases.py` performs bounded GitHub metadata lookups; `overrides.json` holds explicitly sourced corrections and configuration findings. Run deliberately: it refreshes the dated evidence snapshot.
- `app/src/main/assets/searchhh-codebases.json` is shipped reference data, **not executable runtime configuration**.
- `tools/crawlers/render_audit.py` generates this report without network access.
- [Implementation plan](PLAN-0.4-CRAWLERS.md); [source/license ledger](SOURCES.md); [release validation](RELEASE-0.4.md).

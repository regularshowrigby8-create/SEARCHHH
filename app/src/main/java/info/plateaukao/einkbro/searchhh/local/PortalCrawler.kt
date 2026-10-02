package info.plateaukao.einkbro.searchhh.local

import crawlercommons.robots.BaseRobotRules
import info.plateaukao.einkbro.searchhh.Opportunity
import info.plateaukao.einkbro.searchhh.Source
import kotlinx.coroutines.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Phone-native collection; catalogue entries are not auto-executed framework binaries. */
class PortalCrawler(
    private val fetch: suspend (String, Long) -> PublicSearch.Page,
    private val now: () -> Long = { System.currentTimeMillis() },
    private val pause: suspend (Long) -> Unit = { delay(it) },
) {
    constructor(web: PublicSearch) : this({ url, limit -> web.get(url, limit) })

    private data class Robots(
        val rules: BaseRobotRules?,
        val allowed: Boolean,
        val expires: Long,
        val sitemaps: List<String>,
    )

    private val robots = mutableMapOf<String, Robots>()
    private val nextRequest = mutableMapOf<String, Long>()
    private val cursors = mutableMapOf<String, Int>()
    private val follow = mutableMapOf<String, Pair<Long, List<Opportunity>>>()
    private val maps = mutableMapOf<String, Pair<Long, List<String>>>()

    // Stop/start does not erase server-directed cooldowns.
    fun reset() {
        cursors.clear()
        follow.clear()
        maps.clear()
    }

    private fun origin(url: String) =
        url
            .toHttpUrl()
            .newBuilder()
            .encodedPath("/")
            .query(null)
            .fragment(null)
            .build()
            .toString()
            .trimEnd('/')

    private fun cooldown(
        origin: String,
        page: PublicSearch.Page,
    ) {
        if (page.code !in listOf(429, 503)) return
        val retry =
            page.retryAfter
                ?.toLongOrNull()
                ?.coerceIn(0, 604800)
                ?.times(1000)
                ?: page.retryAfter?.let {
                    runCatching {
                        ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() -
                            now()
                    }.getOrNull()
                }
                ?: 900000L
        nextRequest[origin] = now() + maxOf(retry.coerceAtMost(604800000L), 900000L)
        error("HTTP ${page.code}; provider cooldown, no bypass")
    }

    private suspend fun allowedGet(url: String): PublicSearch.Page {
        require(LocalPolicy.canonical(url) != null) { "Non-public URL rejected" }
        val origin = origin(url)
        check((nextRequest[origin] ?: now()) - now() <= 60000) { "Provider cooldown active; other portals continue" }
        val cached =
            robots[origin]?.takeIf { it.expires > now() } ?: run {
                val r = fetch("$origin/robots.txt", 512 * 1024L)
                cooldown(origin, r)
                val parsed = if (r.code == 200) SearchhhRobots.parse("$origin/robots.txt", r.text) else null
                val declared =
                    if (r.code == 200) {
                        r.text
                            .lineSequence()
                            .mapNotNull {
                                val line = it.substringBefore('#').trim()
                                if (line
                                        .substringBefore(
                                            ':',
                                        ).equals("sitemap", true)
                                ) {
                                    StructuredDiscovery.sameHostUrl(url, line.substringAfter(':').trim())
                                } else {
                                    null
                                }
                            }.distinct()
                            .take(20)
                            .toList()
                    } else {
                        emptyList()
                    }
                Robots(
                    parsed,
                    r.code in listOf(200, 404),
                    now() +
                        if (r.code in
                            listOf(200, 404)
                        ) {
                            3600000
                        } else {
                            900000
                        },
                    declared,
                ).also { robots[origin] = it }
            }
        check(cached.allowed && cached.rules?.isAllowed(url) != false) { "robots.txt disallows or is unavailable" }
        val gap = maxOf(3000L, cached.rules?.crawlDelay ?: 0)
        check(gap <= 60000) { "Requested crawl delay exceeds device budget" }
        val wait = maxOf(0, (nextRequest[origin] ?: (now() + gap)) - now())
        check(wait <= 60000) { "Provider cooldown active; other portals continue" }
        pause(wait)
        currentCoroutineContext().ensureActive()
        nextRequest[origin] = now() + gap
        val page = fetch(url, 2L * 1024 * 1024)
        cooldown(origin, page)
        check(page.code == 200) { "HTTP ${page.code}; redirects/login/challenges are not bypassed" }
        check(
            page.type.contains("html", true) ||
                page.type.contains("xml", true) ||
                page.type.contains("rss", true) ||
                page.type.contains("atom", true),
        ) { "Unsupported content (PDF/JS-only pages need browser)" }
        return page
    }

    private fun next(
        key: String,
        size: Int,
    ): Int {
        val cursor = cursors[key] ?: 0
        cursors[key] = (cursor + 1) % size
        return cursor % size
    }

    private suspend fun sitemapPages(source: Source): List<String> {
        val base = requireNotNull(source.url)
        maps[base]?.takeIf { now() - it.first < 900000 }?.let { return it.second }
        maps[base] = now() to emptyList() // also cache failures; do not hammer unsupported endpoints
        val origin = origin(base)
        val roots = robots[origin]?.sitemaps.orEmpty().ifEmpty { listOf("$origin/sitemap.xml") }
        val root = roots[next("map-root:$base", roots.size)]
        val content = allowedGet(root)
        val parsed = StructuredDiscovery.sitemap(content.text, base)
        val pages = parsed.pages.toMutableList()
        if (parsed.indexes.isNotEmpty()) {
            val child = parsed.indexes[next("map-child:$base", parsed.indexes.size)]
            // One index level only, one child per refresh. No recursive sitemap expansion.
            if (child != root) pages += StructuredDiscovery.sitemap(allowedGet(child).text, base).pages
        }
        val urls = pages.distinct().filterNot { LocalPolicy.isForm(it) || it == LocalPolicy.canonical(base) }.take(500)
        maps[base] = now() to urls
        return urls
    }

    suspend fun search(
        query: String,
        sources: List<Source>,
        mode: String,
        crawl: Boolean,
    ): PublicSearch.Batch {
        val rows = mutableListOf<Opportunity>()
        val errors = mutableListOf<String>()
        for (source in sources) {
            currentCoroutineContext().ensureActive()
            try {
                val base = requireNotNull(source.url)
                val page = allowedGet(base)
                val found = PortalParser.listings(source, page.text, query, mode)
                rows += found
                if (crawl && found.isNotEmpty()) {
                    // Read detail pages only on the selected publisher's host. External applications
                    // remain discoverable links, not unsolicited recursive crawl targets.
                    val candidates =
                        found
                            .filterNot { LocalPolicy.isForm(it.url) }
                            .filter { StructuredDiscovery.sameHostUrl(base, it.url) != null }
                    if (candidates.isNotEmpty()) {
                        val seed = candidates[next("detail:${source.id}", candidates.size)]
                        val cached = follow[seed.id]?.takeIf { now() - it.first < 900000 }
                        if (cached != null) {
                            rows += cached.second
                        } else {
                            val detail = if (seed.url == LocalPolicy.canonical(base)) page else allowedGet(seed.url)
                            val links =
                                PortalParser
                                    .page(source.copy(url = seed.url, format = "html"), detail.text, query, mode)
                                    .map { it.copy(published = it.published ?: seed.published) }
                            follow[seed.id] = now() to links
                            if (follow.size > 500) follow.remove(follow.keys.first())
                            rows += links
                        }
                    }
                } else if (crawl && source.format == "html") {
                    val pages = sitemapPages(source)
                    if (pages.isNotEmpty()) {
                        val matching = pages.filter { PortalParser.matches(query, it.replace(Regex("[-_/]"), " ")) }.ifEmpty { pages }
                        val url = matching[next("map-page:${source.id}", matching.size)]
                        rows += PortalParser.page(source.copy(url = url, format = "html"), allowedGet(url).text, query, mode)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errors += "${source.name}: ${e.message}"
            }
        }
        return PublicSearch.Batch(rows.distinctBy { it.id }, errors)
    }
}

package info.plateaukao.einkbro.searchhh.local

import crawlercommons.robots.SimpleRobotRulesParser
import crawlercommons.robots.BaseRobotRules
import info.plateaukao.einkbro.searchhh.Opportunity
import info.plateaukao.einkbro.searchhh.Source
import kotlinx.coroutines.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Bounded, polite public crawler. It never signs in, executes forms or evades limits. */
class PortalCrawler(private val web: PublicSearch) {
    private data class Robots(val rules: BaseRobotRules?, val allowed: Boolean, val expires: Long)
    private val robots = mutableMapOf<String, Robots>()
    private val nextRequest = mutableMapOf<String, Long>()
    private val cursors = mutableMapOf<String, Int>()
    private val follow = mutableMapOf<String, Pair<Long, List<Opportunity>>>()
    fun reset() { cursors.clear(); follow.clear() }
    private suspend fun allowedGet(url: String): PublicSearch.Page {
        val u = url.toHttpUrl()
        val origin = "${u.scheme}://${u.host}"
        val now = System.currentTimeMillis()
        val cached = robots[origin]?.takeIf { it.expires > now } ?: run {
            val r = web.get("$origin/robots.txt", 512 * 1024L)
            val parsed = if (r.code == 200) SimpleRobotRulesParser().parseContent("$origin/robots.txt", r.text.toByteArray(), "text/plain", "Searchhh") else null
            Robots(parsed, r.code in listOf(200, 404), now + if (r.code in listOf(200, 404)) 3600000 else 900000).also { robots[origin] = it }
        }
        check(cached.allowed && cached.rules?.isAllowed(url) != false) { "robots.txt disallows or is unavailable" }
        val gap = maxOf(3000L, cached.rules?.crawlDelay ?: 0)
        check(gap <= 60000) { "Requested crawl delay exceeds device budget" }
        val wait = maxOf(0, (nextRequest[origin] ?: (System.currentTimeMillis() + gap)) - System.currentTimeMillis())
        check(wait <= 60000) { "Provider cooldown active; other portals continue" }
        delay(wait)
        nextRequest[origin] = System.currentTimeMillis() + gap
        val page = web.get(url)
        if (page.code == 429 || page.code == 503) {
            val retry = page.retryAfter?.toLongOrNull()?.coerceIn(0, 604800)?.times(1000)
                ?: page.retryAfter?.let { runCatching { ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() - System.currentTimeMillis() }.getOrNull() }
                ?: 900000L
            nextRequest[origin] = System.currentTimeMillis() + maxOf(retry, 900000L)
            error("HTTP ${page.code}; provider cooldown, no bypass")
        }
        check(page.code == 200) { "HTTP ${page.code}; redirects/login/challenges are not bypassed" }
        check(page.type.contains("html", true) || page.type.contains("xml", true) || page.type.contains("rss", true) || page.type.contains("atom", true)) { "Unsupported content (PDF/JS-only pages need browser)" }
        return page
    }
    suspend fun search(query: String, sources: List<Source>, mode: String, crawl: Boolean): PublicSearch.Batch {
        val rows = mutableListOf<Opportunity>()
        val errors = mutableListOf<String>()
        // Sequential requests preserve per-host pacing; cancellation interrupts both waits and IO.
        for (source in sources) {
            currentCoroutineContext().ensureActive()
            try {
                val page = allowedGet(requireNotNull(source.url))
                val found = PortalParser.listings(source, page.text, query, mode)
                rows += found
                if (crawl && found.isNotEmpty()) {
                    val cursor = cursors[source.id] ?: 0
                    val candidates = found.filterNot { LocalPolicy.isForm(it.url) }
                    if (candidates.isNotEmpty()) {
                        val seed = candidates[cursor % candidates.size]
                        cursors[source.id] = cursor + 1
                        val cached = follow[seed.id]?.takeIf { System.currentTimeMillis() - it.first < 900000 }
                        if (cached != null) rows += cached.second else {
                            val detail = allowedGet(seed.url)
                            val links = PortalParser.applications(seed, detail.text, query)
                            follow[seed.id] = System.currentTimeMillis() to links
                            if (follow.size > 500) follow.remove(follow.keys.first())
                            rows += links
                        }
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { errors += "${source.name}: ${e.message}" }
        }
        return PublicSearch.Batch(rows, errors)
    }
}

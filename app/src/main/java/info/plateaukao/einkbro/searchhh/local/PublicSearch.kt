package info.plateaukao.einkbro.searchhh.local

import com.google.gson.JsonParser
import info.plateaukao.einkbro.searchhh.Opportunity
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import crawlercommons.robots.SimpleRobotRulesParser

/** Reuses OkHttp, Jsoup and SearXNG's published instance/config interfaces. */
class PublicSearch {
    val http = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false).followSslRedirects(false)
        .dns(object : Dns {
            override fun lookup(hostname: String) = Dns.SYSTEM.lookup(hostname).also { addresses ->
                if (addresses.isEmpty() || addresses.any { !LocalPolicy.publicAddress(it) }) throw UnknownHostException("Private/reserved address blocked")
            }
        }).build()
    @Volatile var instance: String? = null
        private set
    @Volatile var supported: Set<String> = emptySet()
        private set
    @Volatile var connection = "Automatic discovery has not run"
        private set
    data class Page(val code: Int, val text: String, val type: String)
    fun get(url: String, limit: Long = 2L * 1024 * 1024): Page {
        require(LocalPolicy.canonical(url) != null) { "Non-public URL rejected" }
        return http.newCall(Request.Builder().url(url).header("User-Agent", "Searchhh/0.2 (+https://github.com/regularshowrigby8-create/SEARCHHH)").build()).execute().use { response ->
            val body = response.body ?: error("Empty response")
            val input = body.source(); input.request(limit + 1)
            require(input.buffer.size <= limit) { "Response too large" }
            Page(response.code, input.readUtf8(), response.header("Content-Type").orEmpty())
        }
    }
    fun discover() {
        if (instance != null) return
        connection = "Discovering a public SearXNG service"
        val response = get("https://searx.space/data/instances.json", 8L * 1024 * 1024)
        require(response.code == 200) { "Public instance directory unavailable (HTTP ${response.code})" }
        val directory = JsonParser.parseString(response.text).asJsonObject.getAsJsonObject("instances")
        val candidates = directory.entrySet().filter { (url, item) ->
            val data = item.asJsonObject
            url.startsWith("https://") && data.get("network_type")?.takeUnless { it.isJsonNull }?.asString == "normal" &&
                data.get("http")?.takeIf { it.isJsonObject }?.asJsonObject?.get("status_code")?.takeUnless { it.isJsonNull }?.asInt == 200 &&
                data.get("analytics")?.takeUnless { it.isJsonNull }?.asBoolean != true
        }.shuffled().take(6)
        for ((url, _) in candidates) {
            try {
                val config = get(url.trimEnd('/') + "/config")
                if (config.code != 200) continue
                val json = JsonParser.parseString(config.text).asJsonObject
                val formats = json.getAsJsonArray("formats")?.map { it.asString }.orEmpty()
                if (formats.isNotEmpty() && "html" !in formats) continue
                val engines = json.getAsJsonArray("engines").map { it.asJsonObject }.filter { it.get("enabled")?.asBoolean != false }.map { it.get("name").asString }.toSet()
                if (engines.isEmpty()) continue
                instance = url.trimEnd('/'); supported = engines
                connection = "Connected to ${url.toHttpUrl().host}; ${engines.size} registry adapters reported. Live success varies."
                return
            } catch (_: Exception) { /* discovery only; never rotate after search rate limits */ }
        }
        connection = "No compatible public service discovered. Direct GitHub/Hacker News sources remain available."
    }
    data class Batch(val rows: List<Opportunity>, val errors: List<String>, val filtered: Int = 0)
    fun search(query: String, selected: List<String>, mode: String, pass: Int): Batch {
        val rows = mutableListOf<Opportunity>(); val errors = mutableListOf<String>(); var filtered = 0
        // Two direct, keyless APIs ensure the included backend is not only a proxy shell.
        selected.filter { it in listOf("github", "hackernews") }.forEach { name ->
            try {
                val url = if (name == "github") "https://api.github.com/search/repositories".toHttpUrl().newBuilder().addQueryParameter("q", query).addQueryParameter("sort", "updated").addQueryParameter("per_page", "20").build()
                    else "https://hn.algolia.com/api/v1/search_by_date".toHttpUrl().newBuilder().addQueryParameter("query", query).addQueryParameter("tags", "story").addQueryParameter("hitsPerPage", "20").build()
                val page = get(url.toString()); check(page.code == 200) { "HTTP ${page.code}; waiting before retry" }
                val json = JsonParser.parseString(page.text).asJsonObject
                val array = json.getAsJsonArray(if (name == "github") "items" else "hits")
                array.forEach { value ->
                    val r = value.asJsonObject
                    fun field(key: String) = r[key]?.takeUnless { it.isJsonNull }?.asString.orEmpty()
                    val result = LocalPolicy.result(if (name == "github") field("html_url") else field("url").ifBlank { "https://news.ycombinator.com/item?id=${field("objectID")}" }, if (name == "github") field("full_name") else field("title"), field("description").ifBlank { field("story_text") }, listOf(name), mode, field("created_at"))
                    if (result == null) filtered++ else rows += result
                }
            } catch (e: Exception) { errors += "$name: ${e.message}" }
        }
        val requested = selected.filterNot { it in listOf("github", "hackernews") }
        if (requested.isNotEmpty()) {
            try {
                discover()
                val base = instance ?: error(connection)
                val usable = requested.filter { it in supported }
                val missing = requested - usable.toSet()
                if (missing.isNotEmpty()) errors += "Not reported by this service: ${missing.joinToString()}"
                if (usable.isNotEmpty()) {
                    val intents = listOf("", " cohort application", " certification enrollment", " site:forms.gle", " site:forms.office.com", " site:docs.google.com/forms", " fellowship scholarship", " site:forms.microsoft.com")
                    val q = query + if (mode == "opportunities") intents[pass % intents.size] else ""
                    val url = "$base/search".toHttpUrl().newBuilder().addQueryParameter("q", q).addQueryParameter("engines", usable.joinToString(",")).addQueryParameter("format", "html").addQueryParameter("language", "en").build()
                    val page = get(url.toString()); check(page.code == 200) { "Public service HTTP ${page.code}. No bypass or instance rotation attempted." }
                    val doc = Jsoup.parse(page.text, base)
                    check(doc.select("#search, #search_form, form#search").isNotEmpty() || doc.select("article.result, #results").isNotEmpty()) { "Service returned an unsupported page or challenge" }
                    doc.select("article.result").take(100).forEach { article ->
                        val a = article.selectFirst("h3 a[href]") ?: return@forEach
                        val names = article.select(".engines span").eachText().ifEmpty { listOf("SearXNG (${base.toHttpUrl().host}); engine not reported") }
                        val r = LocalPolicy.result(a.absUrl("href"), a.text(), article.select(".content").text(), names, mode, article.selectFirst("time[datetime]")?.attr("datetime"))
                        if (r == null) filtered++ else rows += r
                    }
                    doc.select(".dialog-error, .engine-error").eachText().take(8).forEach { errors += it }
                }
            } catch (e: Exception) { errors += "Public metasearch: ${e.message}" }
        }
        return Batch(rows, errors, filtered)
    }
    fun crawl(seed: Opportunity, mode: String): List<Opportunity> {
        if (LocalPolicy.isForm(seed.url)) return emptyList()
        val u = seed.url.toHttpUrl(); val robots = get(u.newBuilder().encodedPath("/robots.txt").query(null).build().toString())
        if (robots.code != 200 && robots.code != 404) return emptyList()
        val rules = SimpleRobotRulesParser().parseContent(u.toString(), robots.text.toByteArray(), "text/plain", "Searchhh")
        if (robots.code == 200 && !rules.isAllowed(seed.url)) return emptyList()
        // Honor longer crawl delays by skipping, rather than shortening them.
        if (robots.code == 200 && rules.crawlDelay > 30000) return emptyList()
        Thread.sleep(maxOf(3000L, if (robots.code == 200) rules.crawlDelay else 0))
        val page = get(seed.url); if (page.code != 200) return emptyList()
        val doc = Jsoup.parse(page.text, seed.url)
        return doc.select("a[href]").take(150).mapNotNull { a -> LocalPolicy.result(a.absUrl("href"), a.text(), "Discovered on ${u.host}. Application status unverified.", listOf("Page crawler"), "opportunities") }.take(12)
    }
}

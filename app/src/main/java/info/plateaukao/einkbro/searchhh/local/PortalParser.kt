package info.plateaukao.einkbro.searchhh.local

import info.plateaukao.einkbro.searchhh.Opportunity
import info.plateaukao.einkbro.searchhh.Source
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Pure extraction glue over Jsoup; portals are publishers, not 110 search engines. */
object PortalParser {
    private val stopWords = setOf("a", "an", "the", "for", "in", "of", "and", "or", "to", "me", "find", "show", "with", "opportunity", "opportunities")
    private val phrases = Regex("\"([^\"]+)\"")
    private val application = Regex("\\b(apply|application|register|registration|enrol|enroll|submit)\\b", RegexOption.IGNORE_CASE)
    fun matches(query: String, text: String): Boolean {
        val haystack = text.lowercase()
        if (phrases.findAll(query).any { !haystack.contains(it.groupValues[1].lowercase()) }) return false
        val terms = Regex("[\\p{L}\\p{N}]+").findAll(phrases.replace(query, "").lowercase())
            .map { it.value }.filter { it !in stopWords }.toList()
        return terms.all { Regex("(?<![\\p{L}\\p{N}])${Regex.escape(it)}(?![\\p{L}\\p{N}])").containsMatchIn(haystack) }
    }
    fun listings(source: Source, text: String, query: String, mode: String): List<Opportunity> {
        val base = source.url ?: return emptyList()
        val rows = if (source.format == "feed") {
            Jsoup.parse(text, base, Parser.xmlParser()).select("item, entry").take(100).mapNotNull { entry ->
                val link = entry.selectFirst("link[href]")?.attr("href") ?: entry.selectFirst("link")?.text().orEmpty()
                val title = entry.selectFirst("title")?.text().orEmpty()
                val description = Jsoup.parse(entry.selectFirst("description, summary, content, content|encoded")?.text().orEmpty()).text().take(2000)
                val rawDate = entry.selectFirst("pubDate, published, updated")?.text()
                val date = rawDate?.let { runCatching { ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toString() }.getOrDefault(it) }
                if (!matches(query, "$title $description")) null
                else LocalPolicy.result(link, title, description, listOf(source.name), mode, date)
            }
        } else {
            val doc = Jsoup.parse(text, base)
            doc.select("script, style, nav, header, footer, form").remove()
            doc.select("a[href]").take(500).mapNotNull { a ->
                val title = a.text().trim()
                val url = a.absUrl("href")
                val context = (a.closest("article, li, .card, .post")?.text() ?: a.parent()?.text()).orEmpty().take(2000)
                if (title.length < 8 && !LocalPolicy.isForm(url)) null
                else if (!matches(query, "$title $context")) null
                else LocalPolicy.result(url, title, context, listOf(source.name), mode)
            }
        }
        return rows.distinctBy { it.id }.filter { it.url != LocalPolicy.canonical(base) }.take(30)
            .map { it.copy(evidenceUrl = base, checkedAt = Instant.now().toString()) }
    }
    fun applications(seed: Opportunity, html: String, query: String): List<Opportunity> {
        val doc = Jsoup.parse(html, seed.url)
        doc.select("script, style, nav, header, footer, form").remove()
        val evidence = doc.selectFirst("article, main") ?: doc.body()
        // Prevent unrelated application links on a portal-wide footer from becoming matches.
        if (!matches(query, "${seed.title} ${evidence.text()}")) return emptyList()
        return evidence.select("a[href]").take(300).mapNotNull { a ->
            val url = a.absUrl("href")
            if (!LocalPolicy.isForm(url) && !application.containsMatchIn(a.text())) return@mapNotNull null
            val excerpt = (a.closest("p, li, section")?.text() ?: a.parent()?.text()).orEmpty().take(1600)
            LocalPolicy.result(url, "${seed.title} — ${a.text().ifBlank { "Application" }}", excerpt,
                seed.sources + "Application link extraction", "opportunities", seed.published)
                ?.copy(evidenceUrl = seed.url, checkedAt = Instant.now().toString())
        }.distinctBy { it.id }.filter { it.id != seed.id }.take(12)
    }
}

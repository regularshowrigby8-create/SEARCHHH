package info.plateaukao.einkbro.searchhh.local

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import info.plateaukao.einkbro.searchhh.Opportunity
import info.plateaukao.einkbro.searchhh.Source
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.time.Instant
import java.util.ArrayDeque

/** Bounded extraction glue over existing Jsoup/Gson; never executes scripts or follows JSON-LD contexts. */
object StructuredDiscovery {
    data class Sitemap(val pages: List<String>, val indexes: List<String>)
    private val types = setOf("JobPosting", "Course", "CourseInstance", "EducationalOccupationalProgram", "Event", "EducationEvent")

    fun sameHostUrl(base: String, raw: String): String? {
        val origin = base.toHttpUrlOrNull() ?: return null
        val target = origin.resolve(raw.trim()) ?: return null
        if (target.host != origin.host || (origin.scheme == "https" && target.scheme != "https")) return null
        return LocalPolicy.canonical(target.toString())
    }

    fun sitemap(xml: String, base: String): Sitemap {
        val doc = Jsoup.parse(xml, base, Parser.xmlParser())
        fun urls(selector: String) = doc.select(selector).take(500).mapNotNull {
            sameHostUrl(base, it.text())
        }.distinct()
        return Sitemap(urls("urlset > url > loc"), urls("sitemapindex > sitemap > loc").take(20))
    }

    // Bound nesting before handing untrusted input to a JSON parser, including deeply nested arrays.
    private fun boundedJson(text: String): Boolean {
        if (text.length > 256 * 1024) return false
        var depth = 0; var quoted = false; var escaped = false
        for (c in text) {
            if (quoted) {
                if (escaped) escaped = false else if (c == '\\') escaped = true else if (c == '"') quoted = false
            } else when (c) {
                '"' -> quoted = true
                '{', '[' -> { depth++; if (depth > 32) return false }
                '}', ']' -> depth--
            }
        }
        return !quoted && depth == 0
    }

    fun opportunities(source: Source, html: String, query: String, mode: String): List<Opportunity> {
        val base = source.url ?: return emptyList()
        val doc = Jsoup.parse(html, base)
        val rows = mutableListOf<Opportunity>()
        val queue = ArrayDeque<JsonElement>()
        doc.select("script[type=application/ld+json]").take(20).forEach { script ->
            val text = script.data()
            if (boundedJson(text)) runCatching { JsonParser.parseString(text) }.getOrNull()?.let(queue::addLast)
        }
        var visited = 0
        while (queue.isNotEmpty() && visited++ < 300 && rows.size < 30) {
            val node = queue.removeFirst()
            if (node.isJsonArray) { node.asJsonArray.take(100).forEach(queue::addLast); continue }
            if (!node.isJsonObject) continue
            val obj = node.asJsonObject
            fun field(key: String): String? = obj[key]?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
            val type = obj["@type"]?.let { if (it.isJsonArray) it.asJsonArray.toList() else listOf(it) }
                ?.filter { it.isJsonPrimitive && it.asJsonPrimitive.isString }
                ?.map { it.asString.substringAfterLast('/').substringAfterLast('#') }?.firstOrNull { it in types }
            if (type != null) {
                val title = (field("name") ?: field("title")).orEmpty()
                val description = Jsoup.parse(field("description").orEmpty()).text().take(1500)
                // Field names retain semantics: an event endDate is not an application deadline.
                val dates = listOf("validThrough", "startDate", "endDate").mapNotNull { key -> field(key)?.take(100)?.let { "$key: $it" } }.joinToString("; ")
                val rawUrl = field("url") ?: obj["url"]?.takeIf { it.isJsonObject }?.asJsonObject?.get("@id")?.takeIf { it.isJsonPrimitive }?.asString ?: base
                val url = base.toHttpUrlOrNull()?.resolve(rawUrl)?.toString()
                if (url != null && title.isNotBlank() && PortalParser.matches(query, "$title $description")) {
                    LocalPolicy.result(url, title, "$description $dates".trim(), listOf(source.name, "JSON-LD $type"), mode,
                        field("datePosted") ?: field("datePublished"))?.let {
                        rows += it.copy(evidenceUrl = base, checkedAt = Instant.now().toString())
                    }
                }
            }
            // Only traverse known containers; do not mistake arbitrary nested metadata for pages.
            for (key in listOf("@graph", "mainEntity", "itemListElement", "item", "hasCourseInstance"))
                obj[key]?.takeIf { it.isJsonObject || it.isJsonArray }?.let(queue::addLast)
        }
        return rows.distinctBy { it.id }
    }
}

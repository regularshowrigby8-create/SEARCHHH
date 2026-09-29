package info.plateaukao.einkbro.searchhh.local

import info.plateaukao.einkbro.searchhh.Opportunity
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.InetAddress
import java.security.MessageDigest
import java.time.Instant
import org.jsoup.Jsoup

object LocalPolicy {
    private val signals = Regex("\\b(cohort|fellowship|bootcamp|scholarship|certification|certificate|applications?|enroll|enrol|register|opportunit\\w*)\\b", RegexOption.IGNORE_CASE)
    private val bots = Regex("captcha|access denied|verify you are human|robot check|just a moment", RegexOption.IGNORE_CASE)
    fun publicAddress(address: InetAddress): Boolean {
        val b = address.address.map { it.toInt() and 255 }
        if (address.isAnyLocalAddress || address.isLoopbackAddress || address.isLinkLocalAddress || address.isSiteLocalAddress || address.isMulticastAddress) return false
        if (b.size == 4) {
            return !(b[0] == 0 || b[0] >= 224 ||
                (b[0] == 100 && b[1] in 64..127) ||
                (b[0] == 192 && b[1] == 0 && b[2] in listOf(0, 2)) ||
                (b[0] == 192 && b[1] == 88 && b[2] == 99) ||
                (b[0] == 198 && b[1] in 18..19) ||
                (b[0] == 198 && b[1] == 51 && b[2] == 100) ||
                (b[0] == 203 && b[1] == 0 && b[2] == 113))
        }
        // Global unicast only; exclude documentation and transition/tunnel ranges.
        return b.size == 16 && (b[0] and 0xe0) == 0x20 &&
            !(b[0] == 0x20 && b[1] == 0x02) &&
            !(b[0] == 0x20 && b[1] == 0x01 && ((b[2] == 0 && b[3] == 0) || (b[2] == 0x0d && b[3] == 0xb8)))
    }
    fun canonical(raw: String): String? {
        val u = raw.toHttpUrlOrNull() ?: return null
        if (u.username.isNotEmpty() || u.password.isNotEmpty() || u.port !in listOf(80, 443)) return null
        val h = u.host.trimEnd('.')
        if (h == "localhost" || h.endsWith(".local") || h.endsWith(".internal") || h.endsWith(".onion") || h.endsWith(".bot")) return null
        if (h.contains(':') || h.matches(Regex("[0-9.]+"))) {
            if (!runCatching { publicAddress(InetAddress.getByName(h)) }.getOrDefault(false)) return null
        }
        val out = u.newBuilder().fragment(null)
        u.queryParameterNames.filter { it.startsWith("utm_", true) || it.lowercase() in listOf("fbclid", "gclid", "msclkid") }.forEach(out::removeAllQueryParameters)
        // Normalize query order without losing repeated form-entry parameters.
        val pairs = (0 until out.build().querySize).map { out.build().queryParameterName(it) to out.build().queryParameterValue(it) }.sortedWith(compareBy({ it.first }, { it.second.orEmpty() }))
        out.query(null); pairs.forEach { out.addQueryParameter(it.first, it.second) }
        return out.build().toString()
    }
    fun isForm(raw: String): Boolean {
        val u = raw.toHttpUrlOrNull() ?: return false
        return u.host in listOf("forms.gle", "forms.office.com", "forms.microsoft.com") || (u.host == "docs.google.com" && u.encodedPath.startsWith("/forms"))
    }
    fun result(url: String, title: String, description: String, sources: List<String>, mode: String, date: String? = null): Opportunity? {
        val clean = canonical(url) ?: return null
        val t = Jsoup.parse(title).text().take(500)
        val text = Jsoup.parse(description).text().take(2000)
        if (bots.containsMatchIn(t)) return null
        val hits = signals.findAll("$t $text").map { it.value.lowercase() }.toSet()
        if (mode == "opportunities" && !isForm(clean) && hits.isEmpty()) return null
        val kind = when {
            isForm(clean) -> "Application form"
            hits.any { it in listOf("cohort", "bootcamp", "fellowship") } -> "Cohort"
            hits.any { it in listOf("certificate", "certification") } -> "Certification"
            hits.isNotEmpty() -> "Opportunity"
            else -> "Link"
        }
        val now = Instant.now()
        val published = runCatching { Instant.parse(date).takeIf { !it.isAfter(now) }?.toString() }.getOrNull()
        val id = MessageDigest.getInstance("SHA-256").digest(clean.toByteArray()).joinToString("") { "%02x".format(it) }
        return Opportunity(id, clean, t.ifBlank { clean }, text, kind, sources.distinct(), published, now.toString(), ((if (isForm(clean)) 50 else 20) + hits.size * 10).coerceAtMost(100), false)
    }
}

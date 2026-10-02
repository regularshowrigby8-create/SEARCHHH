package info.plateaukao.einkbro.searchhh.local

import com.google.gson.JsonParser

/** Parse only structured forwarding events, never URLs in SSH banners/help text.
 * Event fields documented by https://github.com/erlef/localhost-run (existing client).
 */
object RelayEvent {
    fun httpsUrl(line: String): String? =
        runCatching {
            val event = JsonParser.parseString(line).asJsonObject
            if (event["event"]?.asString != "tcpip-forward") return null
            val address = event["address"]?.asString ?: return null
            val host =
                Regex(
                    "(?:https?://)?([a-zA-Z0-9-]+\\.(?:lhr\\.life|localhost\\.run))(?::(?:80|443))?/?",
                ).matchEntire(address)?.groupValues?.get(1)
                    ?: return null
            "https://${host.lowercase()}"
        }.getOrNull()
}

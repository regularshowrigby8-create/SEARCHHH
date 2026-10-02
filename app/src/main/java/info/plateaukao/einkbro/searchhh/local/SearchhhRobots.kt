package info.plateaukao.einkbro.searchhh.local

import crawlercommons.robots.SimpleRobotRules
import crawlercommons.robots.SimpleRobotRulesParser

internal object SearchhhRobots {
    fun parse(
        url: String,
        content: String,
    ): SimpleRobotRules {
        // Preserve the former String overload's matching mode while using the current API.
        val parser = SimpleRobotRulesParser().apply { setExactUserAgentMatching(false) }
        return parser.parseContent(url, content.toByteArray(Charsets.UTF_8), "text/plain", listOf("searchhh"))
    }
}

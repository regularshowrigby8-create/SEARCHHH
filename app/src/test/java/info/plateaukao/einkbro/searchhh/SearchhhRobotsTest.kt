package info.plateaukao.einkbro.searchhh

import info.plateaukao.einkbro.searchhh.local.SearchhhRobots
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchhhRobotsTest {
    @Test
    fun namedAgentMatchingKeepsCaseAndLegacyPrefixSemantics() {
        val rules =
            SearchhhRobots.parse(
                "https://example.org/robots.txt",
                "User-agent: SeArCh\nDisallow: /private\nAllow: /private/public\n\nUser-agent: *\nAllow: /",
            )
        assertFalse(rules.isAllowed("https://example.org/private/data"))
        assertTrue(rules.isAllowed("https://example.org/private/public"))
    }

    @Test
    fun wildcardDelayAndSitemapsRemainAvailable() {
        val rules =
            SearchhhRobots.parse(
                "https://example.org/robots.txt",
                "User-agent: *\nDisallow: /blocked\nCrawl-delay: 7\nSitemap: https://example.org/map.xml",
            )
        assertFalse(rules.isAllowed("https://example.org/blocked"))
        assertTrue(rules.isAllowed("https://example.org/open"))
        assertEquals(7000L, rules.crawlDelay)
        assertEquals(listOf("https://example.org/map.xml"), rules.sitemaps)
    }
}

package info.plateaukao.einkbro.searchhh

import info.plateaukao.einkbro.searchhh.local.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class CrawlerExpansionTest {
    private val source = Source("fixture", "Fixture", "Global", url = "https://example.org/list", format = "html")
    private val ld = """<script type="application/ld+json">{"@context":"https://context.invalid/never-fetch",
      "@graph":[{"@type":"EducationalOccupationalProgram","name":"Climate fellowship",
      "description":"Students can apply for this fellowship.","url":"/apply","validThrough":"2027-01-01","datePublished":"2025-01-01"},
      {"@type":"Course","name":"Climate fellowship private","url":"http://127.0.0.1/private"}]}</script>"""

    private fun page(
        text: String,
        type: String = "text/html",
        code: Int = 200,
        retry: String? = null,
    ) = PublicSearch.Page(code, text, type, retry)

    @Test fun jsonLdUsesKnownTypesKeepsEvidenceAndNeverMarksVerified() {
        val rows = StructuredDiscovery.opportunities(source, ld, "climate fellowship", "opportunities")
        assertEquals(1, rows.size)
        assertEquals("https://example.org/apply", rows.single().url)
        assertEquals(source.url, rows.single().evidenceUrl)
        assertTrue(rows.single().description.contains("validThrough: 2027-01-01"))
        assertFalse(rows.single().verified)
        assertEquals("2025-01-01", rows.single().published)
    }

    @Test fun structuredSelfIsNotDiscardedAsNavigation() {
        val rows = PortalParser.listings(source, ld.replace("/apply", "/list"), "climate fellowship", "opportunities")
        assertEquals(source.url, rows.single().url)
        assertTrue(rows.single().sources.any { it.startsWith("JSON-LD") })
    }

    @Test fun invalidAndDeepJsonDoNotSuppressOtherScripts() {
        val deep = "<script type=\"application/ld+json\">" + "[".repeat(50) + "0" + "]".repeat(50) + "</script>"
        val broken = "<script type=\"application/ld+json\">{broken}</script>"
        assertEquals(1, StructuredDiscovery.opportunities(source, deep + broken + ld, "climate", "opportunities").size)
    }

    @Test fun sitemapRestrictsHostsSchemesAndCredentials() {
        val xml = """<urlset><url><loc>https://example.org/fellowship</loc></url>
          <url><loc>https://external.org/fellowship</loc></url><url><loc>http://example.org/downgrade</loc></url>
          <url><loc>https://user:secret@example.org/private</loc></url><url><loc>http://127.0.0.1/admin</loc></url></urlset>"""
        assertEquals(listOf("https://example.org/fellowship"), StructuredDiscovery.sitemap(xml, source.url!!).pages)
        assertTrue(StructuredDiscovery.sitemap(xml, source.url!!).indexes.isEmpty())
    }

    @Test fun atomRelativeLinkResolvesAgainstPublisher() {
        val xml = """<feed><entry><title>Climate fellowship</title><link href="/apply"/><summary>Students apply</summary></entry></feed>"""
        assertEquals(
            "https://example.org/apply",
            PortalParser.listings(source.copy(format = "feed"), xml, "climate fellowship", "opportunities").single().url,
        )
    }

    @Test fun detailFindsApplicationsWithWholePageContext() {
        val html = """<main><h1>Climate fellowship</h1><p>Students can apply.</p><a href="https://forms.cloud.microsoft/r/abc">Apply here</a></main>"""
        val rows = PortalParser.page(source, html, "climate fellowship", "opportunities")
        assertTrue(rows.any { LocalPolicy.isForm(it.url) })
        assertTrue(rows.all { it.evidenceUrl == source.url && !it.verified })
    }

    @Test fun robotsDenyPreventsPageRetrieval(): Unit =
        runBlocking {
            val calls = mutableListOf<String>()
            val crawler =
                PortalCrawler(fetch = { url, _ ->
                    calls += url
                    page("User-agent: *\nDisallow: /", "text/plain")
                }, pause = {})
            val batch = crawler.search("climate", listOf(source), "opportunities", true)
            assertTrue(batch.rows.isEmpty())
            assertEquals(1, batch.errors.size)
            assertEquals(listOf("https://example.org/robots.txt"), calls)
        }

    @Test fun robotsRateLimitSurvivesReset(): Unit =
        runBlocking {
            var count = 0
            val crawler =
                PortalCrawler(fetch = { _, _ ->
                    count++
                    page("", code = 429, retry = "3600")
                }, now = { 10000L }, pause = {})
            crawler.search("climate", listOf(source), "opportunities", true)
            crawler.reset()
            crawler.search("climate", listOf(source), "opportunities", true)
            assertEquals(1, count)
        }

    @Test fun pageRateLimitStopsOnlyThatOriginAndSurvivesReset(): Unit =
        runBlocking {
            var clock = 10000L
            val calls = mutableListOf<String>()
            val crawler =
                PortalCrawler(fetch = { url, _ ->
                    calls += url
                    if (url.endsWith("robots.txt")) {
                        page("", code = 404)
                    } else if (url.startsWith("https://example.org")) {
                        page("", code = 429, retry = "3600")
                    } else {
                        page(ld)
                    }
                }, now = { clock }, pause = { clock += it })
            crawler.search("climate", listOf(source), "opportunities", false)
            crawler.reset()
            val result =
                crawler.search(
                    "climate",
                    listOf(source, source.copy(id = "other", url = "https://other.org/list")),
                    "opportunities",
                    false,
                )
            assertEquals(2, calls.count { it.startsWith("https://example.org") })
            assertTrue(result.rows.isNotEmpty())
        }

    @Test fun sitemapFallbackFetchesOneChildAndOnePageNotExternalLinks(): Unit =
        runBlocking {
            var clock = 10000L
            val calls = mutableListOf<String>()
            val responses =
                mapOf(
                    "https://example.org/robots.txt" to
                        page(
                            "User-agent: *\nAllow: /\nSitemap: https://example.org/maps.xml",
                            "text/plain",
                        ),
                    source.url!! to page("<html>No listing links</html>"),
                    "https://example.org/maps.xml" to
                        page("<sitemapindex><sitemap><loc>https://example.org/child.xml</loc></sitemap></sitemapindex>", "application/xml"),
                    "https://example.org/child.xml" to
                        page(
                            "<urlset><url><loc>https://example.org/climate-fellowship</loc></url><url><loc>https://external.org/unwanted</loc></url></urlset>",
                            "application/xml",
                        ),
                    "https://example.org/climate-fellowship" to page(ld),
                )
            val crawler =
                PortalCrawler(fetch = { url, _ ->
                    calls += url
                    responses.getValue(url)
                }, now = { clock }, pause = { clock += it })
            val result = crawler.search("climate fellowship", listOf(source), "opportunities", true)
            assertEquals(emptyList<String>(), result.errors)
            assertEquals(5, calls.size)
            assertTrue(result.rows.any { it.evidenceUrl == "https://example.org/climate-fellowship" })
            assertFalse(calls.any { it.contains("external.org") || it.contains("context.invalid") })
        }

    @Test fun disabledDetailModeDoesNotFetchSitemaps(): Unit =
        runBlocking {
            val calls = mutableListOf<String>()
            val crawler =
                PortalCrawler(fetch = {
                    url,
                    _,
                    ->
                    calls += url
                    if (url.endsWith("robots.txt")) page("", code = 404) else page("<html>Empty</html>")
                }, pause = {})
            crawler.search("climate", listOf(source), "opportunities", false)
            assertEquals(2, calls.size)
        }

    @Test fun selectedCapabilityProfileChangesTheRealExtractionPath(): Unit =
        runBlocking {
            val structured =
                PortalCrawler(
                    fetch = { url, _ ->
                        if (url.endsWith("robots.txt")) page("", code = 404) else page(ld)
                    },
                    pause = {},
                ).search("climate fellowship", listOf(source), "opportunities", false, CrawlerAdapters.PHONE_STRUCTURED)
            assertEquals(1, structured.rows.size)
            assertTrue(
                structured.rows
                    .single()
                    .sources
                    .contains("Extruct profile"),
            )

            val article =
                PortalCrawler(
                    fetch = { url, _ ->
                        if (url.endsWith(
                                "robots.txt",
                            )
                        ) {
                            page(
                                "",
                                code = 404,
                            )
                        } else {
                            page("<main><h1>Climate fellowship</h1><p>Students can apply for a fellowship.</p></main>")
                        }
                    },
                    pause = {},
                ).search("climate fellowship", listOf(source), "opportunities", false, CrawlerAdapters.PHONE_ARTICLE)
            assertTrue(article.rows.any { it.title.contains("Climate fellowship") })
            assertTrue(article.rows.all { it.sources.contains("Trafilatura profile") })
        }

    @Test fun cancellationDuringPolitenessWaitDoesNotFetchPage(): Unit =
        runBlocking {
            val waiting = CompletableDeferred<Unit>()
            val calls = mutableListOf<String>()
            val crawler =
                PortalCrawler(fetch = {
                    url,
                    _,
                    ->
                    calls += url
                    page("", code = 404)
                }, pause = {
                    waiting.complete(Unit)
                    delay(Long.MAX_VALUE)
                })
            val task = launch { crawler.search("climate", listOf(source), "opportunities", true) }
            withTimeout(2000) {
                waiting.await()
                task.cancelAndJoin()
            }
            assertEquals(listOf("https://example.org/robots.txt"), calls)
        }
}

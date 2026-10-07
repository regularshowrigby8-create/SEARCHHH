package info.plateaukao.einkbro.searchhh

import info.plateaukao.einkbro.searchhh.local.CodebaseEntry
import info.plateaukao.einkbro.searchhh.local.CrawlerAdapters
import info.plateaukao.einkbro.searchhh.web.SearchhhResultsHtml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchhhCrawlerAndHtmlTest {
    @Test
    fun reviewedCodebasesMapToRealAndroidCapabilities() {
        val entries =
            listOf(1, 65, 66, 67).map { number ->
                CodebaseEntry(
                    number = number,
                    original = true,
                    name = "entry-$number",
                    submittedUrl = "https://example.org/$number",
                    submittedLanguage = "Python",
                    reportedLanguage = "Python",
                    category = "crawler",
                    canonicalUrl = "https://example.org/$number",
                    sourceUrl = "https://example.org/$number",
                    evidenceUrl = null,
                    repositoryStatus = "verified",
                    license = "MIT",
                    archived = false,
                    revision = null,
                    integration = "catalog_only",
                    executionTarget = "android",
                    description = "description",
                    note = null,
                    configurationSupport = emptyMap(),
                )
            }

        entries.forEach { assertNotNull(CrawlerAdapters.forEntry(it)) }
        assertEquals(4, CrawlerAdapters.available.size)
        assertTrue(CrawlerAdapters.available.all { it.implementation.startsWith("Android") })
    }

    @Test
    fun resultDocumentEscapesContentAndPreservesCrawlerIdentity() {
        val result =
            Opportunity(
                id = "a".repeat(64),
                url = "https://example.org/apply?id=1",
                title = "<Apply & learn>",
                description = "A fellowship for <builders>",
                kind = "Opportunity",
                sources = listOf("Portal", "Extruct profile"),
                published = "2026-01-02T00:00:00Z",
                discovered = "2026-01-03T00:00:00Z",
                score = 80,
                verified = false,
            )
        val html =
            SearchhhResultsHtml.render(
                JobStatus("job", "running", 1, 2, 0, emptyList(), listOf(result), "phone-structured"),
                emptySet(),
            )

        assertTrue(html.contains("&lt;Apply &amp; learn&gt;"))
        assertFalse(html.contains("<Apply & learn>"))
        assertTrue(html.contains("Extruct profile"))
        assertTrue(html.contains("Crawler profile: phone-structured"))
        assertTrue(html.contains("searchhh://save/${result.id}"))
        assertTrue(html.contains("data-result-id=\"${result.id}\""))
    }
}

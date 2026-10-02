package info.plateaukao.einkbro.searchhh

import com.google.gson.JsonParser
import info.plateaukao.einkbro.searchhh.ai.AiPolicy
import info.plateaukao.einkbro.searchhh.local.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.*
import org.junit.Test

class OpportunityPipelineTest {
    private val portal = Source("test", "Global portal", "Fellowships", true, "https://example.org/feed/", "feed")

    private fun row() =
        LocalPolicy.result(
            "https://example.org/fellowship",
            "AI fellowship",
            "Applications close 30 October 2026. Open to students.",
            listOf("test"),
            "opportunities",
        )!!

    @Test fun recognizesNewMicrosoftHostWithoutAcceptingLookalikes() {
        assertTrue(LocalPolicy.isForm("https://forms.cloud.microsoft/r/ABC"))
        assertTrue(LocalPolicy.isForm("https://docs.google.com/forms/d/ABC/viewform"))
        assertFalse(LocalPolicy.isForm("https://forms.cloud.microsoft.evil.example/r/ABC"))
        assertFalse(LocalPolicy.isForm("https://docs.google.com/forms-evil"))
    }

    @Test fun matchesWholeWordsAndQuotedPhrasesNotLocationDefaults() {
        assertTrue(PortalParser.matches("AI fellowship", "AI fellowship open worldwide"))
        assertFalse(PortalParser.matches("AI fellowship", "Paid fellowship"))
        assertFalse(PortalParser.matches("\"climate science\" fellowship", "Science and climate fellowship"))
        assertTrue(PortalParser.matches("\"climate science\" fellowship", "Climate science fellowship in Japan"))
        assertTrue(PortalParser.matches("研究", "研究 fellowship"))
    }

    @Test fun readsRssWithProvenanceAndRejectsIrrelevantItems() {
        val xml = """<rss><channel>
          <item><title>AI fellowship</title><link>https://example.org/apply?utm_source=feed</link><description><![CDATA[<p>Students can apply</p>]]></description><pubDate>Mon, 28 Sep 2026 10:00:00 +0000</pubDate></item>
          <item><title>Music fellowship</title><link>https://example.org/music</link></item>
        </channel></rss>"""
        val rows = PortalParser.listings(portal, xml, "AI fellowship", "opportunities")
        assertEquals(1, rows.size)
        assertEquals("https://example.org/apply", rows.single().url)
        assertEquals(portal.url, rows.single().evidenceUrl)
        assertNotNull(rows.single().checkedAt)
        assertFalse(rows.single().verified)
    }

    @Test fun extractsApplicationsNotFormsToSubmitOrFooterNoise() {
        val html = """<main><h1>AI fellowship</h1><p>Apply at <a href="https://forms.cloud.microsoft/r/abc">this form</a></p>
          <a href="https://forms.gle/ABC">Apply now</a><a href="http://127.0.0.1/apply">Apply</a>
          <form action="/submit"><input name="email"/></form></main>
          <footer><a href="https://forms.gle/unrelated">Newsletter</a></footer>"""
        val rows = PortalParser.applications(row(), html, "AI fellowship")
        assertEquals(2, rows.size)
        assertTrue(rows.all { it.evidenceUrl == row().url && !it.verified })
        assertTrue(rows.all { it.kind == "Application form" })
    }

    @Test fun freeModelFilterRejectsPaidMissingPricesAndDuplicateNames() {
        fun model(
            id: String,
            pricing: String,
        ) = """{"id":"$id","pricing":$pricing,"architecture":{"output_modalities":["text"]}}"""
        val zero = """{"prompt":"0","completion":"0","request":"0"}"""
        val json = """{"data":[${listOf(
            model("a:free",zero),
            model("a:free",zero),
            model("paid",zero),
            model("b:free","""{"prompt":"0","completion":"0.1"}"""),
            model("c:free","{}"),
            model("d:free","""{"prompt":"0","completion":"0","request":"1"}"""),
            model("e:free","""{"prompt":"0","completion":"-1"}"""),
        ).joinToString(",") }]}"""
        assertEquals(listOf("a:free"), AiPolicy.freeOpenRouterModels(json))
    }

    @Test fun reviewerHasNoToolsNoKeysNoInventedLinksAndNoPaidFallback() {
        val json = JsonParser.parseString(AiPolicy.request("a:free", "fellowship", listOf(row()), true)).asJsonObject
        assertFalse(json.has("tools"))
        assertFalse(json.has("api_key"))
        assertFalse(json.getAsJsonObject("provider")["allow_fallbacks"].asBoolean)
        assertEquals(0, json.getAsJsonObject("provider").getAsJsonObject("max_price")["completion"].asInt)
    }

    @Test fun groundedReviewRejectsUnknownIdsUnsupportedQuotesAndInvalidScores() {
        val row = row()

        fun output(
            id: String = row.id,
            quote: String = "AI fellowship",
            score: String = "88",
        ) =
            """{"reviews":[{"id":"$id","relevance":$score,"summary":"Relevant topic; confirm eligibility.","quote":"$quote","eligibilityQuote":"Open to students.","deadlineQuote":"30 October 2026"}]}"""
        assertEquals(88, AiPolicy.parseReview(output(), listOf(row), "test")[row.id]!!.relevance)
        listOf(output(id = "made-up"), output(quote = "Open to everybody"), output(score = "101"), output(score = "3.2")).forEach { bad ->
            assertTrue(runCatching { AiPolicy.parseReview(bad, listOf(row), "test") }.isFailure)
        }
    }

    @Test fun cancelledHttpDoesNotWaitForProviderTimeout(): Unit =
        runBlocking {
            val server = MockWebServer()
            val client = OkHttpClient()
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            server.start()
            try {
                val call = client.newCall(Request.Builder().url(server.url("/slow")).build())
                val job = launch { call.awaitPage() }
                yield()
                withTimeout(2000) { job.cancelAndJoin() }
                assertTrue(call.isCanceled())
            } finally {
                client.dispatcher.cancelAll()
                server.shutdown()
                client.dispatcher.executorService.shutdown()
            }
        }

    @Test fun oversizedResponsesFailClosed(): Unit =
        runBlocking {
            val server = MockWebServer()
            server.enqueue(MockResponse().setBody("x".repeat(2000)))
            server.start()
            val client = OkHttpClient()
            try {
                assertTrue(runCatching { client.newCall(Request.Builder().url(server.url("/big")).build()).awaitPage(100) }.isFailure)
            } finally {
                client.dispatcher.cancelAll()
                server.shutdown()
                client.dispatcher.executorService.shutdown()
            }
        }
}

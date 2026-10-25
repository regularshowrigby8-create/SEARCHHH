package com.searchhh.app.data

import com.searchhh.app.data.remote.SearchApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Contract test against a real HTTP server (MockWebServer): proves the Retrofit
 * interface + kotlinx converters agree with the FastAPI response shape.
 */
class SearchApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: SearchApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SearchApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search sends filters and parses results`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {"query":"coffee","total":1,"took_ms":1.7,"source":"memory",
                     "results":[{"id":"svc-001","title":"Campus Coffee Run","subtitle":"Delivery",
                     "description":"Barista coffee.","category":"food","tags":["coffee"],
                     "price":28.0,"currency":"ZAR","rating":4.7,"score":0.8,"highlights":[]}],
                     "suggestions":[]}
                    """.trimIndent(),
                ),
        )

        val response = api.search(query = "coffee", limit = 5, offset = 0, category = "food", sort = "rating")

        assertEquals("coffee", response.query)
        assertEquals("svc-001", response.results.first().id)
        val request = server.takeRequest()
        val url = request.requestUrl!!
        assertEquals("/api/v1/search", url.encodedPath)
        assertEquals("coffee", url.queryParameter("q"))
        assertEquals("food", url.queryParameter("category"))
        assertEquals("rating", url.queryParameter("sort"))
        assertEquals("5", url.queryParameter("limit"))
    }

    @Test
    fun `suggest parses suggestions`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"query":"cof","suggestions":["coffee","coffee beans"]}"""),
        )
        val response = api.suggest("cof")
        assertEquals(listOf("coffee", "coffee beans"), response.suggestions)
    }

    @Test
    fun `health parses snake case fields`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """{"status":"ok","service":"SEARCHHH API","version":"0.1.0","env":"ci",
                       "git_sha":"abc123","uptime_s":12.5,"checks":{"repository":"ok"}}""",
                ),
        )
        val health = api.health()
        assertEquals("ok", health.status)
        assertEquals("abc123", health.gitSha)
        assertTrue(health.checks.containsKey("repository"))
    }
}

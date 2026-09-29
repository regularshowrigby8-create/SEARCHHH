package info.plateaukao.einkbro.searchhh

import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import info.plateaukao.einkbro.searchhh.local.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class InternalBackendTest {
    @Test fun initializesFromNameAndServesAuthenticatedMcp(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ConnectionSettings.prefs(context).edit().putBoolean("relay_enabled", false).putBoolean("server_enabled", true).apply()
        val identity = LocalIdentity.create(context, "Test user")
        val renamed = LocalIdentity.create(context, "Same device new name")
        assertEquals(identity.id, renamed.id); assertEquals(identity.token, renamed.token)
        assertEquals(64, identity.token.length); assertNotEquals(identity.token, LocalIdentity.randomToken())
        ActivityScenario.launch(SearchhhActivity::class.java).use {
            withTimeout(40000) { while (InternalBackendService.localPort == 0) delay(100) }
            val base = "http://127.0.0.1:${InternalBackendService.localPort}"
            val client = OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS).build()
            fun get(path: String, token: String? = null, origin: Boolean = false): Response {
                val req = Request.Builder().url(base + path)
                if (path == "/mcp") req.header("Accept", "text/event-stream")
                if (token != null) req.header("Authorization", "Bearer $token")
                if (origin) req.header("Origin", "https://attacker.example")
                return client.newCall(req.build()).execute()
            }
            get("/health").use { r -> assertEquals(401, r.code) }
            get("/health", identity.token, true).use { r -> assertEquals(403, r.code) }
            get("/health", identity.token).use { r ->
                assertEquals(200, r.code)
                val body = JsonParser.parseString(r.body!!.string()).asJsonObject
                assertEquals(128, body["catalogSources"].asInt)
                assertEquals(identity.id, body["installationId"].asString)
                assertFalse(body.has("token"))
            }
            get("/mcp", identity.token).use { sse ->
                assertEquals(200, sse.code)
                val stream = sse.body!!.source()
                fun event(): String {
                    var data = ""
                    while (true) {
                        val line = stream.readUtf8Line() ?: error("SSE closed")
                        if (line.startsWith("data:")) data += line.removePrefix("data:").trim()
                        if (line.isEmpty() && data.isNotEmpty()) return data
                    }
                }
                val endpoint = event()
                val postUrl = base + (if (endpoint.startsWith("?")) "/mcp$endpoint" else endpoint)
                fun post(body: String, token: String = identity.token): Int = client.newCall(Request.Builder().url(postUrl).header("Authorization", "Bearer $token").post(body.toByteArray().toRequestBody("application/json".toMediaType())).build()).execute().use { it.code }
                assertEquals(202, post("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"device-test","version":"1"}}}"""))
                assertTrue(event().contains("serverInfo"))
                assertEquals(202, post("""{"jsonrpc":"2.0","method":"notifications/initialized"}"""))
                assertEquals(202, post("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}"""))
                val tools = JsonParser.parseString(event()).asJsonObject.getAsJsonObject("result").getAsJsonArray("tools")
                assertEquals(7, tools.size())
                assertEquals(202, post("""{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"server_status","arguments":{}}}"""))
                val response = event(); assertTrue(response.contains(identity.id)); assertFalse(response.contains(identity.token))
                LocalIdentity.rotate(context)
                assertEquals(401, post("""{"jsonrpc":"2.0","id":4,"method":"ping"}"""))
            }
            val backend = LocalBackend.get(context)
            assertEquals(128, backend.sources().size)
            val search = backend.start(StartRequest("cohort", "links", listOf("github"), false))
            backend.stop(search.id)
            assertEquals("stopped", backend.status(search.id).status)
            val count = backend.status(search.id).results.size
            delay(1000)
            assertEquals(count, backend.status(search.id).results.size)
            client.dispatcher.executorService.shutdown(); client.connectionPool.evictAll()
            InternalBackendService.stop(context)
        }
    }
}

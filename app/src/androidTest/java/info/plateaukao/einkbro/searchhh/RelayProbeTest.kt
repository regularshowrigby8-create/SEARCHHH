package info.plateaukao.einkbro.searchhh

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.plateaukao.einkbro.searchhh.local.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** External-service diagnostic, deliberately not a deterministic release gate.
 * Local protocol/security tests are gated separately. Always records actual availability.
 */
@RunWith(AndroidJUnit4::class)
class RelayProbeTest {
    @Test fun recordsLivePublicSearchAvailability() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val web = PublicSearch()
        val result = try {
            val batch = kotlinx.coroutines.runBlocking {
                val catalog = com.google.gson.Gson().fromJson(context.assets.open("searchhh-portals.json").bufferedReader().use { it.readText() }, Array<Source>::class.java)
                PortalCrawler(web).search("fellowship", catalog.filter { it.id in listOf("opportunitydesk", "opportunitiescorners") }, "opportunities", true)
            }
            "PUBLIC SEARCH DIAGNOSTIC: ${batch.rows.size} results; provenance counts=${batch.rows.flatMap { it.sources }.groupingBy { it }.eachCount()}; errors=${batch.errors}; direct global portal RSS/HTML diagnostic"
        } catch (e: Exception) { "PUBLIC SEARCH UNAVAILABLE: ${e.javaClass.simpleName}: ${e.message}" }
        finally { web.http.dispatcher.cancelAll(); web.http.connectionPool.evictAll(); web.http.dispatcher.executorService.shutdown() }
        File(context.filesDir, "search-probe.txt").writeText(result)
        println("Searchhh search diagnostic: $result")
    }
    @Test fun recordsLiveRelayAvailability() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalIdentity.create(context, "Relay test installation")
        val server = DeviceMcpServer(context)
        val bridge = RelayBridge(context)
        val result = try {
            server.start()
            bridge.connect(server.port)
            verifyPublicMcp(bridge.url!!, LocalIdentity.load(context)!!)
            "AVAILABLE: Android JSch outbound SSH, public HTTPS authentication and public MCP SSE initialize/tool call verified against this installation."
        } catch (e: Exception) {
            "UNAVAILABLE: ${e.javaClass.simpleName}: ${e.message}. Public remote connectivity is NOT verified."
        } finally { bridge.close(); server.stop() }
        File(context.filesDir, "relay-probe.txt").writeText(result)
        println("Searchhh relay diagnostic: $result")
    }
    private fun verifyPublicMcp(base: String, identity: LocalIdentity) {
        val client = OkHttpClient.Builder().readTimeout(20, TimeUnit.SECONDS).followRedirects(false).build()
        try {
            client.newCall(Request.Builder().url("$base/sse").header("Accept", "text/event-stream").header("Authorization", "Bearer ${identity.token}").build()).execute().use { response ->
                check(response.code == 200) { "Public MCP SSE HTTP ${response.code}" }
                val stream = response.body!!.source()
                fun event(): String {
                    var data = ""
                    while (true) {
                        val line = stream.readUtf8Line() ?: error("Public SSE closed")
                        if (line.startsWith("data:")) data += line.removePrefix("data:").trim()
                        if (line.isEmpty() && data.isNotEmpty()) return data
                    }
                }
                val endpoint = event()
                check(endpoint.startsWith("/message?")) { "Unexpected public MCP endpoint" }
                fun send(text: String) {
                    client.newCall(Request.Builder().url(base + endpoint).header("Authorization", "Bearer ${identity.token}").post(text.toByteArray().toRequestBody("application/json".toMediaType())).build()).execute().use { check(it.code == 202) { "Public MCP POST HTTP ${it.code}" } }
                }
                send("""{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"relay-test","version":"1"}}}""")
                check(event().contains("serverInfo")) { "Public MCP negotiation failed" }
                send("""{"jsonrpc":"2.0","method":"notifications/initialized"}""")
                send("""{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"server_status","arguments":{}}}""")
                check(event().contains(identity.id)) { "Public tool response did not match this installation" }
            }
        } finally { client.dispatcher.cancelAll(); client.connectionPool.evictAll(); client.dispatcher.executorService.shutdown() }
    }

}

package info.plateaukao.einkbro.searchhh

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.plateaukao.einkbro.searchhh.local.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** External-service diagnostic, deliberately not a deterministic release gate.
 * Local protocol/security tests are gated separately. Always records actual availability.
 */
@RunWith(AndroidJUnit4::class)
class RelayProbeTest {
    @Test fun recordsLivePublicSearchAvailability() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val web = PublicSearch()
        val result = try {
            val batch = kotlinx.coroutines.runBlocking { web.search("fellowship", listOf("github", "hackernews", "duckduckgo"), "links", 0) }
            "PUBLIC SEARCH DIAGNOSTIC: ${batch.rows.size} results; provenance counts=${batch.rows.flatMap { it.sources }.groupingBy { it }.eachCount()}; errors=${batch.errors}; ${web.connection}"
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
            "AVAILABLE: Android JSch outbound SSH and public HTTPS forwarding verified. Unauthorized rejected; authenticated health matched this installation."
        } catch (e: Exception) {
            "UNAVAILABLE: ${e.javaClass.simpleName}: ${e.message}. Public remote connectivity is NOT verified."
        } finally { bridge.close(); server.stop() }
        File(context.filesDir, "relay-probe.txt").writeText(result)
        println("Searchhh relay diagnostic: $result")
    }
}

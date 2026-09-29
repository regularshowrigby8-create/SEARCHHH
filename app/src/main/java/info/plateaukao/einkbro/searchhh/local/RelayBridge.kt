package info.plateaukao.einkbro.searchhh.local

import android.content.Context
import com.jcraft.jsch.*
import info.plateaukao.einkbro.searchhh.ConnectionSettings
import java.io.ByteArrayOutputStream
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/** Existing JSch reverse forwarding + localhost.run's documented free nokey service. */
class RelayBridge(private val context: Context) {
    @Volatile var url: String? = null
        private set
    @Volatile var status = "Not connected"
        private set
    private var session: Session? = null
    private var channel: ChannelExec? = null
    private val prefs get() = ConnectionSettings.prefs(context)
    fun connect(port: Int) {
        status = "Connecting outbound SSH relay"
        val jsch = JSch()
        // Trust on first use; subsequent key changes fail closed. Never StrictHostKeyChecking=no.
        jsch.hostKeyRepository = object : HostKeyRepository {
            override fun check(host: String, key: ByteArray): Int {
                val encoded = android.util.Base64.encodeToString(key, android.util.Base64.NO_WRAP)
                val saved = prefs.getString("relay_host_key", null)
                if (saved == null) { kotlin.check(prefs.edit().putString("relay_host_key", encoded).commit()) { "Cannot persist relay host pin" }; return HostKeyRepository.OK }
                return if (saved == encoded) HostKeyRepository.OK else HostKeyRepository.CHANGED
            }
            override fun add(hostkey: HostKey, ui: UserInfo?) = Unit
            override fun remove(host: String?, type: String?) = Unit
            override fun remove(host: String?, type: String?, key: ByteArray?) = Unit
            override fun getKnownHostsRepositoryID() = "Searchhh localhost.run TOFU pin"
            override fun getHostKey(): Array<HostKey> = emptyArray()
            override fun getHostKey(host: String?, type: String?): Array<HostKey> = emptyArray()
        }
        val connection = jsch.getSession("nokey", "localhost.run", 22)
        connection.setConfig("StrictHostKeyChecking", "yes")
        connection.setConfig("PreferredAuthentications", "none")
        connection.serverAliveInterval = 30000
        session = connection
        connection.connect(20000)
        connection.setPortForwardingR(80, "127.0.0.1", port)
        val exec = connection.openChannel("exec") as ChannelExec
        exec.setCommand("--output json")
        val output = object : ByteArrayOutputStream() {
            @Synchronized override fun write(b: ByteArray, off: Int, len: Int) {
                super.write(b, off, len)
                val text = toString("UTF-8")
                val found = text.lineSequence().mapNotNull(RelayEvent::httpsUrl).firstOrNull()
                if (found != null) { url = found; status = "Verifying public forwarding" }
                if (size() > 16384) reset()
            }
        }
        exec.outputStream = output; exec.setErrStream(output); channel = exec; exec.connect(10000)
        val deadline = System.currentTimeMillis() + 20000
        while (url == null && connection.isConnected && System.currentTimeMillis() < deadline) Thread.sleep(250)
        check(url != null) { "Relay did not return a usable forwarding event" }
        val identity = LocalIdentity.load(context) ?: error("Installation not initialized")
        val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build()
        try {
            client.newCall(Request.Builder().url("$url/health").build()).execute().use { check(it.code == 401) { "Public relay did not enforce authentication" } }
            client.newCall(Request.Builder().url("$url/health").header("Authorization", "Bearer ${identity.token}").build()).execute().use { response ->
                check(response.code == 200) { "Public forwarding check failed (HTTP ${response.code})" }
                val body = response.body!!.source(); body.request(8193)
                check(body.buffer.size <= 8192) { "Unexpected relay response" }
                check(JsonParser.parseString(body.readUtf8()).asJsonObject["installationId"]?.asString == identity.id) { "Relay reached a different installation" }
            }
            status = "Connected · verified public HTTPS forwarding"
        } finally { client.connectionPool.evictAll(); client.dispatcher.executorService.shutdown() }
    }
    fun connected(): Boolean = session?.isConnected == true && channel?.isConnected == true && url != null
    fun close() { channel?.disconnect(); session?.disconnect(); channel = null; session = null; url = null; status = "Disconnected" }
}

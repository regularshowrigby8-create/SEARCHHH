package info.plateaukao.einkbro.searchhh.local

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import info.plateaukao.einkbro.searchhh.ConnectionSettings
import info.plateaukao.einkbro.searchhh.SearchhhActivity
import kotlinx.coroutines.*

/** User-started foreground service; no external backend deployment or manual credentials. */
class InternalBackendService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mcp: DeviceMcpServer? = null
    private var bridge: RelayBridge? = null
    private var starting = false
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("searchhh-server", "Searchhh internal server", NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(this, 1, Intent(this, InternalBackendService::class.java).setAction("STOP"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val open = PendingIntent.getActivity(this, 2, Intent(this, SearchhhActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        startForeground(2042, NotificationCompat.Builder(this, "searchhh-server").setSmallIcon(android.R.drawable.ic_menu_search).setContentTitle("Searchhh personal server").setContentText("Search + MCP running on your device. Tap to manage.").setContentIntent(open).addAction(android.R.drawable.ic_media_pause, "Stop server", stop).setOngoing(true).build())
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { ConnectionSettings.prefs(this).edit().putBoolean("server_enabled", false).apply(); stopSelf(); return START_NOT_STICKY }
        if (LocalIdentity.load(this) == null) { stopSelf(); return START_NOT_STICKY }
        if (active || starting) return START_NOT_STICKY
        starting = true; state = "Starting internal server"
        scope.launch {
            try {
                mcp = DeviceMcpServer(this@InternalBackendService)
                mcp!!.start(); ensureActive()
                withContext(Dispatchers.Main.immediate) { ensureActive(); localPort = mcp!!.port; active = true; starting = false; state = "Internal server ready" }
                if (ConnectionSettings.prefs(this@InternalBackendService).getBoolean("relay_enabled", true)) {
                    var delayMs = 5000L
                    while (isActive) {
                        try {
                            bridge = RelayBridge(this@InternalBackendService)
                            bridge!!.connect(localPort); ensureActive()
                            remoteUrl = bridge!!.url; relayState = bridge!!.status; delayMs = 5000
                            while (isActive && bridge?.connected() == true) delay(5000)
                        } catch (e: CancellationException) { throw e }
                        catch (e: Exception) { relayState = "Remote bridge unavailable: ${e.message}. In-app backend still works." }
                        finally { bridge?.close(); remoteUrl = null }
                        delay(delayMs); delayMs = (delayMs * 2).coerceAtMost(300000)
                    }
                } else relayState = "Remote access disabled"
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { state = "MCP server failed: ${e.message}"; active = false; stopSelf() }
            finally { if (!isActive) { bridge?.close(); mcp?.stop() } }
        }
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        scope.cancel()
        starting = false; active = false; state = "Stopped"; remoteUrl = null; localPort = 0
        bridge?.close(); mcp?.stop()
        CoroutineScope(Dispatchers.IO).launch { LocalBackend.get(applicationContext).shutdown() }
        super.onDestroy()
    }
    companion object {
        @Volatile var active = false
        @Volatile var state = "Not initialized"
        @Volatile var relayState = "Not connected"
        @Volatile var remoteUrl: String? = null
        @Volatile var localPort = 0
        fun start(context: Context) = ContextCompat.startForegroundService(context, Intent(context, InternalBackendService::class.java))
        fun stop(context: Context) = context.startService(Intent(context, InternalBackendService::class.java).setAction("STOP"))
    }
}

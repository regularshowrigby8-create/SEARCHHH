package info.plateaukao.einkbro.searchhh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.*
import com.google.gson.Gson
import info.plateaukao.einkbro.activity.BrowserActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import info.plateaukao.einkbro.searchhh.local.*

class SearchhhActivity : ComponentActivity() {
    private var exportContent = ""
    private val export = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { contentResolver.openOutputStream(it)?.use { out -> out.write(exportContent.toByteArray()) } }
    }
    private fun browse(url: String) {
        if (Uri.parse(url).scheme in listOf("https", "http"))
            startActivity(Intent(this, BrowserActivity::class.java).setAction(Intent.ACTION_VIEW).setData(Uri.parse(url)))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { Searchhh() }
    }
    @Composable private fun Searchhh() {
        val accent = Color(0xFFC5F178)
        MaterialTheme(colors = darkColors(primary = accent, background = Color(0xFF101412), surface = Color(0xFF1A211C), onPrimary = Color(0xFF14200D))) {
            val scope = rememberCoroutineScope()
            val prefs = remember { ConnectionSettings.prefs(this) }
            val gson = remember { Gson() }
            val sources = remember { gson.fromJson(assets.open("searchhh-engines.json").bufferedReader().use { it.readText() }, Array<Source>::class.java).toList() }
            val dao = remember { SearchhhDatabase.get(this).saved() }
            val saved by dao.all().collectAsState(initial = emptyList())
            var tab by rememberSaveable { mutableStateOf(0) }
            var query by rememberSaveable { mutableStateOf("") }
            var mode by rememberSaveable { mutableStateOf("opportunities") }
            var crawl by rememberSaveable { mutableStateOf(false) }
            var selected by remember { mutableStateOf(sources.filter { it.default }.map { it.id }.toSet()) }
            var jobId by remember { mutableStateOf(prefs.getString("job", null)) }
            var status by remember { mutableStateOf(runCatching { gson.fromJson(prefs.getString("last_status", null), JobStatus::class.java) }.getOrNull()) }
            var busy by remember { mutableStateOf(false) }
            var message by remember { mutableStateOf("") }
            var profile by remember { mutableStateOf(LocalIdentity.load(this)) }
            var displayName by rememberSaveable { mutableStateOf(profile?.name.orEmpty()) }
            var serviceState by remember { mutableStateOf(InternalBackendService.state) }
            var bridgeState by remember { mutableStateOf(InternalBackendService.relayState) }
            var publicUrl by remember { mutableStateOf(InternalBackendService.remoteUrl) }
            var relayEnabled by remember { mutableStateOf(prefs.getBoolean("relay_enabled", true)) }
            var sharePairing by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (profile != null && prefs.getBoolean("server_enabled", true)) InternalBackendService.start(this@SearchhhActivity)
                while (true) {
                    serviceState = InternalBackendService.state; bridgeState = InternalBackendService.relayState
                    publicUrl = InternalBackendService.remoteUrl; delay(1500)
                }
            }
            val running = status?.status in listOf("queued", "running") || (jobId != null && status == null)
            suspend fun refresh() {
                val id = jobId ?: return
                try {
                    val latest = ConnectionSettings.api(this@SearchhhActivity).status(id)
                    if (status?.round != latest.round || status?.status != latest.status)
                        prefs.edit().putString("last_status", gson.toJson(latest)).apply()
                    status = latest
                    message = ""
                } catch (e: CancellationException) { throw e } catch (e: Exception) { message = "Cannot refresh: ${e.message}. Server status is unknown; use Stop when connected." }
            }
            LaunchedEffect(jobId) {
                if (jobId != null) lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    while (true) { refresh(); delay(5000) }
                }
            }
            if (profile == null) AlertDialog(
                onDismissRequest = {},
                title = { Text("Your personal Searchhh server") },
                text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter a name. Your installation ID and credentials are generated randomly, not from your name. No server deployment or provider keys required.")
                    OutlinedTextField(displayName, { displayName = it.take(60) }, label = { Text("Your name") }, singleLine = true)
                    Text("Search queries go to public search services. Remote agent access uses a temporary third-party relay which terminates HTTPS; it can see forwarded traffic. Disable it in Settings if unwanted.", style = MaterialTheme.typography.caption)
                    Text("The phone must remain online. This provides MCP tools, not an AI model. Switching from an older external backend does not stop jobs on that server.", style = MaterialTheme.typography.caption)
                } },
                confirmButton = { Button(enabled = displayName.isNotBlank(), onClick = {
                    profile = LocalIdentity.create(this@SearchhhActivity, displayName)
                    prefs.edit().putBoolean("server_enabled", true).remove("job").remove("last_status").apply()
                    jobId = null; status = null; WorkManager.getInstance(this@SearchhhActivity).cancelUniqueWork("searchhh-status"); InternalBackendService.start(this@SearchhhActivity)
                }) { Text("Start my server") } }
            )
            if (sharePairing) AlertDialog(
                onDismissRequest = { sharePairing = false }, title = { Text("Share private agent access?") },
                text = { Text("Anyone receiving this connection credential can use your search tools and read saved results. Share only with an AI client you trust. Rotate credentials to revoke it.") },
                confirmButton = { TextButton(onClick = {
                    sharePairing = false
                    val identity = LocalIdentity.load(this@SearchhhActivity)
                    if (identity != null && publicUrl != null) {
                        val config = gson.toJson(mapOf("mcpServers" to mapOf("searchhh" to mapOf("type" to "sse", "url" to "$publicUrl/sse", "headers" to mapOf("Authorization" to "Bearer ${identity.token}")))))
                        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("application/json").putExtra(Intent.EXTRA_TEXT, config), "Share with your trusted MCP client"))
                    }
                }) { Text("Share credential") } }, dismissButton = { TextButton(onClick = { sharePairing = false }) { Text("Cancel") } }
            )
            Scaffold(
                modifier = Modifier.background(MaterialTheme.colors.background).systemBarsPadding(),
                topBar = { TopAppBar(title = { Column { Text("searchhh", fontWeight = FontWeight.Bold); Text("THE OPPORTUNITY HIVE", style = MaterialTheme.typography.overline) } }, backgroundColor = MaterialTheme.colors.background, elevation = 0.dp,
                    actions = { IconButton(onClick = { browse("https://duckduckgo.com") }) { Icon(Icons.Outlined.Language, "Open WebView browser") } }) },
                bottomBar = { BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
                    listOf("Discover", "Sources", "Saved", "Settings").forEachIndexed { index, label ->
                        BottomNavigationItem(selected = tab == index, onClick = { tab = index }, label = { Text(label) }, icon = { Icon(listOf(Icons.Outlined.Search, Icons.Outlined.Hub, Icons.Outlined.BookmarkBorder, Icons.Outlined.Settings)[index], label) })
                    }
                } }
            ) { padding ->
                LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background).padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (message.isNotBlank()) item { Text(message, color = Color(0xFFFFC28A), style = MaterialTheme.typography.body2) }
                    when (tab) {
                        0 -> {
                            item { Text("Less noise.\nMore opportunity.", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold) }
                            item { Text("Discover cohorts, certifications and application forms across the public web.", color = Color(0xFFACB9AD)) }
                            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("opportunities" to "Opportunity Finder", "links" to "Link Mode").forEach { (value, label) ->
                                    OutlinedButton(onClick = { mode = value }, enabled = !running, colors = ButtonDefaults.outlinedButtonColors(contentColor = if (mode == value) accent else Color.Gray)) { Text(label) }
                                }
                            } }
                            item { OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("What are you looking for?") }, placeholder = { Text("e.g. free AI cohort South Africa") }, enabled = !running, maxLines = 3) }
                            item { Row { Checkbox(crawl, { crawl = it }, enabled = !running); Column { Text("Follow public opportunity links"); Text("Local crawler · robots.txt respected · forms never submitted", style = MaterialTheme.typography.caption) } } }
                            item { Text("${selected.size} of ${sources.size} sources selected · No search API keys", style = MaterialTheme.typography.caption, color = accent) }
                            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(enabled = !busy && !running && profile != null && InternalBackendService.active && query.trim().length >= 2 && selected.isNotEmpty(), onClick = {
                                    scope.launch {
                                        busy = true
                                        try {
                                            val created = ConnectionSettings.api(this@SearchhhActivity).start(StartRequest(query.trim(), mode, selected.toList(), crawl))
                                            status = null; jobId = created.id; prefs.edit().putString("job", jobId).remove("last_status").apply(); message = "Search queued"
                                            // Continuous local work is owned by the visible foreground service, not WorkManager polling.
                                        } catch (e: CancellationException) { throw e } catch (e: Exception) { message = "Search not started: ${e.message}" } finally { busy = false }
                                    }
                                }) { Icon(Icons.Outlined.PlayArrow, null); Text(if (busy) "Connecting…" else "Start swarm") }
                                OutlinedButton(enabled = jobId != null && !busy, onClick = {
                                    scope.launch {
                                        busy = true
                                        try { ConnectionSettings.api(this@SearchhhActivity).stop(jobId!!); refresh(); WorkManager.getInstance(this@SearchhhActivity).cancelUniqueWork("searchhh-status") }
                                        catch (e: CancellationException) { throw e } catch (e: Exception) { message = "Stop not confirmed: ${e.message}. The server may still be searching." } finally { busy = false }
                                    }
                                }) { Icon(Icons.Outlined.Stop, null); Text("Stop") }
                            } }
                            item { Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(status?.status?.uppercase() ?: if (jobId != null) "CONNECTING" else "READY WHEN YOU ARE", color = accent, style = MaterialTheme.typography.overline)
                                Text("${status?.results?.size ?: 0} results   ·   ${status?.duplicates ?: 0} duplicates removed")
                                Text("Pass ${status?.round ?: 0} · ${status?.filtered ?: 0} irrelevant / bot results filtered", style = MaterialTheme.typography.caption)
                                Text("Source-dated results first. Unknown dates stay unknown. Open applications and eligibility are not verified.", style = MaterialTheme.typography.caption)
                            } } }
                            status?.errors?.let { errors -> items(errors.distinct()) { Text(it, style = MaterialTheme.typography.caption, color = Color(0xFFFFC28A)) } }
                            if (status?.results?.isNotEmpty() == true) item { TextButton(onClick = { exportContent = gson.toJson(status); export.launch("searchhh-results.json") }) { Text("Export results as JSON") } }
                            items(status?.results ?: emptyList(), key = { it.id }) { result -> ResultCard(result, saved.any { it.id == result.id }, { browse(result.url) }) {
                                scope.launch { if (saved.any { it.id == result.id }) dao.remove(result.id) else dao.save(SavedOpportunity(result.id, gson.toJson(result))) }
                            } }
                            if (status?.results.isNullOrEmpty()) item { Text("No results yet. Your backend is included. Enter a topic and start. Direct sources work independently; public metasearch availability varies. No model or AI reasoning is bundled.", style = MaterialTheme.typography.body2, color = Color(0xFFACB9AD)) }
                        }
                        1 -> {
                            item { Text("${sources.size} sources. One hive.", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
                            item { Text("128 real source adapters in the catalog, not 128 guaranteed live providers. The included backend searches direct APIs and automatically discovers a public SearXNG service. Sources run in batches of eight. Missing or blocked adapters are reported, never bypassed.") }
                            item { TextButton(enabled = !running, onClick = { selected = if (selected.size == sources.size) emptySet() else sources.map { it.id }.toSet() }) { Text(if (selected.size == sources.size) "Deselect all" else "Select all") } }
                            items(sources, key = { it.id }) { source -> Card { Row(Modifier.fillMaxWidth().padding(8.dp)) { Checkbox(source.id in selected, { selected = if (it) selected + source.id else selected - source.id }, enabled = !running); Column(Modifier.padding(top = 10.dp)) { Text(source.name, fontWeight = FontWeight.Bold); Text("${source.category} · keyless adapter", style = MaterialTheme.typography.caption) } } } }
                        }
                        2 -> {
                            item { Text("Your next chapter.", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold); Text("Saved on this device with Room. Available offline.") }
                            if (saved.isEmpty()) item { Text("Save an opportunity from Discover to keep it here.") }
                            items(saved, key = { it.id }) { row -> val result = gson.fromJson(row.payload, Opportunity::class.java); ResultCard(result, true, { browse(result.url) }) { scope.launch { dao.remove(row.id) } } }
                        }
                        3 -> {
                            item { Text("Your included backend", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
                            item { Text("Hello, ${profile?.name.orEmpty()}", color = accent); Text(serviceState); Text(bridgeState, style = MaterialTheme.typography.caption) }
                            item { Text("Installation ID: ${profile?.id ?: "not initialized"}", style = MaterialTheme.typography.caption); Text("Random per installation. Duplicate names do not share a server, database or credentials.", style = MaterialTheme.typography.caption) }
                            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(enabled = profile != null && !InternalBackendService.active, onClick = { prefs.edit().putBoolean("server_enabled", true).apply(); InternalBackendService.start(this@SearchhhActivity) }) { Text("Start server") }
                                OutlinedButton(enabled = InternalBackendService.active, onClick = { prefs.edit().putBoolean("server_enabled", false).apply(); InternalBackendService.stop(this@SearchhhActivity); message = "Stopping internal server and local searches" }) { Text("Stop server") }
                            } }
                            item { Row { Checkbox(relayEnabled, { enabled -> relayEnabled = enabled; prefs.edit().putBoolean("relay_enabled", enabled).apply(); if (!enabled) InternalBackendService.stop(this@SearchhhActivity); message = "Restart the server to apply remote-access settings" }); Column { Text("Remote MCP bridge"); Text("JSch + localhost.run · outbound SSH · temporary HTTPS URL", style = MaterialTheme.typography.caption) } } }
                            item { Text(publicUrl ?: "Public URL unavailable. In-app calls do not use the tunnel or localhost HTTP.", style = MaterialTheme.typography.body2) }
                            item { Button(enabled = publicUrl != null, onClick = { sharePairing = true }) { Text("Pair an AI client") } }
                            item { OutlinedButton(enabled = profile != null, onClick = { LocalIdentity.rotate(this@SearchhhActivity); profile = LocalIdentity.load(this@SearchhhActivity); message = "Agent credential rotated; old clients can no longer send tool requests" }) { Text("Rotate agent credential") } }
                            item { Text(LocalBackend.get(this@SearchhhActivity).web.connection, style = MaterialTheme.typography.caption) }
                            item { Divider(); Text("AI-ready tools, not a bundled AI model", fontWeight = FontWeight.Bold); Text("MCP exposes start/stop search, results, source catalog, server status and saved opportunities to a separately supplied AI client. No claim of free unlimited AI.") }
                            item { Text("The backend runs in this app: Kotlin, Room, coroutines, Ktor and the official MCP SDK. JSch bridges remote connections. No Docker, PostgreSQL, hosting account or repo deployment is needed on your phone.") }
                            item { Text("Android may stop background processes. Foreground notification provides a Stop control. Free relays can disconnect/change URL. Relay SSH keys use first-use pinning; changed keys fail closed. Public services can see your queries and may limit access.", style = MaterialTheme.typography.caption) }
                            item { TextButton(onClick = { browse("https://github.com/regularshowrigby8-create/SEARCHHH/tree/arena/01a0ea36-searchhh") }) { Text("Sources, licenses & limitations") } }

                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
    @Composable private fun ResultCard(result: Opportunity, saved: Boolean, open: () -> Unit, save: () -> Unit) {
        Card(shape = RoundedCornerShape(14.dp), elevation = 0.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(result.kind.uppercase(), style = MaterialTheme.typography.overline, color = MaterialTheme.colors.primary)
                Text(result.title, style = MaterialTheme.typography.h6)
                Text(result.description, maxLines = 5, style = MaterialTheme.typography.body2)
                Text(result.url, maxLines = 2, style = MaterialTheme.typography.caption)
                Text("${result.sources.joinToString(" · ")}\n${result.published?.take(10) ?: "Date unknown"} · Application unverified", style = MaterialTheme.typography.caption)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = open) { Text("Open in browser ↗") }
                    TextButton(onClick = save) { Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, null); Text(if (saved) "Saved" else "Save") }
                }
            }
        }
    }
}

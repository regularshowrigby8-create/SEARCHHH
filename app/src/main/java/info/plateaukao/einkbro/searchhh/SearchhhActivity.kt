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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.*
import com.google.gson.Gson
import info.plateaukao.einkbro.activity.BrowserActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
            var forgetConnection by remember { mutableStateOf(false) }
            var server by rememberSaveable { mutableStateOf(prefs.getString("url", "").orEmpty()) }
            var token by remember { mutableStateOf(prefs.getString("token", "").orEmpty()) }
            val running = status?.status in listOf("queued", "running") || (jobId != null && status == null)
            suspend fun refresh() {
                val id = jobId ?: return
                try {
                    status = ConnectionSettings.api(this@SearchhhActivity).status(id)
                    prefs.edit().putString("last_status", gson.toJson(status)).apply()
                    message = ""
                } catch (e: CancellationException) { throw e } catch (e: Exception) { message = "Cannot refresh: ${e.message}. Server status is unknown; use Stop when connected." }
            }
            LaunchedEffect(jobId) {
                if (jobId != null) lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    while (true) { refresh(); delay(5000) }
                }
            }
            if (forgetConnection) AlertDialog(
                onDismissRequest = { forgetConnection = false },
                title = { Text("Disconnect this device?") },
                text = { Text("This does NOT stop a remote swarm. If your server is unreachable, stop the job from the server before starting another. Local saved opportunities are kept.") },
                confirmButton = { TextButton(onClick = {
                    prefs.edit().remove("job").remove("last_status").remove("url").remove("token").apply()
                    WorkManager.getInstance(this@SearchhhActivity).cancelUniqueWork("searchhh-status")
                    jobId = null; status = null; server = ""; token = ""; forgetConnection = false
                    message = "Disconnected locally. Any remote job must be stopped on its server."
                }) { Text("Disconnect locally") } },
                dismissButton = { TextButton(onClick = { forgetConnection = false }) { Text("Cancel") } }
            )
            Scaffold(
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
                            item { Row { Checkbox(crawl, { crawl = it }, enabled = !running); Column { Text("Follow public opportunity links"); Text("Scrapy · robots.txt respected · forms never submitted", style = MaterialTheme.typography.caption) } } }
                            item { Text("${selected.size} of ${sources.size} sources selected · No search API keys", style = MaterialTheme.typography.caption, color = accent) }
                            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(enabled = !busy && !running && query.trim().length >= 2 && selected.isNotEmpty(), onClick = {
                                    scope.launch {
                                        busy = true
                                        try {
                                            val created = ConnectionSettings.api(this@SearchhhActivity).start(StartRequest(query.trim(), mode, selected.toList(), crawl))
                                            status = null; jobId = created.id; prefs.edit().putString("job", jobId).remove("last_status").apply(); message = "Search queued"
                                            val request = PeriodicWorkRequestBuilder<SearchhhSyncWorker>(15, TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
                                            WorkManager.getInstance(this@SearchhhActivity).enqueueUniquePeriodicWork("searchhh-status", ExistingPeriodicWorkPolicy.UPDATE, request)
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
                            if (status?.results.isNullOrEmpty()) item { Text("No results yet. Connect your Searchhh backend in Settings, choose sources, and start a search. Ordinary WebView browsing works without a backend.", style = MaterialTheme.typography.body2, color = Color(0xFFACB9AD)) }
                        }
                        1 -> {
                            item { Text("${sources.size} sources. One hive.", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
                            item { Text("Existing SearXNG adapters, not invented engines. Availability and rate limits vary. Selected sources run on your backend; selection locks while searching.") }
                            item { TextButton(enabled = !running, onClick = { selected = if (selected.size == sources.size) emptySet() else sources.map { it.id }.toSet() }) { Text(if (selected.size == sources.size) "Deselect all" else "Select all") } }
                            items(sources, key = { it.id }) { source -> Card { Row(Modifier.fillMaxWidth().padding(8.dp)) { Checkbox(source.id in selected, { selected = if (it) selected + source.id else selected - source.id }, enabled = !running); Column(Modifier.padding(top = 10.dp)) { Text(source.name, fontWeight = FontWeight.Bold); Text("${source.category} · keyless adapter", style = MaterialTheme.typography.caption) } } } }
                        }
                        2 -> {
                            item { Text("Your next chapter.", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold); Text("Saved on this device with Room. Available offline.") }
                            if (saved.isEmpty()) item { Text("Save an opportunity from Discover to keep it here.") }
                            items(saved, key = { it.id }) { row -> val result = gson.fromJson(row.payload, Opportunity::class.java); ResultCard(result, true, { browse(result.url) }) { scope.launch { dao.remove(row.id) } } }
                        }
                        3 -> {
                            item { Text("Make the hive yours.", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
                            item { Text("API-less search", color = accent); Text("Default mode. No search-provider keys. A self-hosted backend is required for the swarm; the access token below protects your server, not a paid search API.") }
                            item { OutlinedTextField(server, { server = it }, Modifier.fillMaxWidth(), label = { Text("Backend HTTPS address") }, singleLine = true, enabled = !running) }
                            item { OutlinedTextField(token, { token = it }, Modifier.fillMaxWidth(), label = { Text("Your server access token") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), enabled = !running) }
                            item { Button(enabled = !busy && !running, onClick = {
                                if (!ConnectionSettings.validServerUrl(server) || token.length < 32) message = "Use a valid HTTPS server address and a 32+ character access token."
                                else { prefs.edit().putString("url", server.trim().trimEnd('/')).putString("token", token).remove("job").remove("last_status").apply(); jobId = null; status = null
                                    scope.launch { busy = true; try { val live = ConnectionSettings.api(this@SearchhhActivity).sources(); message = "Connected: ${live.size} backend sources" } catch (e: CancellationException) { throw e } catch (e: Exception) { message = "Saved, but connection failed: ${e.message}" } finally { busy = false } }
                                }
                            }) { Text("Save & test connection") } }
                            item { TextButton(enabled = !busy, onClick = { forgetConnection = true }) { Text("Disconnect / change server") } }
                            item { Divider(); Text("AI-assisted mode · not enabled", fontWeight = FontWeight.Bold); Text("Optional bring-your-own-key providers are planned. No claim of unlimited free AI. The current swarm uses existing search adapters and transparent opportunity rules.") }
                            item { Text("Built on EinkBro, Android WebView, SearXNG, Scrapy, FastAPI, PostgreSQL and Redis. Background status sync uses WorkManager; crawling runs on the server.", style = MaterialTheme.typography.body2) }
                            item { TextButton(onClick = { browse("https://github.com/regularshowrigby8-create/SEARCHHH/tree/arena/01a0ea36-searchhh") }) { Text("Source, licenses & setup") } }
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

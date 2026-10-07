package info.plateaukao.einkbro.searchhh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.*
import com.google.gson.Gson
import info.plateaukao.einkbro.searchhh.local.*
import info.plateaukao.einkbro.searchhh.testing.SearchhhTestTags
import info.plateaukao.einkbro.searchhh.web.SearchhhResultsWebViewActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchhhActivity : ComponentActivity() {
    private var exportContent = ""
    private val export =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let { contentResolver.openOutputStream(it)?.use { out -> out.write(exportContent.toByteArray()) } }
        }

    private fun browse(url: String) {
        if (Uri.parse(url).scheme in listOf("https", "http")) {
            SearchhhResultsWebViewActivity.openUrl(this, url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent { Searchhh() }
    }

    @Composable private fun Searchhh() {
        val accent = SearchhhDesignTokens.accent
        SearchhhTheme {
            val scope = rememberCoroutineScope()
            val prefs = remember { ConnectionSettings.prefs(this) }
            val gson = remember { Gson() }
            val sources =
                remember {
                    gson
                        .fromJson(
                            assets.open("searchhh-portals.json").bufferedReader().use { it.readText() },
                            Array<Source>::class.java,
                        ).toList()
                }
            val codebases = remember { CodebaseRegistry.load(this@SearchhhActivity) }
            var selectedAdapterId by rememberSaveable { mutableStateOf(CrawlerAdapters.default.id) }
            var showCodebases by rememberSaveable { mutableStateOf(false) }
            var codebaseQuery by rememberSaveable { mutableStateOf("") }
            val dao = remember { SearchhhDatabase.get(this).saved() }
            val saved by dao.all().collectAsState(initial = emptyList())
            var routeId by rememberSaveable { mutableStateOf(SearchhhRoute.DISCOVER.id) }
            val route = SearchhhRoute.fromId(routeId)
            val selectedAdapter = CrawlerAdapters.byId(selectedAdapterId) ?: CrawlerAdapters.default
            BackHandler(enabled = route != SearchhhRoute.DISCOVER) {
                if (route == SearchhhRoute.SOURCES && showCodebases) {
                    showCodebases = false
                } else {
                    routeId = SearchhhRoute.DISCOVER.id
                }
            }
            var query by rememberSaveable { mutableStateOf("") }
            var mode by rememberSaveable { mutableStateOf("opportunities") }
            var crawl by rememberSaveable { mutableStateOf(true) }
            var selected by remember { mutableStateOf(sources.filter { it.default }.map { it.id }.toSet()) }
            var jobId by remember { mutableStateOf(prefs.getString("job", null)) }
            var status by remember {
                mutableStateOf(runCatching { gson.fromJson(prefs.getString("last_status", null), JobStatus::class.java) }.getOrNull())
            }
            var busy by remember { mutableStateOf(false) }
            var message by remember { mutableStateOf("") }
            var profile by remember { mutableStateOf(LocalIdentity.load(this)) }
            var displayName by rememberSaveable { mutableStateOf(profile?.name.orEmpty()) }
            var serviceState by remember { mutableStateOf(InternalBackendService.state) }
            var bridgeState by remember { mutableStateOf(InternalBackendService.relayState) }
            var publicUrl by remember { mutableStateOf(InternalBackendService.remoteUrl) }
            var relayEnabled by remember { mutableStateOf(prefs.getBoolean("relay_enabled", true)) }
            var sharePairing by remember { mutableStateOf(false) }
            val ai = remember { LocalBackend.get(this@SearchhhActivity).ai }
            var aiStatus by remember { mutableStateOf(ai.status) }
            LaunchedEffect(Unit) {
                if (profile != null && prefs.getBoolean("server_enabled", true)) InternalBackendService.start(this@SearchhhActivity)
                while (true) {
                    aiStatus = ai.status
                    serviceState = InternalBackendService.state
                    bridgeState = InternalBackendService.relayState
                    publicUrl = InternalBackendService.remoteUrl
                    delay(1500)
                }
            }
            val running = status?.status in listOf("queued", "running") || (jobId != null && status == null)

            suspend fun refresh() {
                val id = jobId ?: return
                try {
                    val latest = ConnectionSettings.api(this@SearchhhActivity).status(id)
                    if (status?.round != latest.round || status?.status != latest.status) {
                        prefs.edit().putString("last_status", gson.toJson(latest)).apply()
                    }
                    status = latest
                    message = ""
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    message =
                        "Cannot refresh: ${e.message}. Server status is unknown; use Stop when connected."
                }
            }
            LaunchedEffect(jobId) {
                if (jobId != null) {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        while (true) {
                            refresh()
                            delay(5000)
                        }
                    }
                }
            }
            if (profile == null) {
                AlertDialog(
                    onDismissRequest = { Unit },
                    title = { Text("Your personal Searchhh server") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Enter a name. Your installation ID and credentials are generated randomly, not from your name. No server deployment or provider keys required.",
                            )
                            OutlinedTextField(displayName, { displayName = it.take(60) }, label = { Text("Your name") }, singleLine = true)
                            Text(
                                "Search queries go to public search services. Remote agent access uses a temporary third-party relay which terminates HTTPS; it can see forwarded traffic. Disable it in Settings if unwanted.",
                                style = MaterialTheme.typography.caption,
                            )
                            Text(
                                "The phone must remain online. This provides MCP tools, not an AI model. Switching from an older external backend does not stop jobs on that server.",
                                style = MaterialTheme.typography.caption,
                            )
                        }
                    },
                    confirmButton = {
                        Button(enabled = displayName.isNotBlank(), onClick = {
                            profile = LocalIdentity.create(this@SearchhhActivity, displayName)
                            prefs
                                .edit()
                                .putBoolean("server_enabled", true)
                                .remove("job")
                                .remove("last_status")
                                .apply()
                            jobId = null
                            status = null
                            WorkManager.getInstance(this@SearchhhActivity).cancelUniqueWork("searchhh-status")
                            InternalBackendService.start(this@SearchhhActivity)
                        }) { Text("Start my server") }
                    },
                )
            }
            if (sharePairing) {
                AlertDialog(
                    onDismissRequest = { sharePairing = false },
                    title = { Text("Share private agent access?") },
                    text = {
                        Text(
                            "Anyone receiving this connection credential can use your search tools and read saved results. Share only with an AI client you trust. Rotate credentials to revoke it.",
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            sharePairing = false
                            val identity = LocalIdentity.load(this@SearchhhActivity)
                            if (identity != null && publicUrl != null) {
                                val config =
                                    gson.toJson(
                                        mapOf(
                                            "mcpServers" to
                                                mapOf(
                                                    "searchhh" to
                                                        mapOf(
                                                            "type" to "sse",
                                                            "url" to "$publicUrl/sse",
                                                            "headers" to mapOf("Authorization" to "Bearer ${identity.token}"),
                                                        ),
                                                ),
                                        ),
                                    )
                                startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).setType("application/json").putExtra(Intent.EXTRA_TEXT, config),
                                        "Share with your trusted MCP client",
                                    ),
                                )
                            }
                        }) { Text("Share credential") }
                    },
                    dismissButton = { TextButton(onClick = { sharePairing = false }) { Text("Cancel") } },
                )
            }
            Scaffold(
                modifier = Modifier.background(MaterialTheme.colors.background).systemBarsPadding(),
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("searchhh", fontWeight = FontWeight.Bold)
                                Text("THE OPPORTUNITY HIVE", style = MaterialTheme.typography.overline)
                            }
                        },
                        backgroundColor = MaterialTheme.colors.background,
                        elevation = 0.dp,
                        actions = {
                            IconButton(
                                onClick = { browse("https://opportunitydesk.org") },
                            ) { Icon(Icons.Outlined.Language, "Open Searchhh WebView") }
                        },
                    )
                },
                bottomBar = {
                    BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
                        searchhhMenuRegistry.filter { it.showInBottomBar }.forEach { item ->
                            val destination = item.route
                            val label = destination.label
                            BottomNavigationItem(
                                modifier = Modifier.testTag(destination.menuTag),
                                selected = route == destination,
                                onClick = { routeId = destination.id },
                                label = { Text(label) },
                                icon = { Icon(item.icon, label) },
                            )
                        }
                    }
                },
            ) { padding ->
                LazyColumn(
                    modifier =
                        Modifier
                            .testTag(route.screenTag)
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background)
                            .padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (message.isNotBlank()) item { Text(message, color = Color(0xFFFFC28A), style = MaterialTheme.typography.body2) }
                    when (route) {
                        SearchhhRoute.DISCOVER -> {
                            item {
                                Text(
                                    "Less noise.\nMore opportunity.",
                                    style = MaterialTheme.typography.h4,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            item {
                                Text(
                                    "Discover cohorts, certifications and application forms across the public web.",
                                    color = Color(0xFFACB9AD),
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("opportunities" to "Opportunity Finder", "links" to "Link Mode").forEach { (value, label) ->
                                        OutlinedButton(
                                            onClick = { mode = value },
                                            enabled = !running,
                                            colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                    contentColor =
                                                        if (mode ==
                                                            value
                                                        ) {
                                                            accent
                                                        } else {
                                                            Color.Gray
                                                        },
                                                ),
                                        ) { Text(label) }
                                    }
                                }
                            }
                            item {
                                OutlinedTextField(query, {
                                    query = it
                                }, modifier = Modifier.testTag(SearchhhTestTags.SEARCH_FIELD).fillMaxWidth(), label = {
                                    Text("What are you looking for?")
                                }, placeholder = { Text("e.g. artificial intelligence fellowship") }, enabled = !running, maxLines = 3)
                            }
                            item {
                                Row {
                                    Checkbox(crawl, { crawl = it }, enabled = !running)
                                    Column {
                                        Text("Follow public opportunity links")
                                        Text(
                                            "Local crawler · robots.txt respected · forms never submitted",
                                            style = MaterialTheme.typography.caption,
                                        )
                                    }
                                }
                            }
                            item {
                                Text(
                                    "${selected.size} of ${sources.size} sources selected · No search API keys",
                                    style = MaterialTheme.typography.caption,
                                    color = accent,
                                )
                                Text(
                                    "Crawler profile: ${selectedAdapter.label} · ${selectedAdapter.implementation}",
                                    style = MaterialTheme.typography.caption,
                                )
                                TextButton(enabled = !running, onClick = {
                                    routeId = SearchhhRoute.SOURCES.id
                                    showCodebases = true
                                }) { Text("Choose a crawler profile") }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        modifier = Modifier.testTag(SearchhhTestTags.START_STOP_SEARCH),
                                        enabled =
                                            !busy &&
                                                !running &&
                                                profile != null &&
                                                InternalBackendService.active &&
                                                query.trim().length >= 2 &&
                                                selected.isNotEmpty(),
                                        onClick = {
                                            scope.launch {
                                                busy = true
                                                try {
                                                    val created =
                                                        ConnectionSettings
                                                            .api(
                                                                this@SearchhhActivity,
                                                            ).start(
                                                                StartRequest(
                                                                    query.trim(),
                                                                    mode,
                                                                    selected.toList(),
                                                                    crawl,
                                                                    selectedAdapter.id,
                                                                ),
                                                            )
                                                    status = null
                                                    jobId = created.id
                                                    prefs
                                                        .edit()
                                                        .putString("job", jobId)
                                                        .remove("last_status")
                                                        .apply()
                                                    message =
                                                        "Search queued"
                                                    // Continuous local work is owned by the visible foreground service, not WorkManager polling.
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (e: Exception) {
                                                    message =
                                                        "Search not started: ${e.message}"
                                                } finally {
                                                    busy = false
                                                }
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Outlined.PlayArrow, null)
                                        Text(if (busy) "Connecting…" else "Start swarm")
                                    }
                                    OutlinedButton(
                                        modifier =
                                            Modifier.testTag(
                                                SearchhhTestTags.STOP_SEARCH,
                                            ),
                                        enabled = jobId != null && !busy,
                                        onClick = {
                                            scope.launch {
                                                busy = true
                                                try {
                                                    ConnectionSettings.api(this@SearchhhActivity).stop(jobId!!)
                                                    refresh()
                                                    WorkManager.getInstance(this@SearchhhActivity).cancelUniqueWork("searchhh-status")
                                                } catch (
                                                    e: CancellationException,
                                                ) {
                                                    throw e
                                                } catch (e: Exception) {
                                                    message =
                                                        "Stop not confirmed: ${e.message}. The server may still be searching."
                                                } finally {
                                                    busy = false
                                                }
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Outlined.Stop, null)
                                        Text("Stop")
                                    }
                                }
                            }
                            item {
                                Card {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(
                                            status?.status?.uppercase() ?: if (jobId !=
                                                null
                                            ) {
                                                "CONNECTING"
                                            } else {
                                                "READY WHEN YOU ARE"
                                            },
                                            color = accent,
                                            style = MaterialTheme.typography.overline,
                                        )
                                        Text("${status?.results?.size ?: 0} results   ·   ${status?.duplicates ?: 0} duplicates removed")
                                        Text(
                                            "Crawler profile: ${status?.adapter ?: "configured backend"}",
                                            style = MaterialTheme.typography.caption,
                                        )
                                        Text(
                                            "Pass ${status?.round ?: 0} · ${status?.filtered ?: 0} irrelevant / bot results filtered",
                                            style = MaterialTheme.typography.caption,
                                        )
                                        Text(
                                            "Ranked by relevance when AI review is available. Dates and eligibility are evidence, not verified application status. Rolling retention: 1,000 recently checked results; saved links are kept separately.",
                                            style = MaterialTheme.typography.caption,
                                        )
                                    }
                                }
                            }
                            item { Text(aiStatus, style = MaterialTheme.typography.caption) }
                            status?.errors?.let { errors ->
                                items(errors.distinct()) { Text(it, style = MaterialTheme.typography.caption, color = Color(0xFFFFC28A)) }
                            }
                            if (status?.results?.isNotEmpty() ==
                                true
                            ) {
                                item {
                                    Text(
                                        "Crawler output is rendered in the Searchhh WebView so every result, evidence link, detail panel and save action shares one consistent document surface.",
                                        style = MaterialTheme.typography.body2,
                                    )
                                    Button(
                                        modifier = Modifier.testTag(SearchhhTestTags.RESULTS_WEBVIEW),
                                        onClick = { SearchhhResultsWebViewActivity.openResults(this@SearchhhActivity, status!!.id) },
                                    ) { Text("View all results in Searchhh WebView") }
                                }
                                item {
                                    TextButton(onClick = {
                                        exportContent = gson.toJson(status)
                                        export.launch("searchhh-results.json")
                                    }) { Text("Export results as JSON") }
                                }
                            }
                            if (status?.results.isNullOrEmpty()) {
                                item {
                                    Text(
                                        "No matches yet. Searches fetch global portal listings repeatedly and match all query words; put exact phrases in quotes. Try a broader topic if needed. Some portals require JavaScript or login and cannot be read by the crawler.",
                                        style = MaterialTheme.typography.body2,
                                        color = Color(0xFFACB9AD),
                                    )
                                }
                            }
                        }
                        SearchhhRoute.SOURCES -> {
                            item { Text("Sources & codebases", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { showCodebases = false }) { Text("Portals (${sources.size})") }
                                    OutlinedButton(onClick = { showCodebases = true }) { Text("Codebases (${codebases.entries.size})") }
                                }
                            }
                            if (!showCodebases) {
                                item {
                                    Text(
                                        "Global opportunity publishers and official programme/application portals—not generic search engines. These are configured crawl seeds, not guaranteed live adapters. HTML/RSS, JSON-LD and bounded same-host sitemap discovery run in batches of eight. Unsupported or blocked pages are reported.",
                                    )
                                }
                                item {
                                    TextButton(enabled = !running, onClick = {
                                        selected =
                                            if (selected.size ==
                                                sources.size
                                            ) {
                                                emptySet()
                                            } else {
                                                sources.map { it.id }.toSet()
                                            }
                                    }) {
                                        Text(
                                            if (selected.size ==
                                                sources.size
                                            ) {
                                                "Deselect all"
                                            } else {
                                                "Select all"
                                            },
                                        )
                                    }
                                }
                                items(sources, key = { it.id }) { source ->
                                    Card {
                                        Row(Modifier.fillMaxWidth().padding(8.dp)) {
                                            Checkbox(
                                                checked = source.id in selected,
                                                onCheckedChange = { checked ->
                                                    selected = if (checked) selected + source.id else selected - source.id
                                                },
                                                enabled = !running,
                                                modifier = Modifier.testTag(SearchhhTestTags.sourceFilter(source.id)),
                                            )
                                            ; Column(Modifier.padding(top = 10.dp)) {
                                                Text(source.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${source.category} · ${source.format ?: "public"} portal",
                                                    style = MaterialTheme.typography.caption,
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Text(
                                        "All 100 submitted entries + ${codebases.entries.size - 100} additions remain searchable. Four reviewed capability profiles are executable in this app; other entries stay clearly marked as catalogue references until a safe adapter is implemented.",
                                    )
                                }
                                item {
                                    Text(
                                        "Phone runtime: bounded scheduler, structured JSON-LD, article evidence and CSS-selector extraction. These are Android-native implementations inspired by the named projects, not untrusted third-party binaries. The optional Python backend separately integrates Scrapy, Extruct, Trafilatura and Parsel.",
                                        style = MaterialTheme.typography.caption,
                                    )
                                }
                                item {
                                    OutlinedTextField(codebaseQuery, {
                                        codebaseQuery = it.take(160)
                                    }, label = {
                                        Text(
                                            "Filter by name, number, language or role",
                                        )
                                    }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                }
                                item {
                                    Text(
                                        "Requested ignore profile retained: robots.txt, rate limits, politeness delays, max depth and domain scope. This is a recorded request, not active settings or a promise every tool supports it. Server limits cannot be disabled by a client.",
                                        style = MaterialTheme.typography.caption,
                                    )
                                }
                                item {
                                    TextButton(onClick = {
                                        exportContent = assets.open("searchhh-codebases.json").bufferedReader().use { it.readText() }
                                        export.launch("searchhh-codebase-audit.json")
                                    }) { Text("Export complete codebase audit") }
                                }
                                val visible = codebases.entries.filter { it.matches(codebaseQuery) }
                                item {
                                    Text(
                                        "${visible.size} shown · audited ${codebases.auditedAt.take(10)}",
                                        style = MaterialTheme.typography.caption,
                                    )
                                }
                                items(visible, key = { "codebase:${it.number}" }) { codebase ->
                                    CodebaseCard(codebase) { adapter ->
                                        selectedAdapterId = adapter.id
                                        showCodebases = false
                                        routeId = SearchhhRoute.DISCOVER.id
                                        message = "Using ${adapter.upstreamName}: ${adapter.description}"
                                    }
                                }
                            }
                        }
                        SearchhhRoute.SAVED -> {
                            item {
                                Text("Your next chapter.", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                                Text("Saved on this device with Room. Available offline.")
                            }
                            if (saved.isEmpty()) {
                                item { Text("Save an opportunity from Discover to keep it here.") }
                            } else {
                                item {
                                    Text(
                                        "Saved results use the same Searchhh WebView document as live crawler output, with offline Room data and remove actions.",
                                        style = MaterialTheme.typography.body2,
                                    )
                                    Button(
                                        modifier = Modifier.testTag(SearchhhTestTags.SAVED_WEBVIEW),
                                        onClick = { SearchhhResultsWebViewActivity.openSaved(this@SearchhhActivity) },
                                    ) {
                                        Text("Open saved results in Searchhh WebView")
                                    }
                                }
                            }
                        }
                        SearchhhRoute.SETTINGS -> {
                            item { Text("Your included backend", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
                            item {
                                Text("Hello, ${profile?.name.orEmpty()}", color = accent)
                                Text(serviceState)
                                Text(bridgeState, style = MaterialTheme.typography.caption)
                            }
                            item {
                                Text("Installation ID: ${profile?.id ?: "not initialized"}", style = MaterialTheme.typography.caption)
                                Text(
                                    "Random per installation. Duplicate names do not share a server, database or credentials.",
                                    style = MaterialTheme.typography.caption,
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(enabled = profile != null && !InternalBackendService.active, onClick = {
                                        prefs.edit().putBoolean("server_enabled", true).apply()
                                        InternalBackendService.start(this@SearchhhActivity)
                                    }) { Text("Start server") }
                                    OutlinedButton(enabled = InternalBackendService.active, onClick = {
                                        prefs.edit().putBoolean("server_enabled", false).apply()
                                        InternalBackendService.stop(this@SearchhhActivity)
                                        message =
                                            "Stopping internal server and local searches"
                                    }) { Text("Stop server") }
                                }
                            }
                            item {
                                Row {
                                    Checkbox(relayEnabled, { enabled ->
                                        relayEnabled = enabled
                                        prefs.edit().putBoolean("relay_enabled", enabled).apply()
                                        if (!enabled) InternalBackendService.stop(this@SearchhhActivity)
                                        message =
                                            "Restart the server to apply remote-access settings"
                                    })
                                    ; Column {
                                        Text("Remote MCP bridge")
                                        Text(
                                            "JSch + localhost.run · outbound SSH · temporary HTTPS URL",
                                            style = MaterialTheme.typography.caption,
                                        )
                                    }
                                }
                            }
                            item {
                                Text(
                                    publicUrl ?: "Public URL unavailable. In-app calls do not use the tunnel or localhost HTTP.",
                                    style = MaterialTheme.typography.body2,
                                )
                            }
                            item { Button(enabled = publicUrl != null, onClick = { sharePairing = true }) { Text("Pair an AI client") } }
                            item {
                                OutlinedButton(enabled = profile != null, onClick = {
                                    LocalIdentity.rotate(this@SearchhhActivity)
                                    profile =
                                        LocalIdentity.load(this@SearchhhActivity)
                                    message =
                                        "Agent credential rotated; old clients can no longer send tool requests"
                                }) { Text("Rotate agent credential") }
                            }
                            item {
                                info.plateaukao.einkbro.searchhh.ai
                                    .AiSettingsCard(ai)
                            }
                            item { Text(aiStatus, style = MaterialTheme.typography.caption) }
                            item {
                                Divider()
                                Text("AI-ready tools, not a bundled AI model", fontWeight = FontWeight.Bold)
                                Text(
                                    "MCP exposes start/stop search, results, source catalog, server status and saved opportunities to a separately supplied AI client. The internal reviewer is separately consented and has no MCP/search tools.",
                                )
                            }
                            item {
                                Text(
                                    "The backend runs in this app: Kotlin, Room, coroutines, Ktor and the official MCP SDK. JSch bridges remote connections. No Docker, PostgreSQL, hosting account or repo deployment is needed on your phone.",
                                )
                            }
                            item {
                                Text(
                                    "Android may stop background processes. Foreground notification provides a Stop control. Free relays can disconnect/change URL. Relay SSH keys use first-use pinning; changed keys fail closed. Public services can see your queries and may limit access.",
                                    style = MaterialTheme.typography.caption,
                                )
                            }
                            item {
                                TextButton(onClick = {
                                    browse("https://github.com/regularshowrigby8-create/SEARCHHH/tree/arena/01a0ea36-searchhh")
                                }) { Text("Sources, licenses & limitations") }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    @Composable private fun CodebaseCard(
        entry: CodebaseEntry,
        onUseAdapter: (CrawlerAdapter) -> Unit,
    ) {
        var expanded by rememberSaveable(entry.number) { mutableStateOf(false) }
        Card(shape = RoundedCornerShape(14.dp), elevation = 0.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("#${entry.number} ${entry.name}", style = MaterialTheme.typography.h6)
                Text(
                    "${entry.reportedLanguage ?: entry.submittedLanguage} · ${entry.category.replace('_', ' ')}",
                    style = MaterialTheme.typography.caption,
                )
                Text(
                    entry.repositoryStatus.replace('_', ' ') +
                        if (entry.archived ==
                            true
                        ) {
                            " · archived"
                        } else {
                            ""
                        },
                    style = MaterialTheme.typography.caption,
                )
                Text("Integration: ${entry.integration.replace('_', ' ')}", color = MaterialTheme.colors.primary)
                Text("License metadata: ${entry.license}", style = MaterialTheme.typography.caption)
                entry.note?.let { Text(it, style = MaterialTheme.typography.body2) }
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide audit details" else "Show audit details") }
                if (expanded) {
                    Text("Submitted: ${entry.submittedUrl}", style = MaterialTheme.typography.caption)
                    Text("Execution target: ${entry.executionTarget.replace('_', ' ')}", style = MaterialTheme.typography.caption)
                    entry.revision?.let { Text("Source revision: $it", style = MaterialTheme.typography.caption) }
                    entry.configurationSupport.forEach { (key, value) -> Text("$key: $value", style = MaterialTheme.typography.caption) }
                    if (entry.description.isNotBlank()) Text(entry.description, style = MaterialTheme.typography.caption)
                }
                CrawlerAdapters.forEntry(entry)?.let { adapter ->
                    Text("Available in this app: ${adapter.implementation}", color = MaterialTheme.colors.primary)
                    Text(adapter.description, style = MaterialTheme.typography.caption)
                    OutlinedButton(onClick = { onUseAdapter(adapter) }) { Text("Use ${adapter.label}") }
                } ?: Text(
                    "Catalogue reference only; no executable adapter is bundled for this entry.",
                    style = MaterialTheme.typography.caption,
                )
                entry.codeLink()?.let { url -> TextButton(onClick = { browse(url) }) { Text("Open source code ↗") } }
                if (entry.codeLink() ==
                    null
                ) {
                    Text("Exact codebase unresolved; original entry retained.", style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}

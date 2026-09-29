package info.plateaukao.einkbro.searchhh.local

import android.content.Context
import com.google.gson.Gson
import info.plateaukao.einkbro.searchhh.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Backend API in the Android process. Normal UI calls never depend on a loopback socket. */
class LocalBackend private constructor(context: Context) : SearchhhApi {
    private val app = context.applicationContext
    private val gson = Gson()
    private val db = SearchhhDatabase.get(app)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Mutex()
    private var worker: Job? = null
    @Volatile private var current: JobStatus? = null
    val web = PublicSearch()
    private val catalog = gson.fromJson(app.assets.open("searchhh-engines.json").bufferedReader().use { it.readText() }, Array<Source>::class.java).toList()
    override suspend fun sources() = catalog
    override suspend fun status(id: String): JobStatus = lock.withLock {
        current?.takeIf { it.id == id } ?: db.sessions().get(id)?.let { row ->
            gson.fromJson(row.payload, JobStatus::class.java).let { if (it.status in listOf("queued", "running")) it.copy(status = "interrupted", errors = it.errors + "Android stopped the previous process; start a new search to resume") else it }
        } ?: error("Search session not found on this installation")
    }
    override suspend fun start(request: StartRequest): JobStarted = lock.withLock {
        check(LocalIdentity.load(app) != null) { "Enter your name to initialize the internal server" }
        check(InternalBackendService.active) { "Start the internal server from Settings" }
        require(request.query.trim().length in 2..240 && request.mode in listOf("opportunities", "links")) { "Invalid search" }
        require(request.engines.isNotEmpty() && request.engines.all { id -> catalog.any { it.id == id } }) { "Unknown or empty source selection" }
        check(current?.status !in listOf("running", "queued")) { "Stop the current swarm first" }
        val initial = JobStatus(UUID.randomUUID().toString(), "running", 0, 0, 0, emptyList(), emptyList())
        current = initial; persist(initial)
        worker = scope.launch { runPasses(initial.id, request) }
        JobStarted(initial.id, initial.status)
    }
    override suspend fun stop(id: String): JobStarted = lock.withLock {
        val old = current?.takeIf { it.id == id } ?: db.sessions().get(id)?.let { gson.fromJson(it.payload, JobStatus::class.java) } ?: error("Unknown session")
        if (current?.id == id) { worker?.cancel(); web.http.dispatcher.cancelAll(); current = old.copy(status = "stopped") }
        persist(old.copy(status = "stopped")); JobStarted(id, "stopped")
    }
    suspend fun shutdown() { current?.let { stop(it.id) } }
    private suspend fun persist(state: JobStatus) { db.sessions().put(LocalSession(state.id, gson.toJson(state))) }
    private suspend fun runPasses(id: String, config: StartRequest) {
        val visited = mutableSetOf<String>()
        try {
            val chunks = config.engines.distinct().chunked(8)
            while (currentCoroutineContext().isActive) {
                val old = current?.takeIf { it.id == id && it.status == "running" } ?: break
                val batch = web.search(config.query, chunks[old.round % chunks.size], config.mode, old.round / chunks.size)
                currentCoroutineContext().ensureActive()
                val found = batch.rows.toMutableList(); val errors = batch.errors.toMutableList()
                if (config.crawl) for (seed in batch.rows.filter { it.kind != "Link" && it.id !in visited }.take(2)) {
                    currentCoroutineContext().ensureActive(); visited += seed.id
                    try { found += web.crawl(seed, config.mode) } catch (e: Exception) { errors += "Page crawler: ${e.message}" }
                }
                currentCoroutineContext().ensureActive()
                lock.withLock {
                    val state = current?.takeIf { it.id == id && it.status == "running" } ?: return
                    val results = state.results.associateBy { it.id }.toMutableMap(); var dupes = state.duplicates
                    for (r in found) {
                        val previous = results[r.id]
                        if (previous != null) { dupes++; results[r.id] = previous.copy(sources = (previous.sources + r.sources).distinct()) }
                        else if (results.size < 1000) results[r.id] = r
                    }
                    val capacity = results.size >= 1000
                    val next = state.copy(status = if (capacity) "capacity" else "running", round = state.round + 1, duplicates = dupes, filtered = state.filtered + batch.filtered, errors = (errors + if (capacity) listOf("1,000-result device capacity reached; export and start another session") else emptyList()).take(20), results = results.values.sortedWith(compareByDescending<Opportunity> { it.published != null }.thenByDescending { it.published }.thenByDescending { it.score }))
                    current = next; persist(next)
                }
                delay(if (errors.isEmpty()) 75000L else 150000L)
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { lock.withLock { current?.takeIf { it.id == id && it.status == "running" }?.let { current = it.copy(status = "failed", errors = listOf("Backend: ${e.message}")); persist(current!!) } } }
    }
    fun details(): Map<String, Any> = mapOf("installationId" to (LocalIdentity.load(app)?.id ?: "not initialized"), "catalogSources" to catalog.size, "instance" to (web.instance ?: "not connected"), "reportedAdapters" to web.supported.size, "searchConnection" to web.connection, "runtime" to "Android/Kotlin/Room", "aiModelIncluded" to false)
    companion object {
        @Volatile private var instance: LocalBackend? = null
        fun get(context: Context): LocalBackend = instance ?: synchronized(this) { instance ?: LocalBackend(context).also { instance = it } }
    }
}

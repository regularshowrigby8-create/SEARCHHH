package info.plateaukao.einkbro.searchhh.local

import android.content.Context
import com.google.gson.Gson
import info.plateaukao.einkbro.searchhh.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** Backend API in the Android process. Normal UI calls never depend on a loopback socket. */
class LocalBackend private constructor(
    context: Context,
) : SearchhhApi {
    private val app = context.applicationContext
    private val gson = Gson()
    private val db = SearchhhDatabase.get(app)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Mutex()
    private var worker: Job? = null
    private var reviewWorker: Job? = null

    @Volatile private var current: JobStatus? = null
    val web = PublicSearch()
    private val portals = PortalCrawler(web)
    val codebases = CodebaseRegistry.load(app)
    val ai =
        info.plateaukao.einkbro.searchhh.ai
            .AiReviewer(app)
    private val catalog =
        gson
            .fromJson(
                app.assets.open("searchhh-portals.json").bufferedReader().use {
                    it.readText()
                },
                Array<Source>::class.java,
            ).toList()

    override suspend fun sources() = catalog

    override suspend fun status(id: String): JobStatus =
        lock.withLock {
            current?.takeIf { it.id == id } ?: db.sessions().get(id)?.let { row ->
                gson.fromJson(row.payload, JobStatus::class.java).let {
                    if (it.status in
                        listOf("queued", "running")
                    ) {
                        it.copy(
                            status = "interrupted",
                            errors =
                                it.errors + "Android stopped the previous process; start a new search to resume",
                        )
                    } else {
                        it
                    }
                }
            } ?: error("Search session not found on this installation")
        }

    override suspend fun start(request: StartRequest): JobStarted =
        lock.withLock {
            check(LocalIdentity.load(app) != null) { "Enter your name to initialize the internal server" }
            check(InternalBackendService.active) { "Start the internal server from Settings" }
            require(request.query.trim().length in 2..240 && request.mode in listOf("opportunities", "links")) { "Invalid search" }
            require(
                request.engines.isNotEmpty() &&
                    request.engines.all { id ->
                        catalog.any { it.id == id }
                    },
            ) { "Unknown or empty source selection" }
            val adapter =
                CrawlerAdapters.byId(request.adapter)
                    ?: CrawlerAdapters.default.takeIf { request.adapter == null }
                    ?: error("Unknown or unavailable crawler profile")
            check(current?.status !in listOf("running", "queued")) { "Stop the current swarm first" }
            val initial = JobStatus(UUID.randomUUID().toString(), "running", 0, 0, 0, emptyList(), emptyList(), adapter.id)
            current = initial
            persist(initial)
            worker = scope.launch { runPasses(initial.id, request, adapter) }
            reviewWorker = scope.launch { reviewPasses(initial.id, request.query) }
            JobStarted(initial.id, initial.status)
        }

    override suspend fun stop(id: String): JobStarted =
        lock.withLock {
            val old =
                current?.takeIf { it.id == id } ?: db.sessions().get(id)?.let { gson.fromJson(it.payload, JobStatus::class.java) }
                    ?: error("Unknown session")
            if (current?.id ==
                id
            ) {
                worker?.cancel()
                reviewWorker?.cancel()
                ai.cancel()
                web.http.dispatcher.cancelAll()
                current =
                    old.copy(status = "stopped")
            }
            persist(old.copy(status = "stopped"))
            JobStarted(id, "stopped")
        }

    suspend fun shutdown() {
        current?.let { stop(it.id) }
    }

    private suspend fun persist(state: JobStatus) {
        db.sessions().put(LocalSession(state.id, gson.toJson(state)))
    }

    private suspend fun runPasses(
        id: String,
        config: StartRequest,
        adapter: CrawlerAdapter,
    ) {
        portals.reset()
        try {
            val chunks = config.engines.distinct().chunked(8)
            while (currentCoroutineContext().isActive) {
                val old = current?.takeIf { it.id == id && it.status == "running" } ?: break
                val selected = chunks[old.round % chunks.size].map { key -> catalog.single { it.id == key } }
                val batch = portals.search(config.query, selected, config.mode, config.crawl, adapter)
                currentCoroutineContext().ensureActive()
                val found = batch.rows
                val errors = batch.errors
                lock.withLock {
                    val state = current?.takeIf { it.id == id && it.status == "running" } ?: return
                    val results = state.results.associateBy { it.id }.toMutableMap()
                    var dupes = state.duplicates
                    for (r in found) {
                        val previous = results[r.id]
                        if (previous != null) {
                            dupes++
                            // A changed excerpt invalidates the previous review.
                            results[r.id] =
                                r.copy(
                                    sources = (previous.sources + r.sources).distinct(),
                                    review = previous.review.takeIf { previous.title == r.title && previous.description == r.description },
                                )
                        } else {
                            results[r.id] = r
                        }
                    }
                    // Bounded rolling retention, not an automatic search stop. Saved items are separate.
                    val retained = results.values.sortedByDescending { it.checkedAt ?: it.discovered }.take(1000)
                    val next =
                        state.copy(
                            round = state.round + 1,
                            duplicates = dupes,
                            filtered = state.filtered + batch.filtered,
                            errors =
                                (
                                    errors +
                                        if (results.size >
                                            1000
                                        ) {
                                            listOf(
                                                "Rolling view retains 1,000 recently checked results; save/export important links. Search continues.",
                                            )
                                        } else {
                                            emptyList()
                                        }
                                ).take(20),
                            results = rank(retained),
                        )
                    current = next
                    persist(next)
                }
                delay(if (errors.isEmpty()) 75000L else 150000L)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reviewWorker?.cancel()
            ai.cancel()
            lock.withLock {
                current?.takeIf { it.id == id && it.status == "running" }?.let {
                    current =
                        it.copy(status = "failed", errors = listOf("Backend: ${e.message}"))
                    ; persist(current!!)
                }
            }
        }
    }

    private fun rank(rows: List<Opportunity>) =
        rows.sortedWith(
            compareByDescending<Opportunity> { it.review?.relevance ?: it.score }
                .thenByDescending { it.published }
                .thenByDescending { LocalPolicy.isForm(it.url) },
        )

    /** Independent consumer: inference latency/quotas never hold up the crawler. */
    private suspend fun reviewPasses(
        id: String,
        query: String,
    ) {
        while (currentCoroutineContext().isActive) {
            val state = current?.takeIf { it.id == id && it.status == "running" } ?: return
            val rows = state.results.filter { it.review == null }.take(4)
            if (rows.isNotEmpty()) {
                val revision = ai.vault.revision()
                val reviews = ai.review(query, rows)
                currentCoroutineContext().ensureActive()
                lock.withLock {
                    val latest = current?.takeIf { it.id == id && it.status == "running" } ?: return
                    if (reviews.isNotEmpty() && ai.vault.enabled() && ai.vault.revision() == revision) {
                        val originals = rows.associateBy { it.id }
                        val next =
                            latest.copy(
                                results =
                                    rank(
                                        latest.results.map { row ->
                                            val old = originals[row.id]
                                            if (old != null && old.title == row.title && old.description == row.description) {
                                                row.copy(review = reviews[row.id] ?: row.review)
                                            } else {
                                                row
                                            }
                                        },
                                    ),
                            )
                        current = next
                        persist(next)
                    }
                }
            }
            delay(180000L)
        }
    }

    fun details(): Map<String, Any> =
        mapOf(
            "installationId" to (LocalIdentity.load(app)?.id ?: "not initialized"),
            "catalogSources" to catalog.size,
            "catalogCodebases" to codebases.entries.size,
            "codebaseExecution" to "Four reviewed Android-native profiles; other catalogue entries are not installed runtimes",
            "searchConnection" to "Direct global portal crawl; no general search engine required",
            "runtime" to "Android/Kotlin/Room",
            "crawlerProfiles" to CrawlerAdapters.available.map { "${it.id}: ${it.implementation}" },
            "aiModelIncluded" to false,
            "externalAiEnabled" to ai.vault.enabled(),
            "aiStatus" to ai.status,
            "retention" to "1,000 recently checked results per session; saved results retained separately",
        )

    companion object {
        @Volatile private var instance: LocalBackend? = null

        fun get(context: Context): LocalBackend =
            instance ?: synchronized(this) { instance ?: LocalBackend(context).also { instance = it } }
    }
}

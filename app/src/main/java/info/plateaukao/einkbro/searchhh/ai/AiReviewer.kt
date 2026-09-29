package info.plateaukao.einkbro.searchhh.ai

import android.content.Context
import com.google.gson.JsonParser
import info.plateaukao.einkbro.searchhh.Opportunity
import info.plateaukao.einkbro.searchhh.EvidenceReview
import info.plateaukao.einkbro.searchhh.local.PublicSearch
import info.plateaukao.einkbro.searchhh.local.awaitPage
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** External inference only, no crawler methods or MCP tools available to models. */
class AiReviewer(context: Context) {
    val vault = AiVault(context)
    private val http = PublicSearch().http.newBuilder().callTimeout(45, TimeUnit.SECONDS).build()
    @Volatile var status = "AI review off; crawling works without an API key"
        private set
    @Volatile var freeModels: List<String> = emptyList()
        private set
    @Volatile private var refreshed = 0L
    private var cursor = 0
    private val cooldown = java.util.concurrent.ConcurrentHashMap<String, Long>()
    fun cancel() { http.dispatcher.cancelAll() }
    fun settingsChanged() { cancel(); cooldown.clear(); status = "AI settings changed; next review uses current consent" }
    suspend fun refreshCatalog(): List<String> {
        val page = http.newCall(Request.Builder().url("https://openrouter.ai/api/v1/models").build()).awaitPage(8L * 1024 * 1024)
        check(page.code == 200) { "Model catalog HTTP ${page.code}" }
        val models = AiPolicy.freeOpenRouterModels(page.text)
        freeModels = models; refreshed = System.currentTimeMillis()
        return models
    }
    suspend fun review(query: String, rows: List<Opportunity>): Map<String, EvidenceReview> {
        if (!vault.enabled()) { status = "AI review off"; return emptyMap() }
        val providers = AiPolicy.providers.filter { vault.hasKey(it.id) }
        if (providers.isEmpty()) { status = "Connect a free provider in Settings; crawler continues"; return emptyMap() }
        val revision = vault.revision()
        val provider = providers[Math.floorMod(cursor++, providers.size)]
        if ((cooldown[provider.id] ?: 0) > System.currentTimeMillis()) { status = "${provider.name}: cooling down; no key rotation"; return emptyMap() }
        val model = vault.model(provider.id)
        try {
            if (provider.id == "openrouter") {
                if (System.currentTimeMillis() - refreshed > 900000) refreshCatalog()
                check(model in freeModels) { "Selected free model no longer appears in catalog" }
            } else check(model in AiPolicy.zaiModels) { "Model not in published free allowlist" }
            currentCoroutineContext().ensureActive()
            if (vault.revision() != revision || !vault.enabled()) return emptyMap()
            val key = vault.key(provider.id) ?: return emptyMap()
            val page = http.newCall(Request.Builder().url(provider.base + "chat/completions")
                .header("Authorization", "Bearer $key")
                .post(AiPolicy.request(model, query, rows, provider.id == "openrouter").toRequestBody("application/json".toMediaType())).build()).awaitPage(128 * 1024L)
            if (page.code != 200) {
                cooldown[provider.id] = System.currentTimeMillis() + if (page.code in listOf(401, 402, 403)) 86400000L else maxOf(900000L, (page.retryAfter?.toLongOrNull()?.coerceIn(0, 86400) ?: 0) * 1000)
                // Deliberately do not log/echo provider error bodies or credentials.
                status = "${provider.name}: HTTP ${page.code}; review paused, no paid fallback"
                return emptyMap()
            }
            val content = JsonParser.parseString(page.text).asJsonObject.getAsJsonArray("choices")[0]
                .asJsonObject.getAsJsonObject("message")["content"].asString
            val reviewed = AiPolicy.parseReview(content, rows, "${provider.id}/$model")
            currentCoroutineContext().ensureActive()
            if (vault.revision() != revision || !vault.enabled()) return emptyMap()
            status = "Reviewed ${reviewed.size} records with ${provider.name}; AI judgment, not verified eligibility"
            return reviewed
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) {
            cooldown[provider.id] = System.currentTimeMillis() + 900000
            status = "${provider.name}: review unavailable or invalid; retry after cooldown. Crawling continues."
            return emptyMap()
        }
    }
}

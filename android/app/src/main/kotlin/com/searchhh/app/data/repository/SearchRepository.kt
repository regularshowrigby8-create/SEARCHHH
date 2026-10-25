package com.searchhh.app.data.repository

import com.searchhh.app.core.ApiResult
import com.searchhh.app.core.PAGE_SIZE
import com.searchhh.app.core.apiCall
import com.searchhh.app.data.model.CategoryCount
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.FundingResult
import com.searchhh.app.data.model.HealthInfo
import com.searchhh.app.data.model.Insight
import com.searchhh.app.data.model.SearchPage
import com.searchhh.app.data.remote.SearchApi
import com.searchhh.app.data.remote.dto.FeedbackRequestDto
import com.searchhh.app.data.remote.dto.FundingMatchRequestDto
import com.searchhh.app.data.remote.dto.InsightRequestDto
import com.searchhh.app.data.remote.dto.toDomain
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Search data source with a last-known-good cache.
 *
 * When the network fails we return the previous successful response flagged
 * `fromCache = true` instead of an error - that is what makes the demo work in
 * a lecture hall with terrible Wi-Fi.
 */
interface SearchRepository {
    suspend fun search(
        query: String,
        limit: Int = PAGE_SIZE,
        offset: Int = 0,
        category: String? = null,
        sort: String = "relevance",
    ): ApiResult<SearchPage>

    suspend fun suggest(query: String): List<String>
    suspend fun categories(): ApiResult<List<CategoryCount>>
    suspend fun entry(id: String): ApiResult<Entry>
    suspend fun insight(entryId: String, audience: String = "first-time buyer"): ApiResult<Insight>
    suspend fun feedback(message: String, rating: Int, entryId: String? = null): ApiResult<String>
    suspend fun fundingMatch(
        age: Int,
        stage: String,
        registered: Boolean,
        trainingCompleted: Boolean,
        annualTurnover: Double = 0.0,
    ): ApiResult<FundingResult>

    suspend fun health(): ApiResult<HealthInfo>
    fun invalidateCache()
}

class RemoteSearchRepository(
    private val api: SearchApi,
    private val appVersion: String,
) : SearchRepository {

    private val cache = ConcurrentHashMap<String, SearchPage>()
    private val cacheMutex = Mutex()
    private var lastCategories: List<CategoryCount> = emptyList()

    private fun key(query: String, category: String?, sort: String, offset: Int): String =
        "$query|${category ?: ""}|$sort|$offset"

    override suspend fun search(
        query: String,
        limit: Int,
        offset: Int,
        category: String?,
        sort: String,
    ): ApiResult<SearchPage> {
        val cacheKey = key(query, category, sort, offset)
        val result = apiCall {
            api.search(
                query = query.trim(),
                limit = limit,
                offset = offset,
                category = category?.takeIf { it.isNotBlank() },
                sort = sort,
            ).toDomain()
        }
        return when (result) {
            is ApiResult.Success -> {
                cacheMutex.withLock { if (offset == 0) cache[cacheKey] = result.data }
                result
            }
            is ApiResult.Failure -> {
                val cached = cache[cacheKey]
                if (cached != null) {
                    Timber.w("Serving cached results for '%s' after %s", query, result.error)
                    ApiResult.Success(cached.copy(fromCache = true))
                } else {
                    result
                }
            }
            ApiResult.Loading -> result
        }
    }

    override suspend fun suggest(query: String): List<String> {
        if (query.length < 2) return emptyList()
        return when (val result = apiCall { api.suggest(query, limit = 5) }) {
            is ApiResult.Success -> result.data.suggestions
            is ApiResult.Failure -> emptyList()
            ApiResult.Loading -> emptyList()
        }
    }

    override suspend fun categories(): ApiResult<List<CategoryCount>> = when (
        val result = apiCall { api.categories().toDomain() }
    ) {
        is ApiResult.Success -> {
            lastCategories = result.data
            result
        }
        is ApiResult.Failure ->
            if (lastCategories.isNotEmpty()) {
                ApiResult.Success(lastCategories)
            } else {
                ApiResult.Failure(result.error, result.cause)
            }
        ApiResult.Loading -> ApiResult.Loading
    }

    override suspend fun entry(id: String): ApiResult<Entry> {
        val direct: ApiResult<Entry> = apiCall { api.entry(id).toDomain() }
        if (direct is ApiResult.Success) return ApiResult.Success(direct.data)
        // Offline: try to reconstruct it from a cached page.
        cache.values.firstOrNull { page -> page.results.any { it.id == id } }
            ?.let { page -> page.results.first { it.id == id } }
            ?.let { return ApiResult.Success(it) }
        return direct
    }

    override suspend fun insight(entryId: String, audience: String): ApiResult<Insight> =
        when (val result = apiCall { api.insight(InsightRequestDto(entryId, audience)).toDomain() }) {
            is ApiResult.Success -> ApiResult.Success(result.data)
            is ApiResult.Failure -> ApiResult.Failure(result.error, result.cause)
            ApiResult.Loading -> ApiResult.Loading
        }

    override suspend fun feedback(message: String, rating: Int, entryId: String?): ApiResult<String> =
        when (
            val result = apiCall {
                api.feedback(
                    FeedbackRequestDto(
                        message = message,
                        rating = rating,
                        entryId = entryId,
                        appVersion = appVersion,
                    ),
                )
            }
        ) {
            is ApiResult.Success -> ApiResult.Success(result.data.id)
            is ApiResult.Failure -> ApiResult.Failure(result.error, result.cause)
            ApiResult.Loading -> ApiResult.Loading
        }

    override suspend fun fundingMatch(
        age: Int,
        stage: String,
        registered: Boolean,
        trainingCompleted: Boolean,
        annualTurnover: Double,
    ): ApiResult<FundingResult> = when (
        val result = apiCall {
            api.fundingMatch(
                FundingMatchRequestDto(
                    age = age,
                    stage = stage,
                    registered = registered,
                    trainingCompleted = trainingCompleted,
                    annualTurnover = annualTurnover,
                ),
            ).toDomain()
        }
    ) {
        is ApiResult.Success -> ApiResult.Success(result.data)
        is ApiResult.Failure -> ApiResult.Failure(result.error, result.cause)
        ApiResult.Loading -> ApiResult.Loading
    }

    override suspend fun health(): ApiResult<HealthInfo> =
        when (val result = apiCall { api.health().toDomain() }) {
            is ApiResult.Success -> ApiResult.Success(result.data)
            is ApiResult.Failure -> ApiResult.Failure(result.error, result.cause)
            ApiResult.Loading -> ApiResult.Loading
        }

    override fun invalidateCache() {
        cache.clear()
        lastCategories = emptyList()
        Timber.d("Search cache invalidated")
    }
}

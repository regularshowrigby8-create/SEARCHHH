package com.searchhh.app.data

import com.searchhh.app.data.local.RecentQueryDao
import com.searchhh.app.data.local.RecentQueryEntity
import com.searchhh.app.data.local.SavedDao
import com.searchhh.app.data.local.SavedEntryEntity
import com.searchhh.app.core.ApiResult
import com.searchhh.app.data.model.CategoryCount
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.HealthInfo
import com.searchhh.app.data.model.Insight
import com.searchhh.app.data.model.SearchPage
import com.searchhh.app.data.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Hand-written fakes. No mockk/Mockito: fakes are faster, readable, and keep
 * the test dependency list short.
 */

val sampleEntry = Entry(
    id = "svc-001",
    title = "Campus Coffee Run",
    subtitle = "Same-day delivery across campus",
    description = "Barista coffee delivered.",
    category = "food",
    tags = listOf("coffee", "delivery"),
    price = 28.0,
    rating = 4.7,
)

class FakeSearchRepository(
    private val page: SearchPage = SearchPage(
        query = "",
        total = 2,
        tookMs = 1.2,
        results = listOf(sampleEntry, sampleEntry.copy(id = "svc-002", title = "Meal Prep")),
    ),
    private val failure: ApiResult.Failure? = null,
) : SearchRepository {

    var lastQuery: String? = null
        private set
    var lastSort: String? = null
        private set

    override suspend fun search(
        query: String,
        limit: Int,
        offset: Int,
        category: String?,
        sort: String,
    ): ApiResult<SearchPage> {
        lastQuery = query
        lastSort = sort
        failure?.let { return it }
        return ApiResult.Success(page.copy(query = query))
    }

    override suspend fun suggest(query: String): List<String> = listOf("coffee", "coffee beans")

    override suspend fun categories(): ApiResult<List<CategoryCount>> =
        ApiResult.Success(listOf(CategoryCount("food", 3), CategoryCount("services", 5)))

    override suspend fun entry(id: String): ApiResult<Entry> =
        page.results.firstOrNull { it.id == id }?.let { ApiResult.Success(it) }
            ?: ApiResult.Failure(com.searchhh.app.core.AppError.UNKNOWN)

    override suspend fun insight(entryId: String, audience: String): ApiResult<Insight> =
        ApiResult.Success(Insight(entryId = entryId, text = "Great for early lectures.", provider = "heuristic"))

    override suspend fun feedback(message: String, rating: Int, entryId: String?): ApiResult<String> =
        ApiResult.Success("feedback-1")

    override suspend fun health(): ApiResult<HealthInfo> =
        ApiResult.Success(HealthInfo(status = "ok", service = "SEARCHHH API", version = "0.1.0", environment = "ci"))

    override fun invalidateCache() = Unit
}

class FakeSavedDao : SavedDao {
    private val state = MutableStateFlow<List<SavedEntryEntity>>(emptyList())

    override fun observeAll(): Flow<List<SavedEntryEntity>> = state

    override fun observeIsSaved(id: String): Flow<Boolean> = flowOf(state.value.any { it.id == id })

    override suspend fun find(id: String): SavedEntryEntity? = state.value.firstOrNull { it.id == id }

    override suspend fun upsert(entity: SavedEntryEntity) {
        state.value = state.value.filterNot { it.id == entity.id } + entity
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun clear() {
        state.value = emptyList()
    }

    override suspend fun count(): Int = state.value.size
}

class FakeRecentQueryDao : RecentQueryDao {
    private val state = MutableStateFlow<List<RecentQueryEntity>>(emptyList())

    override fun observeRecent(): Flow<List<RecentQueryEntity>> = state

    override suspend fun upsert(entity: RecentQueryEntity) {
        state.value = listOf(entity) + state.value.filterNot { it.query == entity.query }
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}

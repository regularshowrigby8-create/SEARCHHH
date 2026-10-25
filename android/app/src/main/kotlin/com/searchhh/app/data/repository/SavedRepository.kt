package com.searchhh.app.data.repository

import com.searchhh.app.data.local.RecentQueryDao
import com.searchhh.app.data.local.RecentQueryEntity
import com.searchhh.app.data.local.SavedDao
import com.searchhh.app.data.local.SavedEntryEntity
import com.searchhh.app.data.model.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/** Offline bookmarks (Room). */
class SavedRepository(private val dao: SavedDao) {

    val saved: Flow<List<Entry>> = dao.observeAll().map { rows -> rows.map(SavedEntryEntity::toDomain) }

    fun isSaved(entryId: String): Flow<Boolean> = dao.observeIsSaved(entryId)

    /** @return true when the entry is saved after toggling. */
    suspend fun toggle(entry: Entry): Boolean {
        val existing = dao.find(entry.id)
        return if (existing == null) {
            dao.upsert(SavedEntryEntity.from(entry))
            Timber.d("Saved %s", entry.id)
            true
        } else {
            dao.delete(entry.id)
            Timber.d("Unsaved %s", entry.id)
            false
        }
    }

    suspend fun remove(entryId: String) = dao.delete(entryId)

    suspend fun clear() = dao.clear()

    suspend fun count(): Int = dao.count()
}

/** Last 20 searches, used for the home screen "recent" chips. */
class RecentQueriesRepository(private val dao: RecentQueryDao) {

    val recent: Flow<List<String>> = dao.observeRecent().map { rows -> rows.map { it.query } }

    suspend fun record(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        dao.upsert(RecentQueryEntity(query = trimmed, searchedAt = System.currentTimeMillis()))
    }

    suspend fun clear() = dao.clear()
}

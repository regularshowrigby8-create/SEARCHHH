package com.searchhh.app.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.searchhh.app.data.model.Entry
import kotlinx.coroutines.flow.Flow

/**
 * Room database - the offline half of the app.
 *
 * Two tables:
 *  * `saved_entries`  bookmarks, available with no network
 *  * `recent_queries` last 20 unique searches, powers the Home suggestions
 */
@Entity(tableName = "saved_entries")
data class SavedEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: String,
    val tags: String,
    val price: Double?,
    val currency: String,
    val rating: Double,
    val imageUrl: String?,
    val location: String?,
    val url: String?,
    val savedAt: Long,
) {
    fun toDomain(): Entry = Entry(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        category = category,
        tags = tags.split("|").filter { it.isNotBlank() },
        price = price,
        currency = currency,
        rating = rating,
        imageUrl = imageUrl,
        location = location,
        url = url,
    )

    companion object {
        fun from(entry: Entry, savedAt: Long = System.currentTimeMillis()) = SavedEntryEntity(
            id = entry.id,
            title = entry.title,
            subtitle = entry.subtitle,
            description = entry.description,
            category = entry.category,
            tags = entry.tags.joinToString("|"),
            price = entry.price,
            currency = entry.currency,
            rating = entry.rating,
            imageUrl = entry.imageUrl,
            location = entry.location,
            url = entry.url,
            savedAt = savedAt,
        )
    }
}

@Entity(tableName = "recent_queries")
data class RecentQueryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long,
)

@Dao
interface SavedDao {

    @Query("SELECT * FROM saved_entries ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedEntryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_entries WHERE id = :id)")
    fun observeIsSaved(id: String): Flow<Boolean>

    @Query("SELECT * FROM saved_entries WHERE id = :id LIMIT 1")
    suspend fun find(id: String): SavedEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedEntryEntity)

    @Query("DELETE FROM saved_entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM saved_entries")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM saved_entries")
    suspend fun count(): Int
}

@Dao
interface RecentQueryDao {

    @Query("SELECT * FROM recent_queries ORDER BY searchedAt DESC LIMIT 20")
    fun observeRecent(): Flow<List<RecentQueryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentQueryEntity)

    @Query("DELETE FROM recent_queries")
    suspend fun clear()
}

@Database(
    entities = [SavedEntryEntity::class, RecentQueryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SearchhhDatabase : RoomDatabase() {
    abstract fun savedDao(): SavedDao
    abstract fun recentQueryDao(): RecentQueryDao

    companion object {
        private const val NAME = "searchhh.db"

        fun build(context: Context): SearchhhDatabase =
            Room.databaseBuilder(context.applicationContext, SearchhhDatabase::class.java, NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}

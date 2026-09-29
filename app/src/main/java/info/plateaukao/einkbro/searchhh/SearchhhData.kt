package info.plateaukao.einkbro.searchhh

import android.content.Context
import androidx.room.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Integration contracts, not a replacement browser/search engine implementation.
data class Source(val id: String, val name: String, val category: String, val default: Boolean = false)
data class StartRequest(val query: String, val mode: String, val engines: List<String>, val crawl: Boolean)
data class JobStarted(val id: String, val status: String)
data class Opportunity(val id: String, val url: String, val title: String, val description: String, val kind: String, val sources: List<String>, val published: String?, val discovered: String, val score: Int, val verified: Boolean)
data class JobStatus(val id: String, val status: String, val round: Int, val duplicates: Int, val filtered: Int, val errors: List<String>, val results: List<Opportunity>)
interface SearchhhApi {
    @GET("v1/engines") suspend fun sources(): List<Source>
    @POST("v1/jobs") suspend fun start(@Body request: StartRequest): JobStarted
    @GET("v1/jobs/{id}") suspend fun status(@Path("id") id: String): JobStatus
    @POST("v1/jobs/{id}/stop") suspend fun stop(@Path("id") id: String): JobStarted
}

object ConnectionSettings {
    private var cachedAddress = ""
    private var cachedToken = ""
    private var cachedApi: SearchhhApi? = null
    private var cachedClient: OkHttpClient? = null
    @Suppress("DEPRECATION")
    fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context.applicationContext, "searchhh_connection",
        MasterKey.Builder(context.applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    @Synchronized fun api(context: Context): SearchhhApi {
        val p = prefs(context)
        if (p.getString("backend_mode", "internal") == "internal") return info.plateaukao.einkbro.searchhh.local.LocalBackend.get(context)
        val url = p.getString("url", "").orEmpty().trim().trimEnd('/') + "/"
        require(validServerUrl(url)) { "Set your HTTPS backend address in Settings first" }
        val token = p.getString("token", "").orEmpty()
        require(token.length >= 32) { "Enter the access token for your own server" }
        if (url == cachedAddress && token == cachedToken) cachedApi?.let { return it }
        cachedClient?.connectionPool?.evictAll()
        cachedClient?.dispatcher?.executorService?.shutdown()
        val client = OkHttpClient.Builder().callTimeout(35, TimeUnit.SECONDS)
            .followRedirects(false).followSslRedirects(false)
            .addInterceptor { chain -> chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer $token").build()) }.build()
        return Retrofit.Builder().baseUrl(url).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(SearchhhApi::class.java).also {
            cachedAddress = url; cachedToken = token; cachedApi = it; cachedClient = client
        }
    }
    fun validServerUrl(value: String): Boolean = try {
        val uri = java.net.URI(value)
        uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null && uri.query == null && uri.fragment == null
    } catch (_: Exception) { false }
}

@Entity(tableName = "saved_opportunities")
data class SavedOpportunity(@PrimaryKey val id: String, val payload: String, val savedAt: Long = System.currentTimeMillis())
@Dao
interface SavedDao {
    @Query("SELECT * FROM saved_opportunities ORDER BY savedAt DESC") fun all(): Flow<List<SavedOpportunity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(row: SavedOpportunity)
    @Query("DELETE FROM saved_opportunities WHERE id = :id") suspend fun remove(id: String)
}
@Entity(tableName = "local_sessions")
data class LocalSession(@PrimaryKey val id: String, val payload: String)
@Dao
interface LocalSessionDao {
    @Query("SELECT * FROM local_sessions WHERE id = :id") suspend fun get(id: String): LocalSession?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(value: LocalSession)
}
@Database(entities = [SavedOpportunity::class, LocalSession::class], version = 2, exportSchema = true)
abstract class SearchhhDatabase : RoomDatabase() {
    abstract fun saved(): SavedDao
    abstract fun sessions(): LocalSessionDao
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) { db.execSQL("CREATE TABLE IF NOT EXISTS `local_sessions` (`id` TEXT NOT NULL, `payload` TEXT NOT NULL, PRIMARY KEY(`id`))") }
        }
        @Volatile private var instance: SearchhhDatabase? = null
        fun get(context: Context): SearchhhDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, SearchhhDatabase::class.java, "searchhh.db").addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}

/** WorkManager only refreshes status; continuous crawling belongs on the server. */
class SearchhhSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val p = ConnectionSettings.prefs(applicationContext)
        val id = p.getString("job", null)
        if (id == null) Result.success() else {
            val state = ConnectionSettings.api(applicationContext).status(id)
            p.edit().putString("last_status", Gson().toJson(state)).apply()
            Result.success()
        }
    } catch (_: Exception) { Result.retry() }
}

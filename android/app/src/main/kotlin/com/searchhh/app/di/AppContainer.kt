package com.searchhh.app.di

import android.content.Context
import com.searchhh.app.BuildConfig
import com.searchhh.app.core.DefaultDispatcherProvider
import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.data.local.SearchhhDatabase
import com.searchhh.app.data.local.SettingsRepository
import com.searchhh.app.data.local.settingsDataStore
import com.searchhh.app.data.remote.NetworkModule
import com.searchhh.app.data.remote.SearchApi
import com.searchhh.app.data.repository.RecentQueriesRepository
import com.searchhh.app.data.repository.RemoteSearchRepository
import com.searchhh.app.data.repository.SavedRepository
import com.searchhh.app.data.repository.SearchRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Manual service locator (deliberately not Hilt: one file, zero annotation
 * processing, and the whole dependency graph reads top-to-bottom).
 *
 * Lifetimes are process-wide; swap the implementation in tests by constructing
 * the repositories directly.
 */
class AppContainer(
    context: Context,
    val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) {
    private val appContext: Context = context.applicationContext

    val settingsRepository: SettingsRepository = SettingsRepository(appContext.settingsDataStore)

    private val database: SearchhhDatabase = SearchhhDatabase.build(appContext)
    val savedRepository: SavedRepository = SavedRepository(database.savedDao())
    val recentQueriesRepository: RecentQueriesRepository = RecentQueriesRepository(database.recentQueryDao())

    @Volatile
    private var baseUrl: String = BuildConfig.API_BASE_URL

    private val okHttpClient = NetworkModule.okHttpClient { baseUrl }
    private val retrofit = NetworkModule.retrofit(okHttpClient)
    private val api: SearchApi = NetworkModule.searchApi(retrofit)

    val searchRepository: SearchRepository = RemoteSearchRepository(
        api = api,
        appVersion = BuildConfig.VERSION_NAME,
    )

    /** Current base URL actually used by the HTTP layer. */
    fun apiBaseUrl(): String = baseUrl

    /** Repoint Retrofit at another host (Settings screen). */
    fun updateBaseUrl(url: String) {
        val normalised = if (url.endsWith("/")) url else "$url/"
        baseUrl = normalised
        Timber.i("API base URL set to %s", normalised)
    }

    /** Called once from Application.onCreate in a background scope. */
    suspend fun hydrate() {
        val persisted = settingsRepository.settings.first()
        updateBaseUrl(persisted.apiBaseUrl)
    }
}

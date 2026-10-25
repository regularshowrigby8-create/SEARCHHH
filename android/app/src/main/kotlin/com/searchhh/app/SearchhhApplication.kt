package com.searchhh.app

import android.app.Application
import com.searchhh.app.di.AppContainer
import com.searchhh.app.push.SearchhhMessagingService
import com.searchhh.app.telemetry.Telemetry
import com.searchhh.app.work.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Process entry point.
 *
 * Order matters: logging/crash reporting first (so startup crashes are
 * captured), then the dependency graph, then background scheduling.
 */
class SearchhhApplication : Application() {

    lateinit var container: AppContainer
        private set

    /** Process-lived scope for fire-and-forget startup work. */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)
        Telemetry.init(this, telemetryEnabled = true)

        SearchhhMessagingService.ensureChannel(this)

        applicationScope.launch {
            container.hydrate()
            Timber.i("Containers hydrated (baseUrl=%s)", container.apiBaseUrl())
        }

        SyncWorker.schedule(this)

        Timber.i("SEARCHHH %s (%s) started", BuildConfig.VERSION_NAME, BuildConfig.GIT_SHA)
        if (BuildConfig.DEBUG) {
            Telemetry.log("debug build started")
        }
    }
}

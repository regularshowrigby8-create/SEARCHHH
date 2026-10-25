package com.searchhh.app.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.searchhh.app.SearchhhApplication
import com.searchhh.app.core.ApiResult
import com.searchhh.app.telemetry.Telemetry
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Periodic background sync (WorkManager).
 *
 * Refreshes the catalog index and backend health so the next cold start can
 * show fresh data instantly, and records the timestamp in DataStore.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SearchhhApplication ?: return Result.success()
        val container = app.container

        return try {
            val health = container.searchRepository.health()
            val categories = container.searchRepository.categories()

            val ok = health is ApiResult.Success && categories is ApiResult.Success
            if (ok) {
                container.settingsRepository.markSynced()
                Telemetry.log("Background sync completed")
                Timber.i("Background sync OK")
                Result.success()
            } else {
                Timber.w("Background sync incomplete: %s / %s", health, categories)
                if (runAttemptCount < 3) Result.retry() else Result.success()
            }
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Background sync failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "searchhh-periodic-sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .addTag("sync")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
            Timber.i("Scheduled periodic sync (6h)")
        }
    }
}

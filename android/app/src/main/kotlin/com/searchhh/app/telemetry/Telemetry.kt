package com.searchhh.app.telemetry

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.searchhh.app.BuildConfig
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import timber.log.Timber

/**
 * One place that owns crash reporting, analytics and push tokens.
 *
 * Everything is *optional and guarded*: a clone with no google-services.json
 * and no Sentry DSN boots cleanly, logs a line, and continues.
 */
object Telemetry {

    private const val TAG = "Telemetry"

    @Volatile
    private var sentryReady = false

    @Volatile
    private var firebaseReady = false

    fun init(context: Context, telemetryEnabled: Boolean) {
        plantTrees()
        Timber.i("Telemetry init: enabled=%s debug=%s", telemetryEnabled, BuildConfig.DEBUG)
        initSentry(context, telemetryEnabled)
        initFirebase(context, telemetryEnabled)
    }

    private fun plantTrees() {
        // Debug: full logs. Release: silence local logging; Sentry/Crashlytics
        // capture what matters, and PII never lands in logcat.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initSentry(context: Context, enabled: Boolean) {
        val dsn = BuildConfig.SENTRY_DSN
        if (!enabled) {
            Timber.tag(TAG).i("Sentry disabled by user preference")
            return
        }
        if (dsn.isBlank()) {
            Timber.tag(TAG).i("No SENTRY_DSN configured - crash reporting stays local")
            return
        }
        runCatching {
            SentryAndroid.init(context) { options ->
                options.dsn = dsn
                options.environment = BuildConfig.SENTRY_ENVIRONMENT
                options.release = "com.searchhh.app@${BuildConfig.VERSION_NAME}+${BuildConfig.GIT_SHA}"
                options.tracesSampleRate = if (BuildConfig.DEBUG) 0.0 else 0.2
                options.isEnableAutoSessionTracking = true
                options.isAnrEnabled = true
                options.isSendDefaultPii = false
                options.setDiagnosticLevel(if (BuildConfig.DEBUG) SentryLevel.DEBUG else SentryLevel.ERROR)
            }
            sentryReady = true
        }.onFailure { Timber.tag(TAG).w(it, "Sentry init failed") }
    }

    private fun initFirebase(context: Context, enabled: Boolean) {
        if (!BuildConfig.FIREBASE_CONFIGURED) {
            Timber.tag(TAG).i("google-services.json absent - Firebase features are inert")
            return
        }
        runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                Timber.tag(TAG).w("FirebaseApp not initialised - skipping Crashlytics")
                return
            }
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enabled)
            firebaseReady = true
            Timber.tag(TAG).i("Firebase ready")
        }.onFailure { Timber.tag(TAG).w(it, "Firebase init failed") }
    }

    /** Non-fatal error: recorded in Sentry *and* Crashlytics. */
    fun sendTestError(reason: String) {
        val error = IllegalStateException("SEARCHHH test error: $reason @ ${System.currentTimeMillis()}")
        Timber.tag(TAG).e(error, "Test error requested")
        if (sentryReady) runCatching { Sentry.captureException(error) }
        if (firebaseReady) runCatching { FirebaseCrashlytics.getInstance().recordException(error) }
    }

    fun log(message: String) {
        Timber.tag(TAG).i(message)
        if (firebaseReady) runCatching { FirebaseCrashlytics.getInstance().log(message) }
        if (sentryReady) runCatching { Sentry.addBreadcrumb(message) }
    }

    fun currentPushToken(onResult: (String?) -> Unit) {
        if (!BuildConfig.FIREBASE_CONFIGURED) {
            onResult(null)
            return
        }
        runCatching {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                onResult(if (task.isSuccessful) task.result else null)
            }
        }.onFailure {
            Timber.tag(TAG).w(it, "Token request failed")
            onResult(null)
        }
    }
}

package com.searchhh.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.searchhh.app.data.local.AppSettings
import com.searchhh.app.ui.navigation.SearchhhNavGraph
import com.searchhh.app.ui.theme.SearchhhTheme
import timber.log.Timber

/**
 * The single activity. Compose owns everything below this line.
 *
 * Theme (light/dark) comes from DataStore so the user's choice survives
 * process death, with the system setting as the fallback.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate to swap in the splash theme.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as SearchhhApplication).container
        val deepLinkQuery = resolveDeepLinkQuery(intent)
        Timber.i("MainActivity started (deepLinkQuery=%s)", deepLinkQuery)

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            SearchhhTheme(darkTheme = settings.darkMode ?: isSystemInDarkTheme()) {
                SearchhhNavGraph(container = container, deepLinkQuery = deepLinkQuery)
            }
        }
    }

    /** Supports both `searchhh://search?q=coffee` and the push extra. */
    private fun resolveDeepLinkQuery(intent: Intent?): String? {
        if (intent == null) return null
        val fromUri = intent.data?.getQueryParameter("q")
        if (!fromUri.isNullOrBlank()) return fromUri
        return intent.getStringExtra(EXTRA_QUERY)?.takeIf { it.isNotBlank() }
    }

    companion object {
        const val EXTRA_QUERY = "extra_query"
    }
}

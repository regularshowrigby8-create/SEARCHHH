package info.plateaukao.einkbro.searchhh.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.gson.Gson
import info.plateaukao.einkbro.preference.BrowserConfig
import info.plateaukao.einkbro.searchhh.ConnectionSettings
import info.plateaukao.einkbro.searchhh.JobStatus
import info.plateaukao.einkbro.searchhh.Opportunity
import info.plateaukao.einkbro.searchhh.SavedOpportunity
import info.plateaukao.einkbro.searchhh.SearchhhDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * App-owned result/browser surface. It deliberately does not reuse EinkBro's
 * tab WebView or its privileged JavaScript bridges.
 */
class SearchhhResultsWebViewActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var statusText: TextView
    private val gson = Gson()
    private val dao by lazy { SearchhhDatabase.get(this).saved() }
    private var sessionId: String? = null
    private var currentStatus: JobStatus? = null
    private var savedIds: Set<String> = emptySet()
    private var lastExternalUrl: String? = null
    private var showingResults = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        buildView()
        configureWebView()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) webView.goBack() else finish()
                }
            },
        )
        if (intent.getStringExtra(EXTRA_URL) != null) {
            openExternal(intent.getStringExtra(EXTRA_URL).orEmpty())
        } else if (intent.getBooleanExtra(EXTRA_SAVED, false)) {
            observeSaved()
        } else if (sessionId != null) {
            observeSession()
        } else {
            showError("No Searchhh document was supplied")
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }

    private fun buildView() {
        val root =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.TRANSPARENT)
            }
        val toolbar =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12, 8, 12, 8)
                setBackgroundColor(if (isDarkTheme()) Color.rgb(26, 33, 28) else Color.WHITE)
            }
        val back =
            Button(this).apply {
                text = "Back"
                contentDescription = "Go back or close results"
                setOnClickListener { if (webView.canGoBack()) webView.goBack() else finish() }
            }
        statusText =
            TextView(this).apply {
                text = "Searchhh WebView"
                textSize = 16f
                setTextColor(if (isDarkTheme()) Color.WHITE else Color.rgb(24, 32, 24))
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12, 0, 12, 0)
            }
        val reload =
            Button(this).apply {
                text = "Reload"
                contentDescription = "Reload search results"
                setOnClickListener {
                    if (sessionId != null && showingResults) refreshNow() else webView.reload()
                }
            }
        toolbar.addView(back, LinearLayout.LayoutParams(WRAP, WRAP))
        toolbar.addView(statusText, LinearLayout.LayoutParams(0, WRAP, 1f))
        toolbar.addView(reload, LinearLayout.LayoutParams(WRAP, WRAP))
        webView = WebView(this)
        root.addView(toolbar, LinearLayout.LayoutParams(MATCH, WRAP))
        root.addView(webView, LinearLayout.LayoutParams(MATCH, 0, 1f))
        setContentView(root)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.setBackgroundColor(if (isDarkTheme()) Color.rgb(16, 20, 18) else Color.rgb(246, 248, 243))
        webView.settings.apply {
            // External result pages may need JavaScript to render. This surface
            // installs no JavaScript bridge, so third-party pages cannot call app APIs.
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            blockNetworkLoads = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, isDarkTheme())
        }
        // Touches AndroidX WebKit at runtime and keeps this surface on the
        // supported WebView implementation rather than legacy Chromium hooks.
        WebViewCompat.getCurrentWebViewPackage(this)
        webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean = handleNavigation(request.url)

                override fun onReceivedSslError(
                    view: WebView,
                    handler: SslErrorHandler,
                    error: android.net.http.SslError,
                ) {
                    handler.cancel()
                    Toast.makeText(this@SearchhhResultsWebViewActivity, "Secure connection blocked", Toast.LENGTH_SHORT).show()
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError,
                ) {
                    if (request.isForMainFrame) showError(error.description?.toString() ?: "The page could not be loaded")
                }

                override fun onPageFinished(
                    view: WebView,
                    url: String,
                ) {
                    val uri = Uri.parse(url)
                    if (isResultsOrigin(uri) && currentStatus != null) showingResults = true
                    statusText.text =
                        if (isResultsOrigin(uri)) "Searchhh results" else uri.host ?: "Searchhh WebView"
                }

                override fun onRenderProcessGone(
                    view: WebView,
                    detail: android.webkit.RenderProcessGoneDetail,
                ): Boolean {
                    showError("The WebView renderer stopped unexpectedly")
                    return true
                }
            }
    }

    private fun observeSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    dao.all().collectLatest { rows ->
                        savedIds = rows.mapTo(mutableSetOf()) { it.id }
                        currentStatus?.takeIf { showingResults }?.let(::renderResults)
                    }
                }
                launch {
                    while (isActive) {
                        if (showingResults || currentStatus == null) refreshNow()
                        delay(5000)
                    }
                }
            }
        }
    }

    private fun observeSaved() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.all().collectLatest { rows ->
                    savedIds = rows.mapTo(mutableSetOf()) { it.id }
                    val results = rows.mapNotNull { row -> runCatching { gson.fromJson(row.payload, Opportunity::class.java) }.getOrNull() }
                    renderResults(JobStatus("saved", "saved", 0, 0, 0, emptyList(), results, "saved"))
                }
            }
        }
    }

    private fun refreshNow() {
        val id = sessionId ?: return
        lifecycleScope.launch {
            runCatching { ConnectionSettings.api(this@SearchhhResultsWebViewActivity).status(id) }
                .onSuccess { status -> renderResults(status) }
                .onFailure { failure ->
                    if (currentStatus == null) showError(failure.message ?: "Search status unavailable")
                }
        }
    }

    private fun renderResults(status: JobStatus) {
        currentStatus = status
        showingResults = true
        statusText.text = "Searchhh results"
        webView.loadDataWithBaseURL(
            SearchhhResultsHtml.BASE_URL,
            SearchhhResultsHtml.render(status, savedIds),
            "text/html",
            "UTF-8",
            null,
        )
    }

    private fun showError(message: String) {
        showingResults = false
        statusText.text = "Searchhh WebView"
        webView.loadDataWithBaseURL(
            SearchhhResultsHtml.BASE_URL,
            SearchhhResultsHtml.error(message),
            "text/html",
            "UTF-8",
            null,
        )
    }

    private fun handleNavigation(uri: Uri): Boolean {
        if (SearchhhWebViewPolicy.isAction(uri)) {
            when (SearchhhWebViewPolicy.action(uri)) {
                "save", "unsave" -> toggleSaved(uri)
                "retry" -> if (sessionId != null) refreshNow() else lastExternalUrl?.let(::openExternal)
                else -> Toast.makeText(this, "Unsupported Searchhh action", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        if (isResultsOrigin(uri)) return false
        if (!SearchhhWebViewPolicy.isSupportedExternal(uri, allowHttp())) {
            Toast.makeText(this, "This URL is blocked by Searchhh transport policy", Toast.LENGTH_SHORT).show()
            return true
        }
        showingResults = false
        lastExternalUrl = uri.toString()
        return false
    }

    private fun toggleSaved(uri: Uri) {
        val id = SearchhhWebViewPolicy.actionId(uri) ?: return
        val result = currentStatus?.results?.firstOrNull { it.id == id } ?: return
        val isSaved = id in savedIds
        lifecycleScope.launch(Dispatchers.IO) {
            if (isSaved) dao.remove(id) else dao.save(SavedOpportunity(id, gson.toJson(result)))
            withContext(Dispatchers.Main) {
                if (showingResults) currentStatus?.let(::renderResults)
            }
        }
    }

    private fun openExternal(url: String) {
        val uri = Uri.parse(url)
        if (!SearchhhWebViewPolicy.isSupportedExternal(uri, allowHttp())) {
            showError("This URL is not allowed by Searchhh's HTTPS-first policy")
            return
        }
        showingResults = false
        lastExternalUrl = uri.toString()
        webView.loadUrl(uri.toString())
    }

    private fun allowHttp(): Boolean =
        android.preference.PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
            .getBoolean(BrowserConfig.K_ALLOW_HTTP, false)

    private fun isResultsOrigin(uri: Uri): Boolean = uri.scheme.equals("https", true) && uri.host.equals("searchhh.local", true)

    private fun isDarkTheme(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    companion object {
        private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
        private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
        private const val EXTRA_SESSION_ID = "searchhh_session_id"
        private const val EXTRA_URL = "searchhh_url"
        private const val EXTRA_SAVED = "searchhh_saved"

        fun openResults(
            context: Context,
            sessionId: String,
        ) {
            context.startActivity(
                Intent(context, SearchhhResultsWebViewActivity::class.java).putExtra(EXTRA_SESSION_ID, sessionId),
            )
        }

        fun openUrl(
            context: Context,
            url: String,
        ) {
            context.startActivity(
                Intent(context, SearchhhResultsWebViewActivity::class.java).putExtra(EXTRA_URL, url),
            )
        }

        fun openSaved(context: Context) {
            context.startActivity(
                Intent(context, SearchhhResultsWebViewActivity::class.java).putExtra(EXTRA_SAVED, true),
            )
        }
    }
}

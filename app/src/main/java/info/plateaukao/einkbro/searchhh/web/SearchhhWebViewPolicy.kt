package info.plateaukao.einkbro.searchhh.web

import android.net.Uri
import info.plateaukao.einkbro.searchhh.local.LocalPolicy

/** Allow-list for the app-owned result WebView and its private action scheme. */
object SearchhhWebViewPolicy {
    private const val ACTION_SCHEME = "searchhh"

    fun isAction(uri: Uri): Boolean = uri.scheme.equals(ACTION_SCHEME, ignoreCase = true)

    fun action(uri: Uri): String? =
        if (isAction(uri)) {
            uri.host?.lowercase()
        } else {
            null
        }

    fun actionId(uri: Uri): String? =
        if (isAction(uri)) {
            uri.pathSegments.firstOrNull()?.takeIf { it.matches(Regex("[a-f0-9]{64}")) }
        } else {
            null
        }

    fun isSupportedExternal(
        uri: Uri,
        allowHttp: Boolean,
    ): Boolean {
        val scheme = uri.scheme?.lowercase()
        if (scheme !in listOf("http", "https")) return false
        if (scheme == "http" && !allowHttp) return false
        return LocalPolicy.canonical(uri.toString()) != null
    }
}

package info.plateaukao.einkbro.browser

import android.app.Activity
import android.content.Context
import android.net.http.SslError
import android.security.KeyChain
import android.util.Log
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import info.plateaukao.einkbro.view.dialog.compose.AuthenticationDialogFragment

class WebViewSslHandler(
    private val context: Context,
    private val notifyTlsBlocked: (WebView, String) -> Unit = { view, message ->
        Toast.makeText(view.context, message, Toast.LENGTH_LONG).show()
    },
) {
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler?) {
        AuthenticationDialogFragment { username, password ->
            handler?.proceed(username, password)
        }.show((context as FragmentActivity).supportFragmentManager, "AuthenticationDialog")
    }

    // return true means it's processed
    private fun handlePrivateKeyAlias(
        request: ClientCertRequest,
        alias: String?,
    ): Boolean {
        val keyAlias = alias ?: return false
        val holder = context as? Activity ?: return false
        try {
            val certChain = KeyChain.getCertificateChain(holder, keyAlias) ?: return false
            val privateKey = KeyChain.getPrivateKey(holder, keyAlias) ?: return false
            request.proceed(privateKey, certChain)
            return true
        } catch (e: Exception) {
            Log.e(
                "ebWebViewClient",
                "Error when getting CertificateChain or PrivateKey for alias '$alias'",
                e,
            )
        }
        return false
    }

    fun onReceivedClientCertRequest(
        view: WebView,
        request: ClientCertRequest,
        fallback: () -> Unit,
    ) {
        val holder = view.context as? Activity ?: return
        KeyChain.choosePrivateKeyAlias(
            holder,
            { alias ->
                if (!handlePrivateKeyAlias(request, alias)) {
                    fallback()
                }
            },
            request.keyTypes,
            request.principals,
            request.host,
            request.port,
            null,
        )
    }

    fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError,
    ) {
        // Never offer a bypass: untrusted network content must not obtain native privileges.
        handler.cancel()
        val message = "Connection blocked: invalid TLS certificate (error ${error.primaryError})."
        Log.e(TAG, message)
        notifyTlsBlocked(view, message)
    }

    companion object {
        private const val TAG = "ebWebViewClient"
    }
}

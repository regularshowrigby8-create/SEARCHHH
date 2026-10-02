package info.plateaukao.einkbro.browser

import android.content.Context
import android.net.http.SslError
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebView
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewSslHandlerTest {
    @Test
    fun invalidCertificatesAreCancelledAndReportedWithoutBypass() {
        mockkStatic(Log::class)
        try {
            every { Log.e(any(), any<String>()) } returns 0
            for (reason in listOf(SslError.SSL_UNTRUSTED, SslError.SSL_EXPIRED, SslError.SSL_IDMISMATCH)) {
                val handler = mockk<SslErrorHandler>(relaxed = true)
                val error = mockk<SslError>()
                every { error.primaryError } returns reason
                val messages = mutableListOf<String>()
                val subject = WebViewSslHandler(mockk<Context>()) { _, message ->
                    verify(exactly = 1) { handler.cancel() }
                    messages.add(message)
                }
                subject.onReceivedSslError(mockk<WebView>(), handler, error)
                verify(exactly = 1) { handler.cancel() }
                verify(exactly = 0) { handler.proceed() }
                assertEquals(1, messages.size)
                assertTrue(messages.single().contains("Connection blocked"))
            }
        } finally {
            unmockkStatic(Log::class)
        }
    }
}

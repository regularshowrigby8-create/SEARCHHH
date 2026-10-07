package info.plateaukao.einkbro.searchhh

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.plateaukao.einkbro.searchhh.web.SearchhhWebViewPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchhhWebViewPolicyTest {
    private val resultId = "a".repeat(64)

    @Test
    fun privateActionsRequireKnownShapeAndResultHashes() {
        val save = Uri.parse("searchhh://save/$resultId")
        assertTrue(SearchhhWebViewPolicy.isAction(save))
        assertTrue(SearchhhWebViewPolicy.action(save) == "save")
        assertTrue(SearchhhWebViewPolicy.actionId(save) == resultId)
        assertNull(SearchhhWebViewPolicy.actionId(Uri.parse("searchhh://save/not-a-result")))
        assertFalse(SearchhhWebViewPolicy.action(Uri.parse("searchhh://unknown/$resultId")) == "save")
    }

    @Test
    fun externalNavigationIsHttpsFirstAndPublicOnly() {
        assertTrue(SearchhhWebViewPolicy.isSupportedExternal(Uri.parse("https://example.org/path"), allowHttp = false))
        assertFalse(SearchhhWebViewPolicy.isSupportedExternal(Uri.parse("http://example.org/path"), allowHttp = false))
        assertTrue(SearchhhWebViewPolicy.isSupportedExternal(Uri.parse("http://example.org/path"), allowHttp = true))
        assertFalse(SearchhhWebViewPolicy.isSupportedExternal(Uri.parse("https://127.0.0.1/private"), allowHttp = true))
        assertFalse(SearchhhWebViewPolicy.isSupportedExternal(Uri.parse("file:///sdcard/private"), allowHttp = true))
    }
}

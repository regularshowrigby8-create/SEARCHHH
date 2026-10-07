package info.plateaukao.einkbro.searchhh

import info.plateaukao.einkbro.searchhh.testing.SearchhhTestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SearchhhTestTagsTest {
    @Test
    fun repeatedResultControlsKeepEntityIdentity() {
        val first = "result-1"
        val second = "result-2"

        assertEquals("result_open:$first", SearchhhTestTags.resultOpen(first))
        assertEquals("result_save:$first", SearchhhTestTags.resultSave(first))
        assertEquals("result_details:$first", SearchhhTestTags.resultDetails(first))
        assertNotEquals(SearchhhTestTags.resultOpen(first), SearchhhTestTags.resultOpen(second))
        assertNotEquals(SearchhhTestTags.resultSave(first), SearchhhTestTags.resultDetails(first))
    }

    @Test
    fun savedResultsUseTheirOwnWebViewControl() {
        assertEquals("saved_webview", SearchhhTestTags.SAVED_WEBVIEW)
        assertNotEquals(SearchhhTestTags.RESULTS_WEBVIEW, SearchhhTestTags.SAVED_WEBVIEW)
    }

    @Test
    fun sourceFilterTagsDoNotCollapseDifferentSources() {
        assertEquals("source_filter:portal-a", SearchhhTestTags.sourceFilter("portal-a"))
        assertNotEquals(SearchhhTestTags.sourceFilter("portal-a"), SearchhhTestTags.sourceFilter("portal-b"))
    }
}

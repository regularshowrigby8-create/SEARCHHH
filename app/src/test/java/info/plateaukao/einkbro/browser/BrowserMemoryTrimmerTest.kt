package info.plateaukao.einkbro.browser

import android.content.ComponentCallbacks2
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserMemoryTrimmerTest {
    @Test
    fun foregroundAndUiHiddenDoNotPauseTabsOrDiscardPreloadedView() {
        val current = mockk<AlbumController>(relaxed = true)
        val background = mockk<AlbumController>(relaxed = true)
        var releases = 0
        for (level in listOf(0, ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN, ComponentCallbacks2.TRIM_MEMORY_BACKGROUND - 1)) {
            BrowserMemoryTrimmer.trim(level, listOf(current, background), current) { releases++ }
        }
        verify(exactly = 0) { current.pauseWebView() }
        verify(exactly = 0) { background.pauseWebView() }
        assertEquals(0, releases)
    }

    @Test
    fun backgroundAndHigherPauseOnlyInactiveTabsAndReleasePreload() {
        val current = mockk<AlbumController>(relaxed = true)
        val background = mockk<AlbumController>(relaxed = true)
        var releases = 0
        for (level in listOf(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND, ComponentCallbacks2.TRIM_MEMORY_BACKGROUND + 1)) {
            BrowserMemoryTrimmer.trim(level, listOf(current, background), current) { releases++ }
        }
        verify(exactly = 0) { current.pauseWebView() }
        verify(exactly = 2) { background.pauseWebView() }
        assertEquals(2, releases)
    }

    @Test
    fun absentActiveTabPausesAllRemainingTabs() {
        val background = mockk<AlbumController>(relaxed = true)
        var releases = 0
        BrowserMemoryTrimmer.trim(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND, listOf(background), null) { releases++ }
        verify(exactly = 1) { background.pauseWebView() }
        assertEquals(1, releases)
    }
}

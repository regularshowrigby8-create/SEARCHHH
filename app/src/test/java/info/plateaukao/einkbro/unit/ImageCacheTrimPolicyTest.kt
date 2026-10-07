package info.plateaukao.einkbro.unit

import android.content.ComponentCallbacks2
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageCacheTrimPolicyTest {
    @Test
    fun legacyLevelsKeepTheirHistoricalCacheActions() {
        // Protocol fixtures: RUNNING_MODERATE=5, LOW=10, CRITICAL=15 on older Android.
        assertEquals(ImageCacheTrimAction.KEEP, ImageCacheTrimPolicy.action(0))
        assertEquals(ImageCacheTrimAction.KEEP, ImageCacheTrimPolicy.action(5))
        assertEquals(ImageCacheTrimAction.QUARTER, ImageCacheTrimPolicy.action(10))
        assertEquals(ImageCacheTrimAction.EVICT, ImageCacheTrimPolicy.action(15))
    }

    @Test
    fun modernUiHiddenTrimsAndBackgroundOrHigherEvicts() {
        assertEquals(ImageCacheTrimAction.QUARTER, ImageCacheTrimPolicy.action(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN))
        assertEquals(ImageCacheTrimAction.EVICT, ImageCacheTrimPolicy.action(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND))
        assertEquals(ImageCacheTrimAction.EVICT, ImageCacheTrimPolicy.action(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND + 1))
    }
}

package info.plateaukao.einkbro.unit

import android.content.ComponentCallbacks2

internal enum class ImageCacheTrimAction { KEEP, QUARTER, EVICT }

internal object ImageCacheTrimPolicy {
    // API 24–33 still deliver RUNNING_* levels. Keep their behavior, not magic replacements.
    // Scope/removal criteria/tests: docs/quality/COMPATIBILITY_EXCEPTIONS.md.
    @Suppress("DEPRECATION")
    fun action(level: Int): ImageCacheTrimAction = when {
        level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND ||
            level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> ImageCacheTrimAction.EVICT
        level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> ImageCacheTrimAction.QUARTER
        else -> ImageCacheTrimAction.KEEP
    }
}

package info.plateaukao.einkbro.browser

import android.content.ComponentCallbacks2

internal object BrowserMemoryTrimmer {
    /** BACKGROUND is still delivered on modern Android; MODERATE stopped at API 34. */
    fun trim(
        level: Int,
        albums: List<AlbumController>,
        currentAlbum: AlbumController?,
        releasePreloaded: () -> Unit,
    ) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            albums.filter { it != currentAlbum }.forEach { it.pauseWebView() }
            releasePreloaded()
        }
    }
}

package info.plateaukao.einkbro.unit

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ImageCacheBehaviorTest {
    @Test
    fun trimEvictsRealMemoryWithoutDeletingTheDiskCopy() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(base.cacheDir, "quality-image-${UUID.randomUUID()}")
        val context =
            object : ContextWrapper(base) {
                override fun getCacheDir(): File = directory
            }
        try {
            val cache = EinkImageCache(context)
            val bytes = ByteArray(MEBIBYTE) { 7 }
            repeat(6) { cache.put("https://example.org/image-$it", 1, bytes) }
            assertEquals(6 * MEBIBYTE, cache.memoryUsageBytes())
            cache.trimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
            assertEquals(4 * MEBIBYTE, cache.memoryUsageBytes())
            cache.trimMemory(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND)
            assertEquals(0, cache.memoryUsageBytes())
            val restored = requireNotNull(cache.get("https://example.org/image-0", 1)).use { it.readBytes() }
            assertArrayEquals(bytes, restored)
            assertEquals(MEBIBYTE, cache.memoryUsageBytes())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun failedDiskWriteLeavesTheMemoryEntryUsable() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val file = File.createTempFile("quality-cache-", ".file", base.cacheDir)
        val context =
            object : ContextWrapper(base) {
                override fun getCacheDir(): File = file
            }
        try {
            val cache = EinkImageCache(context)
            val bytes = byteArrayOf(1, 2, 3)
            cache.put("https://example.org/image", 1, bytes)
            assertEquals(bytes.size, cache.memoryUsageBytes())
            val restored = requireNotNull(cache.get("https://example.org/image", 1)).use { it.readBytes() }
            assertArrayEquals(bytes, restored)
        } finally {
            file.delete()
        }
    }

    companion object {
        private const val MEBIBYTE = 1024 * 1024
    }
}

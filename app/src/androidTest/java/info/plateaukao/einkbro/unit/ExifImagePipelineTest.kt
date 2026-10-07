package info.plateaukao.einkbro.unit

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ExifImagePipelineTest {
    private lateinit var directory: File
    private lateinit var context: Context
    private lateinit var evidence: File

    @Before
    fun isolateBackgroundStorage() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        directory = File(base.cacheDir, "quality-exif-${UUID.randomUUID()}")
        check(directory.mkdirs())
        context =
            object : ContextWrapper(base) {
                override fun getFilesDir(): File = directory
            }
        evidence = File(base.filesDir, "exif-evidence")
    }

    @After
    fun removeFixtures() {
        check(directory.deleteRecursively())
    }

    @Test
    fun jpegOrientationsPreserveBothProductionPipelines() {
        if (evidence.exists()) check(evidence.deleteRecursively())
        check(evidence.mkdirs())
        var comparisons = 0
        for (orientation in ExifImageFixtures.NORMAL..ExifImageFixtures.ROTATE_270) {
            val input = ExifImageFixtures.jpeg(directory, orientation)
            assertEquals(
                orientation,
                ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0),
            )
            verifyJpegBackground(input, orientation)
            verifyJpegProcessing(input, orientation)
            comparisons += PIPELINES
        }
        assertEquals(JPEG_COMPARISONS, comparisons)
        for (name in EVIDENCE_IMAGES) {
            assertTrue("Missing $name", File(evidence, name).length() > 0)
        }
        File(evidence, "complete.txt").writeText(
            "reference=pre-migration-orientation-semantics\njpeg_pixel_comparisons=$comparisons\n" +
                "scope=image-pipeline-not-full-screen\n",
        )
    }

    @Test
    fun jpegWithoutExifKeepsNormalOrientation() {
        val input = ExifImageFixtures.jpeg(directory, null)
        verifyJpegBackground(input, ExifImageFixtures.NORMAL)
        verifyJpegProcessing(input, ExifImageFixtures.NORMAL)
    }

    @Test
    fun pngRemainsLosslessAndTransparentInBothPipelines() {
        val original = ExifImageFixtures.pattern(true)
        try {
            val input = File(directory, "transparent.png")
            input.writeBytes(ExifImageFixtures.encode(original, Bitmap.CompressFormat.PNG, PNG_QUALITY))
            assertTrue(BookmarkRenderer.saveStartPageBackground(context, Uri.fromFile(input)))
            assertPixels(original, BookmarkRenderer.startPageBackgroundFile(context).readBytes(), "png-background")
            val decoded = ExifImageFixtures.decode(input.readBytes())
            try {
                val expected = EinkImageProcessor.process(decoded, ExifImageFixtures.STRENGTH)
                val actual =
                    requireNotNull(
                        EinkImageProcessor.processBytes(input.readBytes(), "image/png", ExifImageFixtures.STRENGTH),
                    )
                assertPixels(expected, actual, "png-web")
                assertEquals(Color.TRANSPARENT, expected.getPixel(0, 0))
                assertMime("image/png", actual)
            } finally {
                decoded.recycle()
            }
            assertMime("image/png", BookmarkRenderer.startPageBackgroundFile(context).readBytes())
        } finally {
            original.recycle()
        }
    }

    @Test
    fun rejectedInputsDoNotReplaceBackgroundAndKeepWebPassThrough() {
        val existing = ExifImageFixtures.jpeg(directory, null).readBytes()
        val output = BookmarkRenderer.startPageBackgroundFile(context)
        output.writeBytes(existing)
        val corrupt = File(directory, "corrupt.jpg").apply { writeText("not an image") }
        assertFalse(BookmarkRenderer.saveStartPageBackground(context, Uri.fromFile(corrupt)))
        assertArrayEquals(existing, output.readBytes())
        assertFalse(BookmarkRenderer.saveStartPageBackground(context, Uri.fromFile(File(directory, "missing.jpg"))))
        assertArrayEquals(existing, output.readBytes())
        assertNull(EinkImageProcessor.processBytes(corrupt.readBytes(), "image/jpeg", ExifImageFixtures.STRENGTH))
        val valid = ExifImageFixtures.jpeg(directory, ExifImageFixtures.ROTATE_90).readBytes()
        assertNull(EinkImageProcessor.processBytes(valid, "image/jpeg", 0))
        val tiny = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        try {
            val bytes = ExifImageFixtures.encode(tiny, Bitmap.CompressFormat.PNG, PNG_QUALITY)
            assertNull(EinkImageProcessor.processBytes(bytes, "image/png", ExifImageFixtures.STRENGTH))
        } finally {
            tiny.recycle()
        }
    }

    private fun verifyJpegBackground(
        input: File,
        orientation: Int,
    ) {
        assertTrue(BookmarkRenderer.saveStartPageBackground(context, Uri.fromFile(input)))
        val actual = BookmarkRenderer.startPageBackgroundFile(context).readBytes()
        val source = ExifImageFixtures.decode(input.readBytes())
        val expected = ExifImageFixtures.orient(source, ExifImageFixtures.backgroundOrientation(orientation))
        try {
            compareEncoded(expected, actual, ExifImageFixtures.BACKGROUND_QUALITY, "background", orientation)
        } finally {
            source.recycle()
            expected.recycle()
        }
    }

    private fun verifyJpegProcessing(
        input: File,
        orientation: Int,
    ) {
        val bytes = input.readBytes()
        val source = ExifImageFixtures.decode(bytes)
        val processed = EinkImageProcessor.process(source, ExifImageFixtures.STRENGTH)
        val expected = ExifImageFixtures.orient(processed, orientation)
        try {
            val actual =
                requireNotNull(EinkImageProcessor.processBytes(bytes, "image/jpeg", ExifImageFixtures.STRENGTH))
            compareEncoded(expected, actual, ExifImageFixtures.WEB_QUALITY, "web", orientation)
        } finally {
            source.recycle()
            expected.recycle()
        }
    }

    private fun compareEncoded(
        expectedPixels: Bitmap,
        actualBytes: ByteArray,
        quality: Int,
        pipeline: String,
        orientation: Int,
    ) {
        val expectedBytes = ExifImageFixtures.encode(expectedPixels, Bitmap.CompressFormat.JPEG, quality)
        val expected = ExifImageFixtures.decode(expectedBytes)
        val actual = ExifImageFixtures.decode(actualBytes)
        try {
            assertTrue("$pipeline orientation=$orientation", expected.sameAs(actual))
            assertMime("image/jpeg", actualBytes)
            actualBytes.inputStream().use {
                assertEquals(
                    ExifInterface.ORIENTATION_UNDEFINED,
                    ExifInterface(it).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED,
                    ),
                )
            }
            val capture =
                when (pipeline) {
                    "background" -> orientation == ExifImageFixtures.ROTATE_90
                    "web" -> orientation == ExifImageFixtures.TRANSVERSE
                    else -> error("Unsupported evidence pipeline: $pipeline")
                }
            if (capture) {
                File(evidence, "reference-$pipeline.png").writeBytes(
                    ExifImageFixtures.encode(expected, Bitmap.CompressFormat.PNG, PNG_QUALITY),
                )
                File(evidence, "current-$pipeline.png").writeBytes(
                    ExifImageFixtures.encode(actual, Bitmap.CompressFormat.PNG, PNG_QUALITY),
                )
            }
        } finally {
            expected.recycle()
            actual.recycle()
        }
    }

    private fun assertPixels(
        expected: Bitmap,
        actualBytes: ByteArray,
        label: String,
    ) {
        val actual = ExifImageFixtures.decode(actualBytes)
        try {
            assertTrue(label, expected.sameAs(actual))
        } finally {
            actual.recycle()
        }
    }

    private fun assertMime(
        mime: String,
        bytes: ByteArray,
    ) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        assertEquals(mime, bounds.outMimeType)
    }

    private companion object {
        const val PIPELINES = 2
        const val JPEG_COMPARISONS = 16
        const val PNG_QUALITY = 100
        val EVIDENCE_IMAGES =
            listOf("reference-background.png", "current-background.png", "reference-web.png", "current-web.png")
    }
}

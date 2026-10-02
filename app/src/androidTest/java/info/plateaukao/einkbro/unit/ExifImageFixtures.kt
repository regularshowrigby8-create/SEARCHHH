package info.plateaukao.einkbro.unit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

/** Test-only TIFF orientation reference; pixel coordinates, not the production Matrix code. */
internal object ExifImageFixtures {
    const val NORMAL = 1
    const val FLIP_HORIZONTAL = 2
    const val ROTATE_180 = 3
    const val FLIP_VERTICAL = 4
    const val TRANSPOSE = 5
    const val ROTATE_90 = 6
    const val TRANSVERSE = 7
    const val ROTATE_270 = 8
    const val BACKGROUND_QUALITY = 80
    const val WEB_QUALITY = 85
    const val STRENGTH = 50
    private const val WIDTH = 160
    private const val HEIGHT = 128
    private const val HALF = 2
    private const val FULL_QUALITY = 100

    fun jpeg(
        directory: File,
        orientation: Int?,
    ): File {
        val file = File(directory, "fixture-${orientation ?: "untagged"}.jpg")
        val bitmap = pattern(false)
        try {
            file.writeBytes(encode(bitmap, Bitmap.CompressFormat.JPEG, FULL_QUALITY))
        } finally {
            bitmap.recycle()
        }
        if (orientation != null) {
            val exif = ExifInterface(file)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            exif.saveAttributes()
        }
        return file
    }

    fun pattern(transparent: Boolean): Bitmap {
        val pixels =
            IntArray(WIDTH * HEIGHT) { index ->
                val x = index % WIDTH
                val y = index / WIDTH
                when {
                    transparent && x == 0 && y == 0 -> Color.TRANSPARENT
                    x < WIDTH / HALF && y < HEIGHT / HALF -> Color.RED
                    y < HEIGHT / HALF -> Color.GREEN
                    x < WIDTH / HALF -> Color.BLUE
                    else -> Color.YELLOW
                }
            }
        return Bitmap.createBitmap(pixels, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
    }

    fun decode(bytes: ByteArray): Bitmap {
        val options = BitmapFactory.Options().apply { inMutable = true }
        return requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
    }

    fun encode(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int,
    ): ByteArray =
        ByteArrayOutputStream().use { output ->
            check(bitmap.compress(format, quality, output))
            output.toByteArray()
        }

    fun orient(
        source: Bitmap,
        orientation: Int,
    ): Bitmap {
        val width = source.width
        val height = source.height
        val swapped = orientation in TRANSPOSE..ROTATE_270
        val outWidth = if (swapped) height else width
        val outHeight = if (swapped) width else height
        val pixels = IntArray(outWidth * outHeight)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val (destX, destY) =
                    when (orientation) {
                        NORMAL -> x to y
                        FLIP_HORIZONTAL -> width - 1 - x to y
                        ROTATE_180 -> width - 1 - x to height - 1 - y
                        FLIP_VERTICAL -> x to height - 1 - y
                        TRANSPOSE -> y to x
                        ROTATE_90 -> height - 1 - y to x
                        TRANSVERSE -> height - 1 - y to width - 1 - x
                        ROTATE_270 -> y to width - 1 - x
                        else -> error("Unsupported fixture orientation: $orientation")
                    }
                pixels[destY * outWidth + destX] = source.getPixel(x, y)
            }
        }
        return Bitmap.createBitmap(pixels, outWidth, outHeight, Bitmap.Config.ARGB_8888)
    }

    /** Preserve the pre-migration background's rotation-only handling, including mirror omission. */
    fun backgroundOrientation(tag: Int): Int =
        when (tag) {
            ROTATE_90, ROTATE_180, ROTATE_270 -> tag
            else -> NORMAL
        }
}

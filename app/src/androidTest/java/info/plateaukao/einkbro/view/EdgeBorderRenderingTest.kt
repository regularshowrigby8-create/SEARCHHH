package info.plateaukao.einkbro.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.DisplayMetrics
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import info.plateaukao.einkbro.preference.UiBorder
import info.plateaukao.einkbro.view.compose.UiThemeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.FutureTask

@RunWith(AndroidJUnit4::class)
class EdgeBorderRenderingTest {
    @Test
    fun renderedPixelsMatchPreFixAcrossStylesDensitySizeAndMirroring() {
        val render = FutureTask { compareMatrix() }
        InstrumentationRegistry.getInstrumentation().runOnMainSync(render)
        render.get() // Propagate assertion and I/O failures from the UI thread to JUnit.
    }

    private fun compareMatrix() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val evidence = File(base.filesDir, "edge-border-evidence")
        if (evidence.exists()) check(evidence.deleteRecursively())
        check(evidence.mkdirs())
        val oldBorder = UiThemeState.uiBorder.value
        val oldInverted = UiThemeState.inverted.value
        try {
            var cases = 0
            for (dpi in DENSITIES) {
                val config = Configuration(base.resources.configuration).apply { densityDpi = dpi }
                val view = ThemedEdgeBorderView(base.createConfigurationContext(config))
                assertEquals(dpi, view.resources.configuration.densityDpi)
                assertEquals(
                    dpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT,
                    view.resources.displayMetrics.density,
                    0f,
                )
                for (width in WIDTHS) {
                    view.layout(0, 0, width, (BAND_HEIGHT_DP * view.resources.displayMetrics.density).toInt())
                    cases += compareStyles(view, evidence)
                }
            }
            assertEquals(EXPECTED_CASES, cases)
            for (name in EVIDENCE_IMAGES) {
                assertTrue("Missing image: $name", File(evidence, name).length() > 0)
            }
            File(evidence, "complete.txt").writeText(
                "reference_commit=5896283\npixel_cases=$cases\nscope=software-rendered-toolbar-border\n",
            )
        } finally {
            UiThemeState.uiBorder.value = oldBorder
            UiThemeState.inverted.value = oldInverted
        }
    }

    @Test
    fun dashEffectIsReusedAndRecreatedOnlyWhenDensityChanges() {
        val cache = EdgeDashEffectCache()
        val initial = cache.atDensity(1f)
        repeat(REPEATED_FRAMES) { assertSame(initial, cache.atDensity(1f)) }
        val scaled = cache.atDensity(HIGH_DENSITY)
        assertNotSame(initial, scaled)
        repeat(REPEATED_FRAMES) { assertSame(scaled, cache.atDensity(HIGH_DENSITY)) }
        val restored = cache.atDensity(1f)
        assertNotSame(scaled, restored)
        assertSame(restored, cache.atDensity(1f))
    }

    private fun compareStyles(
        view: ThemedEdgeBorderView,
        evidence: File,
    ): Int {
        var cases = 0
        for (border in TEST_BORDERS) {
            UiThemeState.uiBorder.value = border
            for (atTop in listOf(true, false)) {
                view.edgeAtTop = atTop
                for (inverted in listOf(false, true)) {
                    UiThemeState.inverted.value = inverted
                    comparePixels(view, border, inverted, evidence)
                    cases++
                }
            }
        }
        return cases
    }

    private fun comparePixels(
        view: ThemedEdgeBorderView,
        border: UiBorder,
        inverted: Boolean,
        evidence: File,
    ) {
        val before = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val after = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        try {
            LegacyEdgeBorderRenderer(view.context, view.width, view.height, view.edgeAtTop).render(Canvas(before))
            view.draw(Canvas(after))
            assertTrue(
                "Pixel mismatch: $border width=${view.width} density=${view.resources.displayMetrics.density}" +
                    " top=${view.edgeAtTop} inverted=$inverted",
                before.sameAs(after),
            )
            if (
                view.width == EVIDENCE_WIDTH &&
                view.resources.displayMetrics.density == 1f &&
                view.edgeAtTop &&
                !inverted &&
                (border == UiBorder.STAMP || border == UiBorder.DASHED)
            ) {
                savePng(before, File(evidence, "reference-${border.name.lowercase()}.png"))
                savePng(after, File(evidence, "current-${border.name.lowercase()}.png"))
            }
        } finally {
            before.recycle()
            after.recycle()
        }
    }

    private fun savePng(
        bitmap: Bitmap,
        file: File,
    ) {
        file.outputStream().use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream))
        }
    }

    private companion object {
        val DENSITIES =
            intArrayOf(
                DisplayMetrics.DENSITY_MEDIUM,
                DisplayMetrics.DENSITY_HIGH,
                DisplayMetrics.DENSITY_XHIGH,
            )
        val EVIDENCE_IMAGES =
            listOf("reference-stamp.png", "current-stamp.png", "reference-dashed.png", "current-dashed.png")
        val TEST_BORDERS = listOf(UiBorder.DASHED, UiBorder.CLASSIC, UiBorder.STAMP)
        const val NARROW_WIDTH = 17
        const val MEDIUM_WIDTH = 64
        const val HIGH_DENSITY = 2f
        const val BAND_HEIGHT_DP = 5
        const val EXPECTED_CASES = 144
        const val EVIDENCE_WIDTH = 257
        const val REPEATED_FRAMES = 16
        const val PNG_QUALITY = 100
        val WIDTHS = intArrayOf(1, NARROW_WIDTH, MEDIUM_WIDTH, EVIDENCE_WIDTH)
    }
}

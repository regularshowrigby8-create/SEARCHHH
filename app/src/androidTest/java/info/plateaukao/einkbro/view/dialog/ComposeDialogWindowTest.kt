package info.plateaukao.einkbro.view.dialog

import android.graphics.Color
import android.os.Build
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.plateaukao.einkbro.activity.SettingActivity
import info.plateaukao.einkbro.searchhh.testing.SearchhhTestTags
import info.plateaukao.einkbro.view.dialog.compose.ThemeColorDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeDialogWindowTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun panelUsesPublicThemeAndOkDismissesTheActualDialog() {
        ActivityScenario.launch(SettingActivity::class.java).use { scenario ->
            val fragment = ThemeColorDialogFragment()
            scenario.onActivity { activity ->
                fragment.showNow(activity.supportFragmentManager, "quality-panel")
                val dialog = requireNotNull(fragment.dialog)
                assertTrue(dialog.isShowing)
                assertEquals(0, requireNotNull(dialog.window).attributes.windowAnimations)
                val colors = dialog.context.obtainStyledAttributes(intArrayOf(android.R.attr.colorControlNormal))
                try {
                    assertEquals(Color.BLACK, colors.getColor(0, Color.TRANSPARENT))
                } finally {
                    colors.recycle()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val movement = dialog.context.obtainStyledAttributes(intArrayOf(android.R.attr.windowNoMoveAnimation))
                    try {
                        assertTrue(movement.getBoolean(0, false))
                    } finally {
                        movement.recycle()
                    }
                }
            }
            compose.onNodeWithTag(SearchhhTestTags.THEME_DIALOG_CLOSE).performScrollTo().performClick()
            compose.waitForIdle()
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()
                assertFalse(fragment.isAdded)
                assertNull(activity.supportFragmentManager.findFragmentByTag("quality-panel"))
            }
        }
    }
}

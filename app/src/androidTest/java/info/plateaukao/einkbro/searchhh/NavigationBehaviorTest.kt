package info.plateaukao.einkbro.searchhh

import android.content.Context
import android.view.KeyEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import info.plateaukao.einkbro.searchhh.local.LocalIdentity
import info.plateaukao.einkbro.searchhh.testing.SearchhhTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationBehaviorTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private fun prepare() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalIdentity.create(context, "Navigation behavior test")
        ConnectionSettings
            .prefs(context)
            .edit()
            .remove("job")
            .remove("last_status")
            .putBoolean("relay_enabled", false)
            .putBoolean("server_enabled", false)
            .commit()
    }

    @Test
    fun everyRegisteredDestinationChangesSelectionAndBackReturnsHome() {
        prepare()
        ActivityScenario.launch(SearchhhActivity::class.java).use {
            for (destination in SearchhhRoute.entries.filter { it != SearchhhRoute.DISCOVER }) {
                compose.onNodeWithTag(destination.menuTag).performClick()
                compose.onNodeWithTag(destination.menuTag).assertIsSelected()
                compose.onNodeWithTag(destination.screenTag).assertIsDisplayed()
                InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                compose.onNodeWithTag(SearchhhTestTags.MENU_HOME).assertIsSelected()
                compose.onNodeWithTag(SearchhhTestTags.HOME).assertIsDisplayed()
            }
        }
    }

    @Test
    fun enteredQuerySurvivesNavigationAndActivityRecreation() {
        prepare()
        ActivityScenario.launch(SearchhhActivity::class.java).use { activity ->
            compose
                .onNodeWithTag(SearchhhTestTags.SEARCH_FIELD)
                .performScrollTo()
                .performTextInput("global fellowship")
            compose.onNodeWithTag(SearchhhTestTags.MENU_SETTINGS).performClick()
            compose.onNodeWithTag(SearchhhTestTags.SETTINGS).assertIsDisplayed()
            activity.recreate()
            compose.onNodeWithTag(SearchhhTestTags.MENU_SETTINGS).assertIsSelected()
            compose.onNodeWithTag(SearchhhTestTags.MENU_HOME).performClick()
            compose
                .onNodeWithTag(SearchhhTestTags.SEARCH_FIELD)
                .performScrollTo()
                .assertTextContains("global fellowship")
        }
    }
}

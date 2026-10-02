package info.plateaukao.einkbro.searchhh

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.plateaukao.einkbro.searchhh.local.LocalIdentity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CodebaseUiTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun allCodebasesCanBeBrowsedAndFilteredSeparatelyFromPortals() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalIdentity.create(context, "Codebase catalogue test")
        ConnectionSettings.prefs(context).edit().putBoolean("relay_enabled", false)
            .putBoolean("server_enabled", false).apply()
        ActivityScenario.launch(SearchhhActivity::class.java).use {
            compose.onNodeWithText("Sources").performClick()
            compose.onNodeWithText("Portals (110)").assertExists()
            compose.onNodeWithText("Codebases (120)").performClick()
            compose.onNode(hasSetTextAction()).performScrollTo().performTextInput("92")
            compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("#92 Uscraper"))
            compose.onNodeWithText("#92 Uscraper").assertExists()
            compose.onNodeWithText("Integration: catalog only").assertExists()
            compose.onNodeWithText("Open source code ↗").assertExists()
        }
    }
}

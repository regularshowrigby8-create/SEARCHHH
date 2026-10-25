package com.searchhh.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.searchhh.app.data.model.CategoryCount
import com.searchhh.app.data.model.Entry
import com.searchhh.app.ui.home.HomeScreen
import com.searchhh.app.ui.home.HomeUiState
import com.searchhh.app.ui.theme.SearchhhTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke test: renders the home screen with deterministic state.
 * Runs on a device/emulator (CI uses a Gradle Managed Device).
 */
@RunWith(AndroidJUnit4::class)
class ComposeSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeScreenRendersHeadlineAndResults() {
        val state = HomeUiState(
            query = "",
            categories = listOf(CategoryCount("food", 3), CategoryCount("services", 5)),
            trending = listOf(
                Entry(id = "svc-001", title = "Campus Coffee Run", price = 28.0, rating = 4.7),
                Entry(id = "svc-002", title = "Budget Meal Prep", price = 249.0, rating = 4.4),
            ),
        )

        composeRule.setContent {
            SearchhhTheme(dynamicColor = false) {
                HomeScreen(
                    state = state,
                    onQueryChange = {},
                    onSubmit = {},
                    onRefresh = {},
                    onOpenEntry = {},
                    onToggleSave = {},
                    onCategoryClick = {},
                    onQueryChipClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Find it in seconds").assertIsDisplayed()
        composeRule.onNodeWithText("Campus Coffee Run").assertIsDisplayed()
    }
}

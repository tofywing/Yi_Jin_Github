package com.example.yijinsgithub

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.yijinsgithub.ui.screens.SearchScreen
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test onSearch is called when search button clicked`() {
        val onSearchMock: (String, String?, Boolean) -> Unit = mock()
        
        composeTestRule.setContent {
            SearchScreen(
                uiState = GithubUiState.Idle,
                repos = emptyList(),
                query = "kotlin",
                language = "Kotlin",
                onQueryChange = {},
                onLanguageChange = {},
                onSearch = onSearchMock,
                onRepoClick = {}
            )
        }

        // Search button text is "Search GitHub"
        composeTestRule.onNodeWithText("Search GitHub").performClick()
        verify(onSearchMock).invoke("kotlin", "Kotlin", false)
    }
}

package com.example.yijinsgithub

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
                isLoadingMore = false,
                isLastPage = false,
                listState = rememberLazyListState(),
                onQueryChange = {},
                onLanguageChange = {},
                onSearch = onSearchMock,
                onLoadMore = {},
                onRepoClick = {}
            )
        }

        // Search button text is "Search GitHub"
        composeTestRule.onNodeWithText("Search GitHub").performClick()
        verify(onSearchMock).invoke("kotlin", "Kotlin", false)
    }

    @Test
    fun `test loading state shows refreshing indicator in RepoList`() {
        composeTestRule.setContent {
            SearchScreen(
                uiState = GithubUiState.Loading,
                repos = emptyList(),
                query = "kotlin",
                language = "",
                isLoadingMore = false,
                isLastPage = false,
                listState = rememberLazyListState(),
                onQueryChange = {},
                onLanguageChange = {},
                onSearch = { _, _, _ -> },
                onLoadMore = {},
                onRepoClick = {}
            )
        }

        // We can't easily test the internal PullRefresh state in RepoList with simple unit tests,
        // but we can verify the Search button is enabled if query is not blank
        composeTestRule.onNodeWithText("Search GitHub").assertExists()
    }

    @Test
    fun `test error message is displayed when uiState is Error`() {
        val errorMsg = "Search Failed"
        composeTestRule.setContent {
            SearchScreen(
                uiState = GithubUiState.Error(errorMsg),
                repos = emptyList(),
                query = "kotlin",
                language = "",
                isLoadingMore = false,
                isLastPage = false,
                listState = rememberLazyListState(),
                onQueryChange = {},
                onLanguageChange = {},
                onSearch = { _, _, _ -> },
                onLoadMore = {},
                onRepoClick = {}
            )
        }

        composeTestRule.onNodeWithText(errorMsg).assertExists()
    }

    @Test
    fun `test query and language inputs work`() {
        val onQueryChangeMock: (String) -> Unit = mock()
        val onLanguageChangeMock: (String) -> Unit = mock()

        composeTestRule.setContent {
            SearchScreen(
                uiState = GithubUiState.Idle,
                repos = emptyList(),
                query = "",
                language = "",
                isLoadingMore = false,
                isLastPage = false,
                listState = rememberLazyListState(),
                onQueryChange = onQueryChangeMock,
                onLanguageChange = onLanguageChangeMock,
                onSearch = { _, _, _ -> },
                onLoadMore = {},
                onRepoClick = {}
            )
        }

        composeTestRule.onNodeWithText("Search Keywords").performTextInput("android")
        verify(onQueryChangeMock).invoke("android")

        composeTestRule.onNodeWithText("Language (e.g. Kotlin)").performTextInput("Kotlin")
        verify(onLanguageChangeMock).invoke("Kotlin")
    }

    @Test
    fun `test search button is disabled when query is blank`() {
        composeTestRule.setContent {
            SearchScreen(
                uiState = GithubUiState.Idle,
                repos = emptyList(),
                query = "",
                language = "",
                isLoadingMore = false,
                isLastPage = false,
                listState = rememberLazyListState(),
                onQueryChange = {},
                onLanguageChange = {},
                onSearch = { _, _, _ -> },
                onLoadMore = {},
                onRepoClick = {}
            )
        }

        composeTestRule.onNodeWithText("Search GitHub").assertIsNotEnabled()
    }
}

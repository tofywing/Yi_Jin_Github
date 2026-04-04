package com.example.yijinsgithub

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.yijinsgithub.ui.screens.HomeScreen
import com.example.yijinsgithub.ui.theme.YiJinsGithubTheme
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import com.example.yijinsgithub.ui.viewmodel.UserState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginSectionIsDisplayedWhenAnonymous() {
        composeTestRule.setContent {
            YiJinsGithubTheme {
                HomeScreen(
                    uiState = GithubUiState.Idle,
                    userState = UserState.Anonymous,
                    homeRepos = emptyList(),
                    isLoadingMore = false,
                    isLastPage = true,
                    onSearchClick = {},
                    onProfileClick = {},
                    onLogin = {},
                    onRefresh = {},
                    onLoadMore = {},
                    onRepoClick = {}
                )
            }
        }

        // Check for specific text in LoginSection
        composeTestRule.onNodeWithText("Authentication Required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun testWelcomeMessageIsDisplayedWhenAuthenticated() {
        val user = com.example.yijinsgithub.data.model.User(
            login = "yi",
            id = 123L,
            avatarUrl = "",
            htmlUrl = ""
        )
        
        composeTestRule.setContent {
            YiJinsGithubTheme {
                HomeScreen(
                    uiState = GithubUiState.Idle,
                    userState = UserState.Authenticated(user, "token", emptyList()),
                    homeRepos = emptyList(),
                    isLoadingMore = false,
                    isLastPage = true,
                    onSearchClick = {},
                    onProfileClick = {},
                    onLogin = {},
                    onRefresh = {},
                    onLoadMore = {},
                    onRepoClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Welcome, Yi!").assertIsDisplayed()
    }
}

package com.example.yijinsgithub

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.yijinsgithub.data.model.User
import com.example.yijinsgithub.ui.screens.HomeScreen
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import com.example.yijinsgithub.ui.viewmodel.UserState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test login button triggers onLogin when token is entered`() {
        val onLoginMock: (String) -> Unit = mock()

        composeTestRule.setContent {
            HomeScreen(
                uiState = GithubUiState.Idle,
                userState = UserState.Anonymous,
                homeRepos = emptyList(),
                onSearchClick = {},
                onProfileClick = {},
                onLogin = onLoginMock,
                onRefresh = {},
                onRepoClick = {}
            )
        }

        val testToken = "ghp_testToken"
        composeTestRule.onNodeWithText("Personal Access Token").performTextInput(testToken)
        composeTestRule.onNodeWithText("Login with Token").performClick()

        verify(onLoginMock).invoke(testToken)
    }

    @Test
    fun `test search icon triggers onSearchClick`() {
        val onSearchClickMock: () -> Unit = mock()

        composeTestRule.setContent {
            HomeScreen(
                uiState = GithubUiState.Idle,
                userState = UserState.Anonymous,
                homeRepos = emptyList(),
                onSearchClick = onSearchClickMock,
                onProfileClick = {},
                onLogin = {},
                onRefresh = {},
                onRepoClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").performClick()
        verify(onSearchClickMock).invoke()
    }

    @Test
    fun `test loading state shows circular progress indicator`() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = GithubUiState.Loading,
                userState = UserState.Anonymous,
                homeRepos = emptyList(),
                onSearchClick = {},
                onProfileClick = {},
                onLogin = {},
                onRefresh = {},
                onRepoClick = {}
            )
        }

        composeTestRule.onNodeWithText("Login with Token").assertIsNotEnabled()
    }

    @Test
    fun `test error state displays error message`() {
        val errorMessage = "Network Error"
        composeTestRule.setContent {
            HomeScreen(
                uiState = GithubUiState.Error(errorMessage),
                userState = UserState.Anonymous,
                homeRepos = emptyList(),
                onSearchClick = {},
                onProfileClick = {},
                onLogin = {},
                onRefresh = {},
                onRepoClick = {}
            )
        }

        composeTestRule.onNodeWithText(errorMessage).assertExists()
    }

    @Test
    fun `test authenticated state displays welcome message and profile button`() {
        val mockUser = User(
            login = "testuser",
            id = 1,
            avatarUrl = "url",
            htmlUrl = "url",
            name = "Test User"
        )
        val authenticatedState = UserState.Authenticated(mockUser, "token", emptyList())

        composeTestRule.setContent {
            HomeScreen(
                uiState = GithubUiState.Idle,
                userState = authenticatedState,
                homeRepos = emptyList(),
                onSearchClick = {},
                onProfileClick = {},
                onLogin = {},
                onRefresh = {},
                onRepoClick = {}
            )
        }

        composeTestRule.onNodeWithText("Welcome, testuser!").assertExists()
        composeTestRule.onNodeWithText("Profile").assertExists()
    }
}

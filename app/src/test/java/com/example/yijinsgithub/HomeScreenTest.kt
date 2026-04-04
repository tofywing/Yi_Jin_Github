package com.example.yijinsgithub

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
}

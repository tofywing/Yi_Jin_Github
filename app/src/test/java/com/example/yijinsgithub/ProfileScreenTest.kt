package com.example.yijinsgithub

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.yijinsgithub.data.model.User
import com.example.yijinsgithub.ui.screens.ProfileScreen
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
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockUser = User(
        login = "testuser",
        id = 1,
        avatarUrl = "https://example.com/avatar.png",
        htmlUrl = "https://github.com/testuser",
        name = "Test User",
        company = "Test Co",
        location = "Test City",
        bio = "Test Bio",
        publicRepos = 10,
        followers = 5,
        following = 2,
        blog = "https://example.com"
    )

    @Test
    fun `test logout button triggers onLogout`() {
        val onLogoutMock: () -> Unit = mock()
        val authenticatedState = UserState.Authenticated(mockUser, "token", emptyList())

        composeTestRule.setContent {
            ProfileScreen(
                uiState = GithubUiState.Idle,
                userState = authenticatedState,
                onLogout = onLogoutMock,
                onCreateIssue = { _, _, _, _ -> }
            )
        }

        // The logout button has content description "Logout" in resources
        composeTestRule.onNodeWithContentDescription("Logout").performClick()
        verify(onLogoutMock).invoke()
    }

    @Test
    fun `test user information is displayed`() {
        val authenticatedState = UserState.Authenticated(mockUser, "token", emptyList())

        composeTestRule.setContent {
            ProfileScreen(
                uiState = GithubUiState.Idle,
                userState = authenticatedState,
                onLogout = {},
                onCreateIssue = { _, _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Test User").assertExists()
        composeTestRule.onNodeWithText("@testuser").assertExists()
        composeTestRule.onNodeWithText("Test Bio").assertExists()
    }
}

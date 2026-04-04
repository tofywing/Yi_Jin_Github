package com.example.yijinsgithub

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.yijinsgithub.ui.screens.RepoDetailScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepoDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test back button triggers onBack`() {
        val onBackMock: () -> Unit = mock()
        val testUrl = URLEncoder.encode("https://github.com/test/repo", StandardCharsets.UTF_8.toString())

        composeTestRule.setContent {
            RepoDetailScreen(
                url = testUrl,
                token = null,
                onBack = onBackMock
            )
        }

        // The back button has content description "Back" from R.string.back_button_desc
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        verify(onBackMock).invoke()
    }

    @Test
    fun `test title is displayed`() {
        composeTestRule.setContent {
            RepoDetailScreen(
                url = "url",
                onBack = {}
            )
        }

        // R.string.repo_detail_title is "Repository Detail"
        composeTestRule.onNodeWithText("Repository Detail").assertExists()
    }
}

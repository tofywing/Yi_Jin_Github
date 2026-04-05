package com.example.yijinsgithub

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.yijinsgithub.data.local.TokenManager
import com.example.yijinsgithub.data.model.IssueResponse
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.data.model.User
import com.example.yijinsgithub.data.repository.GithubRepository
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import com.example.yijinsgithub.ui.viewmodel.GithubViewModel
import com.example.yijinsgithub.ui.viewmodel.UserState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.whenever
import org.mockito.kotlin.anyOrNull

@OptIn(ExperimentalCoroutinesApi::class)
class GithubViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val application = mock(Application::class.java)
    private val tokenManager = mock(TokenManager::class.java)
    private val repository = mock(GithubRepository::class.java)
    
    private val tokenFlow = MutableStateFlow<String?>(null)
    
    private lateinit var viewModel: GithubViewModel

    private val mockUser = User(
        login = "testuser",
        id = 1L,
        avatarUrl = "https://avatar.url",
        htmlUrl = "https://github.com/testuser",
        name = "Test User"
    )

    private val mockRepo = Repo(
        id = 1L,
        name = "test-repo",
        fullName = "testuser/test-repo",
        description = "A test repository",
        owner = mockUser,
        stars = 100,
        forks = 10,
        language = "Kotlin",
        htmlUrl = "https://github.com/testuser/test-repo"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(tokenManager.token).thenReturn(tokenFlow)
        whenever(application.getString(any())).thenReturn("Error")
        
        runBlocking {
            whenever(repository.getPopularRepositories(any())).thenReturn(emptyList())
        }
        
        viewModel = GithubViewModel(application, tokenManager, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state is success because unconfined dispatcher runs init block`() = runTest {
        assertTrue(viewModel.uiState.value is GithubUiState.Success)
        assertEquals(UserState.Anonymous, viewModel.userState.value)
    }

    @Test
    fun `test updateSearchQuery and Language`() {
        viewModel.updateSearchQuery("android")
        viewModel.updateSearchLanguage("Kotlin")
        assertEquals("android", viewModel.searchQuery.value)
        assertEquals("Kotlin", viewModel.searchLanguage.value)
    }

    @Test
    fun `test searchRepos success updates state`() = runTest {
        val repos = listOf(mockRepo)
        whenever(repository.searchRepositories(any(), anyOrNull(), any(), any())).thenReturn(repos)

        viewModel.searchRepos("query", "Kotlin")
        
        assertEquals(repos, viewModel.searchRepos.value)
        assertEquals(GithubUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `test login flow`() = runTest {
        val token = "ghp_token"
        whenever(repository.getCurrentUser(token)).thenReturn(mockUser)
        whenever(repository.getUserRepositories(token)).thenReturn(listOf(mockRepo))

        viewModel.login(token)
        verify(tokenManager).saveToken(token)
        
        tokenFlow.value = token
        
        assertTrue(viewModel.userState.value is UserState.Authenticated)
        assertEquals(GithubUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `test logout flow`() = runTest {
        viewModel.logout()
        verify(tokenManager).clearToken()
        assertEquals(UserState.Anonymous, viewModel.userState.value)
    }

    @Test
    fun `test createIssue flow`() = runTest {
        val token = "token"
        // Ensure user is authenticated first
        whenever(repository.getCurrentUser(token)).thenReturn(mockUser)
        whenever(repository.getUserRepositories(token)).thenReturn(listOf(mockRepo))
        tokenFlow.value = token

        whenever(repository.createIssue(any(), any(), any(), any(), any()))
            .thenReturn(IssueResponse(1L, 1, "Title", "Body"))

        viewModel.createIssue("owner", "repo", "Title", "Body")
        
        verify(repository, atLeastOnce()).getCurrentUser(token)
        assertEquals(GithubUiState.Success, viewModel.uiState.value)
    }
}

package com.example.yijinsgithub

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.yijinsgithub.data.local.TokenManager
import com.example.yijinsgithub.data.repository.GithubRepository
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import com.example.yijinsgithub.ui.viewmodel.GithubViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class GithubViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val application = mock(Application::class.java)
    private val tokenManager = mock(TokenManager::class.java)
    private val repository = mock(GithubRepository::class.java)
    
    private lateinit var viewModel: GithubViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mock token flow to prevent NullPointerException during init
        `when`(tokenManager.token).thenReturn(flowOf(null))
        
        viewModel = GithubViewModel(application, tokenManager, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial uiState is Idle`() {
        assertEquals(GithubUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `test updateSearchQuery updates state`() {
        viewModel.updateSearchQuery("kotlin")
        assertEquals("kotlin", viewModel.searchQuery.value)
    }

    @Test
    fun `test updateSearchLanguage updates state`() {
        viewModel.updateSearchLanguage("Kotlin")
        assertEquals("Kotlin", viewModel.searchLanguage.value)
    }
}

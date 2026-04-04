package com.example.yijinsgithub.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.yijinsgithub.R
import com.example.yijinsgithub.common.Constants
import com.example.yijinsgithub.data.local.TokenManager
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.data.model.User
import com.example.yijinsgithub.data.remote.GithubService
import com.example.yijinsgithub.data.repository.GithubRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * ViewModel responsible for managing the state and business logic of the GitHub application.
 * It handles authentication, repository searching, profile loading, and issue creation.
 *
 * @param application The application context.
 */
class GithubViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository: GithubRepository

    private val _uiState = MutableStateFlow<GithubUiState>(GithubUiState.Idle)
    /**
     * Observable state representing the current UI status (Loading, Success, Error, etc.).
     */
    val uiState: StateFlow<GithubUiState> = _uiState.asStateFlow()

    private val _userState = MutableStateFlow<UserState>(UserState.Anonymous)
    /**
     * Observable state representing the current user's authentication status and data.
     */
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _homeRepos = MutableStateFlow<List<Repo>>(emptyList())
    /**
     * Observable state representing the list of repositories for the Home screen.
     */
    val homeRepos: StateFlow<List<Repo>> = _homeRepos.asStateFlow()

    private val _searchRepos = MutableStateFlow<List<Repo>>(emptyList())
    /**
     * Observable state representing the list of repositories for the Search screen.
     */
    val searchRepos: StateFlow<List<Repo>> = _searchRepos.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    private var homeJob: Job? = null
    private var searchJob: Job? = null
    private var profileJob: Job? = null
    private var issueJob: Job? = null

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder().baseUrl(Constants.GITHUB_BASE_URL).client(client)
            .addConverterFactory(json.asConverterFactory(Constants.MEDIA_TYPE_JSON.toMediaType()))
            .build()

        val service = retrofit.create(GithubService::class.java)
        repository = GithubRepository(service)

        // Monitor token changes and load user profile or popular repos accordingly
        viewModelScope.launch {
            tokenManager.token.collectLatest { token ->
                token?.let { nonNullToken ->
                    loadUserProfile(nonNullToken, isInitialLoad = true)
                } ?: run {
                    _userState.value = UserState.Anonymous
                    loadPopularRepos(isInitialLoad = true)
                }
            }
        }
    }

    /**
     * Loads a list of popular repositories from GitHub.
     *
     * @param isInitialLoad Whether this is the first time data is being loaded for the screen.
     */
    private fun loadPopularRepos(isInitialLoad: Boolean = false) {
        homeJob?.cancel()
        homeJob = viewModelScope.launch {
            _uiState.value = if (isInitialLoad) GithubUiState.Loading else GithubUiState.Refreshing
            try {
                _homeRepos.value = repository.getPopularRepositories()
                _uiState.value = GithubUiState.Success
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                _uiState.value = GithubUiState.Error(
                    e.message ?: getApplication<Application>().getString(R.string.error_unknown)
                )
            }
        }
    }

    /**
     * Searches for repositories based on keywords and an optional language.
     *
     * @param query The search keywords.
     * @param language The optional programming language filter.
     * @param isRefresh Whether this is a refresh operation (triggered by pull-to-refresh).
     */
    fun searchRepos(query: String, language: String?, isRefresh: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = if (isRefresh) GithubUiState.Refreshing else GithubUiState.Loading
            try {
                _searchRepos.value = repository.searchRepositories(query, language)
                _uiState.value = GithubUiState.Success
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                _uiState.value = GithubUiState.Error(
                    e.message ?: getApplication<Application>().getString(R.string.error_search_failed)
                )
            }
        }
    }

    /**
     * Loads the profile and repositories for the authenticated user.
     *
     * @param token The GitHub Personal Access Token.
     * @param isInitialLoad Whether this is the first time data is being loaded for the screen.
     */
    private fun loadUserProfile(token: String, isInitialLoad: Boolean = false) {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _uiState.value = if (isInitialLoad) GithubUiState.Loading else GithubUiState.Refreshing
            try {
                val user = repository.getCurrentUser(token)
                val userRepos = repository.getUserRepositories(token)
                _userState.value = UserState.Authenticated(user, token, userRepos)
                _homeRepos.value = userRepos
                _uiState.value = GithubUiState.Success
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                _userState.value = UserState.Anonymous
                tokenManager.clearToken()
                _uiState.value = GithubUiState.Error(
                    e.message ?: getApplication<Application>().getString(R.string.error_unknown)
                )
            }
        }
    }

    /**
     * Attempts to log in by saving the provided PAT.
     *
     * @param token The raw GitHub Personal Access Token.
     */
    fun login(token: String) {
        viewModelScope.launch {
            _uiState.value = GithubUiState.Loading
            tokenManager.saveToken(token)
        }
    }

    /**
     * Logs out the user by clearing the stored token and cancelling active jobs.
     */
    fun logout() {
        cancelAllJobs()
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }

    /**
     * Refreshes the data based on the current authentication status.
     */
    fun refresh() {
        viewModelScope.launch {
            tokenManager.token.firstOrNull()?.let { currentToken ->
                loadUserProfile(currentToken, isInitialLoad = false)
            } ?: run {
                loadPopularRepos(isInitialLoad = false)
            }
        }
    }

    /**
     * Creates a new issue in a specific repository.
     *
     * @param owner The owner of the repository.
     * @param repo The name of the repository.
     * @param title The title of the issue.
     * @param body The body text of the issue.
     */
    fun createIssue(owner: String, repo: String, title: String, body: String) {
        val currentUserState = _userState.value
        if (currentUserState is UserState.Authenticated) {
            issueJob?.cancel()
            issueJob = viewModelScope.launch {
                _uiState.value = GithubUiState.Loading
                try {
                    repository.createIssue(currentUserState.token, owner, repo, title, body)
                    loadUserProfile(currentUserState.token, isInitialLoad = false)
                } catch (e: Exception) {
                    if (e is CancellationException) return@launch
                    val errorMsg = getApplication<Application>().getString(
                        R.string.error_create_issue_failed, e.message ?: ""
                    )
                    _uiState.value = GithubUiState.Error(errorMsg)
                }
            }
        }
    }

    /**
     * Cancels any active jobs related to the home screen.
     */
    fun cancelHome() {
        homeJob?.cancel()
        val currentState = _uiState.value
        if (currentState is GithubUiState.Loading || currentState is GithubUiState.Refreshing) {
            _uiState.value = GithubUiState.Idle
        }
    }

    /**
     * Cancels any active jobs related to the search screen.
     */
    fun cancelSearch() {
        searchJob?.cancel()
        if (_uiState.value is GithubUiState.Loading || _uiState.value is GithubUiState.Refreshing) {
            _uiState.value = GithubUiState.Idle
        }
    }

    /**
     * Cancels any active jobs related to the profile screen.
     */
    fun cancelProfile() {
        profileJob?.cancel()
        issueJob?.cancel()
        val currentState = _uiState.value
        if (currentState is GithubUiState.Loading || currentState is GithubUiState.Refreshing) {
            _uiState.value = GithubUiState.Idle
        }
    }

    /**
     * Cancels all active background jobs.
     */
    private fun cancelAllJobs() {
        homeJob?.cancel()
        searchJob?.cancel()
        profileJob?.cancel()
        issueJob?.cancel()
    }
}

/**
 * Represents the state of the UI for the GitHub application.
 */
sealed class GithubUiState {
    /** Idle state when no operation is in progress. */
    object Idle : GithubUiState()
    /** Loading state for full-screen operations (login, initial load, search). */
    object Loading : GithubUiState()
    /** Refreshing state specifically for pull-to-refresh operations. */
    object Refreshing : GithubUiState()
    /** Success state when an operation completes successfully. */
    object Success : GithubUiState()
    /** Error state containing an error message. */
    data class Error(val message: String) : GithubUiState()
}

/**
 * Represents the user's authentication and data state.
 */
sealed class UserState {
    /** Anonymous state when no user is logged in. */
    object Anonymous : UserState()
    /** Authenticated state containing user profile, token, and repositories. */
    data class Authenticated(val user: User, val token: String, val repos: List<Repo>) : UserState()
}

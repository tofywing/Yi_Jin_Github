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
 */
class GithubViewModel @JvmOverloads constructor(
    application: Application,
    private val tokenManager: TokenManager = TokenManager(application),
    private val repository: GithubRepository = createDefaultRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<GithubUiState>(GithubUiState.Idle)
    val uiState: StateFlow<GithubUiState> = _uiState.asStateFlow()

    private val _userState = MutableStateFlow<UserState>(UserState.Anonymous)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _homeRepos = MutableStateFlow<List<Repo>>(emptyList())
    val homeRepos: StateFlow<List<Repo>> = _homeRepos.asStateFlow()

    private val _searchRepos = MutableStateFlow<List<Repo>>(emptyList())
    val searchRepos: StateFlow<List<Repo>> = _searchRepos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchLanguage = MutableStateFlow("")
    val searchLanguage: StateFlow<String> = _searchLanguage.asStateFlow()

    private var homeJob: Job? = null
    private var searchJob: Job? = null
    private var profileJob: Job? = null
    private var issueJob: Job? = null

    init {
        // Monitor token changes and load user profile or popular repos accordingly
        viewModelScope.launch {
            tokenManager.token.collectLatest { token ->
                token?.let { nonNullToken ->
                    loadUserProfile(nonNullToken, isInitialLoad = true)
                } ?: run {
                    _userState.value = UserState.Anonymous
                    clearSearchState()
                    loadPopularRepos(isInitialLoad = true)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchLanguage(language: String) {
        _searchLanguage.value = language
    }

    private fun clearSearchState() {
        _searchQuery.value = ""
        _searchLanguage.value = ""
        _searchRepos.value = emptyList()
        searchJob?.cancel()
    }

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

    fun login(token: String) {
        viewModelScope.launch {
            _uiState.value = GithubUiState.Loading
            tokenManager.saveToken(token)
        }
    }

    fun logout() {
        cancelAllJobs()
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            tokenManager.token.firstOrNull()?.let { currentToken ->
                loadUserProfile(currentToken, isInitialLoad = false)
            } ?: run {
                loadPopularRepos(isInitialLoad = false)
            }
        }
    }

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

    fun cancelHome() {
        homeJob?.cancel()
        val currentState = _uiState.value
        if (currentState is GithubUiState.Loading || currentState is GithubUiState.Refreshing) {
            _uiState.value = GithubUiState.Idle
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        if (_uiState.value is GithubUiState.Loading || _uiState.value is GithubUiState.Refreshing) {
            _uiState.value = GithubUiState.Idle
        }
    }

    fun cancelProfile() {
        profileJob?.cancel()
        issueJob?.cancel()
        val currentState = _uiState.value
        if (currentState is GithubUiState.Loading || currentState is GithubUiState.Refreshing) {
            _uiState.value = GithubUiState.Idle
        }
    }

    private fun cancelAllJobs() {
        homeJob?.cancel()
        searchJob?.cancel()
        profileJob?.cancel()
        issueJob?.cancel()
    }

    companion object {
        private fun createDefaultRepository(): GithubRepository {
            val json = Json { ignoreUnknownKeys = true }
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder().addInterceptor(logging).build()
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.GITHUB_BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory(Constants.MEDIA_TYPE_JSON.toMediaType()))
                .build()
            val service = retrofit.create(GithubService::class.java)
            return GithubRepository(service)
        }
    }
}

sealed class GithubUiState {
    object Idle : GithubUiState()
    object Loading : GithubUiState()
    object Refreshing : GithubUiState()
    object Success : GithubUiState()
    data class Error(val message: String) : GithubUiState()
}

sealed class UserState {
    object Anonymous : UserState()
    data class Authenticated(val user: User, val token: String, val repos: List<Repo>) : UserState()
}

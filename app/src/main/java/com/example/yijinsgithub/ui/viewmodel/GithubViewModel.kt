package com.example.yijinsgithub.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.yijinsgithub.R
import com.example.yijinsgithub.common.Constants
import com.example.yijinsgithub.common.Constants.DEFAULT_PAGE
import com.example.yijinsgithub.common.Constants.DEFAULT_PER_PAGE
import com.example.yijinsgithub.data.local.TokenManager
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.data.model.User
import com.example.yijinsgithub.data.remote.AuthInterceptor
import com.example.yijinsgithub.data.remote.GithubService
import com.example.yijinsgithub.data.repository.GithubRepository
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
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException

/**
 * ViewModel responsible for managing the application state and business logic for GitHub interactions.
 */
class GithubViewModel @JvmOverloads constructor(
    private val app: Application,
    private val tokenManager: TokenManager = TokenManager(app),
    private val repository: GithubRepository = createDefaultRepository(tokenManager)
) : AndroidViewModel(app) {

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

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Pagination & Loading States
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isLastPage = MutableStateFlow(false)
    val isLastPage: StateFlow<Boolean> = _isLastPage.asStateFlow()

    private val _isSearchLastPage = MutableStateFlow(false)
    val isSearchLastPage: StateFlow<Boolean> = _isSearchLastPage.asStateFlow()

    private var homePage = DEFAULT_PAGE
    private var searchPage = DEFAULT_PAGE

    private var homeJob: Job? = null
    private var searchJob: Job? = null
    private var profileJob: Job? = null
    private var issueJob: Job? = null

    private val connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOffline.value = false
            // Clear error state if it was a network error
            if (_uiState.value is GithubUiState.Error && 
                (_uiState.value as GithubUiState.Error).message == app.getString(R.string.error_no_internet)) {
                _uiState.value = GithubUiState.Idle
            }
        }

        override fun onLost(network: Network) {
            _isOffline.value = true
            _uiState.value = GithubUiState.Error(app.getString(R.string.error_no_internet))
        }
    }

    init {
        checkInitialNetworkState()
        registerNetworkCallback()
        
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

    private fun checkInitialNetworkState() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isOffline.value = capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(networkCallback)
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
        searchPage = DEFAULT_PAGE
        _isSearchLastPage.value = false
        searchJob?.cancel()
    }

    fun clearErrorState() {
        if (_uiState.value is GithubUiState.Error) {
            _uiState.value = GithubUiState.Idle
        }
    }

    private fun loadPopularRepos(isInitialLoad: Boolean = false) {
        if (_isOffline.value) {
            _uiState.value = GithubUiState.Error(app.getString(R.string.error_no_internet))
            return
        }
        homeJob?.cancel()
        homePage = DEFAULT_PAGE
        _isLastPage.value = false
        homeJob = viewModelScope.launch {
            _uiState.value = if (isInitialLoad) GithubUiState.Loading else GithubUiState.Refreshing
            try {
                val results = repository.getPopularRepositories(page = homePage)
                _homeRepos.value = results
                _isLastPage.value = results.size < DEFAULT_PER_PAGE
                _uiState.value = GithubUiState.Success
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun loadMoreHomeRepos() {
        if (_isOffline.value || _isLoadingMore.value || _isLastPage.value || _uiState.value is GithubUiState.Loading) return

        _isLoadingMore.value = true
        homePage++

        viewModelScope.launch {
            try {
                val currentUserState = _userState.value
                val newRepos = if (currentUserState is UserState.Authenticated) {
                    repository.getUserRepositories(currentUserState.token, page = homePage)
                } else {
                    repository.getPopularRepositories(page = homePage)
                }

                if (newRepos.isNotEmpty()) {
                    _homeRepos.value = _homeRepos.value + newRepos
                }
                _isLastPage.value = newRepos.size < DEFAULT_PER_PAGE
            } catch (e: Exception) {
                homePage--
                handleError(e, isPagination = true)
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun searchRepos(query: String, language: String?, isRefresh: Boolean = false) {
        if (_isOffline.value) {
            _uiState.value = GithubUiState.Error(app.getString(R.string.error_no_internet))
            return
        }
        searchJob?.cancel()
        searchPage = DEFAULT_PAGE
        _isSearchLastPage.value = false
        searchJob = viewModelScope.launch {
            _uiState.value = if (isRefresh) GithubUiState.Refreshing else GithubUiState.Loading
            try {
                val results = repository.searchRepositories(query, language, page = searchPage)
                _searchRepos.value = results
                _isLastPage.value = results.size < DEFAULT_PER_PAGE
                _uiState.value = GithubUiState.Success
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun loadMoreSearchRepos() {
        if (_isOffline.value || _isLoadingMore.value || _isSearchLastPage.value || _uiState.value is GithubUiState.Loading) return

        val query = _searchQuery.value
        if (query.isBlank()) return

        _isLoadingMore.value = true
        searchPage++

        viewModelScope.launch {
            try {
                val newRepos =
                    repository.searchRepositories(query, _searchLanguage.value, page = searchPage)
                if (newRepos.isNotEmpty()) {
                    _searchRepos.value = _searchRepos.value + newRepos
                }
                _isSearchLastPage.value = newRepos.size < DEFAULT_PER_PAGE
            } catch (e: Exception) {
                searchPage--
                handleError(e, isPagination = true)
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private fun loadUserProfile(token: String, isInitialLoad: Boolean = false) {
        if (_isOffline.value && isInitialLoad) {
            _uiState.value = GithubUiState.Error(app.getString(R.string.error_no_internet))
            return
        }
        profileJob?.cancel()
        homePage = DEFAULT_PAGE
        _isLastPage.value = false
        profileJob = viewModelScope.launch {
            _uiState.value = if (isInitialLoad) GithubUiState.Loading else GithubUiState.Refreshing
            try {
                val user = repository.getCurrentUser(token)
                val userRepos = repository.getUserRepositories(token, page = homePage)
                _userState.value = UserState.Authenticated(user, token, userRepos)
                _homeRepos.value = userRepos
                _isLastPage.value = userRepos.size < DEFAULT_PER_PAGE
                _uiState.value = GithubUiState.Success
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                _userState.value = UserState.Anonymous
                tokenManager.clearToken()
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception, isPagination: Boolean = false) {
        if (e is CancellationException) return

        val message = when (e) {
            is IOException -> app.getString(R.string.error_no_internet)
            is HttpException -> {
                when (e.code()) {
                    403 -> app.getString(R.string.error_rate_limit)
                    401 -> app.getString(R.string.error_unauthorized)
                    else -> app.getString(R.string.error_network_generic, e.code())
                }
            }
            else -> e.message ?: app.getString(R.string.error_unknown)
        }

        _uiState.value = GithubUiState.Error(message)
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
        if (_isOffline.value) {
            _uiState.value = GithubUiState.Error(app.getString(R.string.error_no_internet))
            return
        }
        val currentUserState = _userState.value
        if (currentUserState is UserState.Authenticated) {
            issueJob?.cancel()
            issueJob = viewModelScope.launch {
                _uiState.value = GithubUiState.Loading
                try {
                    repository.createIssue(currentUserState.token, owner, repo, title, body)
                    loadUserProfile(currentUserState.token, isInitialLoad = false)
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    fun cancelHome() {
        homeJob?.cancel()
        if (_uiState.value is GithubUiState.Loading) _uiState.value = GithubUiState.Idle
    }

    fun cancelSearch() {
        searchJob?.cancel()
        if (_uiState.value is GithubUiState.Loading) _uiState.value = GithubUiState.Idle
    }

    private fun cancelAllJobs() {
        homeJob?.cancel()
        searchJob?.cancel()
        profileJob?.cancel()
        issueJob?.cancel()
    }

    companion object {
        private fun createDefaultRepository(tokenManager: TokenManager): GithubRepository {
            val json = Json { ignoreUnknownKeys = true }
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val authInterceptor = AuthInterceptor(tokenManager)

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .build()

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

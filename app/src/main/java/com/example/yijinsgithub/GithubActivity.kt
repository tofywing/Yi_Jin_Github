/**
 * Main entry point for the Yi Jin's Github application.
 * 
 * This Activity serves as the single activity for the application, hosting the Compose-based UI.
 * It implements several key responsibilities:
 * 1. **Security Integrity Check**: On startup, it performs a root and emulator detection check 
 *    using the RootBeer library and verifies the APK signature to ensure the application 
 *    runs in a secure, untampered environment.
 * 2. **Navigation Management**: Orchestrates the navigation between different screens (Home, Search, Profile, Detail)
 *    using Jetpack Navigation Compose.
 * 3. **UI State Persistence**: Manages LazyListStates for major screens to maintain scroll positions 
 *    during navigation.
 * 4. **Edge-to-Edge Support**: Enables modern Android edge-to-edge display characteristics.
 */
package com.example.yijinsgithub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yijinsgithub.ui.viewmodel.GithubViewModel
import com.example.yijinsgithub.ui.viewmodel.UserState
import com.example.yijinsgithub.ui.screens.HomeScreen
import com.example.yijinsgithub.ui.screens.ProfileScreen
import com.example.yijinsgithub.ui.screens.SearchScreen
import com.example.yijinsgithub.ui.screens.RepoDetailScreen
import com.example.yijinsgithub.ui.screens.SecurityRiskScreen
import com.example.yijinsgithub.ui.theme.YiJinsGithubTheme
import com.example.yijinsgithub.ui.navigation.Screen
import com.scottyab.rootbeer.RootBeer
import com.example.yijinsgithub.security.SignatureVerifier
import com.example.yijinsgithub.security.SecurityAudit

class GithubActivity : ComponentActivity() {
    private val viewModel: GithubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YiJinsGithubTheme {
                val rootBeer = remember { RootBeer(this) }
                var isSecurityRiskDetected by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    // SECURITY INTEGRITY CHECK:
                    val isRooted = rootBeer.isRooted
                    val isSignatureValid = SignatureVerifier.isSignatureValid(this@GithubActivity)
                    
                    if (isRooted || !isSignatureValid) {
                        // SECURITY AUDITING: Log the specific violation for tracking
                        val reason = if (isRooted) "Root/Emulator detected" else "APK Signature Mismatch"
                        SecurityAudit.logIntegrityViolation(reason)

                        isSecurityRiskDetected = true
                    }
                }

                if (isSecurityRiskDetected) {
                    // Display the dedicated security risk screen if root is detected or signature is invalid
                    // to prevent potential data tampering or hooking in insecure environments.
                    SecurityRiskScreen()
                } else {
                    val navController = rememberNavController()
                    
                    // Maintain list states at this level to preserve scroll positions across navigation
                    val homeListState = rememberLazyListState()
                    val searchListState = rememberLazyListState()

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(navController = navController, startDestination = Screen.Home.route) {
                            composable(Screen.Home.route) {
                                val homeRepos by viewModel.homeRepos.collectAsState()
                                val uiState by viewModel.uiState.collectAsState()
                                val userState by viewModel.userState.collectAsState()
                                val isLoadingMore by viewModel.isLoadingMore.collectAsState()
                                val isLastPage by viewModel.isLastPage.collectAsState()

                                LaunchedEffect(Unit) {
                                    viewModel.clearErrorState()
                                }

                                HomeScreen(
                                    uiState = uiState,
                                    userState = userState,
                                    homeRepos = homeRepos,
                                    isLoadingMore = isLoadingMore,
                                    isLastPage = isLastPage,
                                    listState = homeListState,
                                    onSearchClick = { navController.navigate(Screen.Search.route) },
                                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                                    onLogin = { viewModel.login(it) },
                                    onRefresh = { viewModel.refresh() },
                                    onLoadMore = { viewModel.loadMoreHomeRepos() },
                                    onRepoClick = { repo ->
                                        navController.navigate(Screen.RepoDetail.createRoute(repo.htmlUrl))
                                    }
                                )
                            }
                            composable(Screen.Search.route) {
                                val searchRepos by viewModel.searchRepos.collectAsState()
                                val uiState by viewModel.uiState.collectAsState()
                                val searchQuery by viewModel.searchQuery.collectAsState()
                                val searchLanguage by viewModel.searchLanguage.collectAsState()
                                val isLoadingMore by viewModel.isLoadingMore.collectAsState()
                                val isLastPage by viewModel.isSearchLastPage.collectAsState()

                                LaunchedEffect(Unit) {
                                    viewModel.clearErrorState()
                                }

                                SearchScreen(
                                    uiState = uiState,
                                    repos = searchRepos,
                                    query = searchQuery,
                                    language = searchLanguage,
                                    isLoadingMore = isLoadingMore,
                                    isLastPage = isLastPage,
                                    listState = searchListState,
                                    onQueryChange = { viewModel.updateSearchQuery(it) },
                                    onLanguageChange = { viewModel.updateSearchLanguage(it) },
                                    onSearch = { q, l, r -> viewModel.searchRepos(q, l, r) },
                                    onLoadMore = { viewModel.loadMoreSearchRepos() },
                                    onRepoClick = { repo ->
                                        navController.navigate(Screen.RepoDetail.createRoute(repo.htmlUrl))
                                    }
                                )
                            }
                            composable(Screen.Profile.route) {
                                val userState by viewModel.userState.collectAsState()
                                val uiState by viewModel.uiState.collectAsState()

                                LaunchedEffect(Unit) {
                                    viewModel.clearErrorState()
                                }

                                ProfileScreen(
                                    uiState = uiState,
                                    userState = userState,
                                    onLogout = {
                                        viewModel.logout()
                                        navController.popBackStack(Screen.Home.route, inclusive = false)
                                    },
                                    onCreateIssue = { owner, repo, title, body ->
                                        viewModel.createIssue(owner, repo, title, body)
                                    }
                                )
                            }
                            composable(
                                route = Screen.RepoDetail.route,
                                arguments = listOf(navArgument("url") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val url = backStackEntry.arguments?.getString("url") ?: ""
                                val userState by viewModel.userState.collectAsState()
                                val token = (userState as? UserState.Authenticated)?.token

                                RepoDetailScreen(
                                    url = url,
                                    token = token,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

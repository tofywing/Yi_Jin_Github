package com.example.yijinsgithub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.yijinsgithub.ui.theme.YiJinsGithubTheme
import com.example.yijinsgithub.ui.navigation.Screen

/**
 * The main entry point of the application.
 * This activity sets up the Jetpack Compose UI and manages navigation between different screens.
 */
class GithubActivity : ComponentActivity() {
    /**
     * The shared ViewModel instance that manages the app's state and business logic.
     */
    private val viewModel: GithubViewModel by viewModels()

    /**
     * Initializes the activity, enables edge-to-edge display, and sets the Compose content.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YiJinsGithubTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up the Navigation Host with the home screen as the starting destination
                    NavHost(navController = navController, startDestination = Screen.Home.route) {
                        // Home Screen Route
                        composable(Screen.Home.route) {
                            val homeRepos by viewModel.homeRepos.collectAsState()
                            val uiState by viewModel.uiState.collectAsState()
                            val userState by viewModel.userState.collectAsState()

                            HomeScreen(
                                uiState = uiState,
                                userState = userState,
                                homeRepos = homeRepos,
                                onSearchClick = { navController.navigate(Screen.Search.route) },
                                onProfileClick = { navController.navigate(Screen.Profile.route) },
                                onLogin = { viewModel.login(it) },
                                onRefresh = { viewModel.refresh() },
                                onRepoClick = { repo ->
                                    navController.navigate(Screen.RepoDetail.createRoute(repo.htmlUrl))
                                },
                                onDispose = { viewModel.cancelHome() }
                            )
                        }
                        // Search Screen Route
                        composable(Screen.Search.route) {
                            val searchRepos by viewModel.searchRepos.collectAsState()
                            val uiState by viewModel.uiState.collectAsState()

                            SearchScreen(
                                uiState = uiState,
                                repos = searchRepos,
                                onSearch = { q, l, r -> viewModel.searchRepos(q, l, r) },
                                onRepoClick = { repo ->
                                    navController.navigate(Screen.RepoDetail.createRoute(repo.htmlUrl))
                                },
                                onDispose = { viewModel.cancelSearch() }
                            )
                        }
                        // Profile Screen Route
                        composable(Screen.Profile.route) {
                            val userState by viewModel.userState.collectAsState()
                            val uiState by viewModel.uiState.collectAsState()

                            ProfileScreen(
                                uiState = uiState,
                                userState = userState,
                                onLogout = {
                                    viewModel.logout()
                                    navController.popBackStack(Screen.Home.route, inclusive = false)
                                },
                                onCreateIssue = { owner, repo, title, body ->
                                    viewModel.createIssue(owner, repo, title, body)
                                },
                                onDispose = { viewModel.cancelProfile() }
                            )
                        }
                        // Repository Detail Screen Route with URL argument
                        composable(
                            route = Screen.RepoDetail.route,
                            arguments = listOf(navArgument("url") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val url = backStackEntry.arguments?.getString("url") ?: ""
                            val userState by viewModel.userState.collectAsState()
                            // Pass the token if the user is authenticated to support private repo viewing if applicable
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

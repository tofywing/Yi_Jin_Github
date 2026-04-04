package com.example.yijinsgithub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import com.example.yijinsgithub.R
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.ui.components.RepoList
import com.example.yijinsgithub.ui.theme.Dimens
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import com.example.yijinsgithub.ui.viewmodel.UserState

/**
 * The main screen of the application that displays a list of popular repositories
 * or user-specific repositories if authenticated.
 *
 * @param uiState The current UI state from the ViewModel.
 * @param userState The current user authentication state.
 * @param homeRepos The list of repositories (recommended or user's) to display.
 * @param onSearchClick Callback when the search icon is clicked.
 * @param onProfileClick Callback when the profile button is clicked.
 * @param onLogin Callback when a user attempts to log in with a PAT.
 * @param onRefresh Callback when the user triggers a refresh action.
 * @param onRepoClick Callback when a repository item is clicked.
 * @param onDispose Callback to clean up resources when leaving the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: GithubUiState,
    userState: UserState,
    homeRepos: List<Repo>,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogin: (String) -> Unit,
    onRefresh: () -> Unit,
    onRepoClick: (Repo) -> Unit,
    onDispose: () -> Unit = {}
) {
    // Cancel async work when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    // Capture state into local variables for stable smart casting
    val currentUiState = uiState
    val currentUserState = userState
    val isLoading = currentUiState is GithubUiState.Loading
    val isRefreshing = currentUiState is GithubUiState.Refreshing

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.home_title)) },
                    actions = {
                        IconButton(onClick = onSearchClick, enabled = !isLoading) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_content_description)
                            )
                        }
                        (currentUserState as? UserState.Authenticated)?.let {
                            TextButton(onClick = onProfileClick, enabled = !isLoading) {
                                Text(stringResource(R.string.profile_button))
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (currentUiState is GithubUiState.Error) {
                    Text(
                        text = currentUiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(Dimens.PaddingLarge)
                    )
                }

                when (currentUserState) {
                    is UserState.Anonymous -> {
                        LoginSection(onLogin = onLogin, isLoading = isLoading)
                    }

                    is UserState.Authenticated -> {
                        Text(
                            text = stringResource(R.string.welcome_message, currentUserState.user.login),
                            modifier = Modifier.padding(Dimens.PaddingLarge),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                RepoList(
                    repos = homeRepos,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    onRepoClick = onRepoClick
                )
            }
        }

        // Full screen loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

/**
 * A section on the home screen that allows anonymous users to log in using a GitHub PAT.
 *
 * @param onLogin Callback when the login button is clicked.
 * @param isLoading Whether a login operation is currently in progress.
 */
@Composable
fun LoginSection(onLogin: (String) -> Unit, isLoading: Boolean) {
    var token by rememberSaveable { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.PaddingLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(Dimens.PaddingLarge)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Dimens.SpacerMedium))
                Text(
                    text = stringResource(R.string.auth_required_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpacerMedium))
            Text(
                text = stringResource(R.string.auth_required_desc),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text(stringResource(R.string.pat_label)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.pat_placeholder)) },
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(Dimens.SpacerLarge))
            Button(
                onClick = { onLogin(token) },
                modifier = Modifier.align(Alignment.End),
                enabled = token.isNotBlank() && !isLoading
            ) {
                Text(stringResource(R.string.login_button))
            }
        }
    }
}

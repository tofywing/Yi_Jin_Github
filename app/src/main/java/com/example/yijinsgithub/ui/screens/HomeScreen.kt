package com.example.yijinsgithub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.yijinsgithub.R
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.ui.components.RepoList
import com.example.yijinsgithub.ui.theme.Dimens
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import com.example.yijinsgithub.ui.viewmodel.UserState

/**
 * The main screen of the application that displays a list of popular repositories
 * or user-specific repositories if authenticated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: GithubUiState,
    userState: UserState,
    homeRepos: List<Repo>,
    isLoadingMore: Boolean,
    isLastPage: Boolean,
    listState: LazyListState = rememberLazyListState(),
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogin: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRepoClick: (Repo) -> Unit,
    onDispose: () -> Unit = {}
) {
    DisposableEffect(Unit) {
        onDispose { onDispose() }
    }

    // Show pull-to-refresh indicator for both initial loading and manual refreshing
    val isRefreshing = uiState is GithubUiState.Loading || uiState is GithubUiState.Refreshing

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.home_title)) },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_content_description)
                            )
                        }
                        (userState as? UserState.Authenticated)?.let {
                            TextButton(onClick = onProfileClick) {
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
                if (uiState is GithubUiState.Error) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(Dimens.PaddingLarge)
                    )
                }

                when (userState) {
                    is UserState.Anonymous -> {
                        LoginSection(
                            onLogin = onLogin,
                            isLoading = uiState is GithubUiState.Loading
                        )
                    }

                    is UserState.Authenticated -> {
                        Text(
                            text = stringResource(R.string.welcome_message, userState.user.login),
                            modifier = Modifier.padding(Dimens.PaddingLarge),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                RepoList(
                    repos = homeRepos,
                    listState = listState,
                    isRefreshing = isRefreshing,
                    isLoadingMore = isLoadingMore,
                    isLastPage = isLastPage,
                    onRefresh = onRefresh,
                    onLoadMore = onLoadMore,
                    onRepoClick = onRepoClick
                )
            }
        }
    }
}

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
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(), // SECURITY: Mask token input
                singleLine = true
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

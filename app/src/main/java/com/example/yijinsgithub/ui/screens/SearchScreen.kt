package com.example.yijinsgithub.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.example.yijinsgithub.R
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.ui.components.RepoList
import com.example.yijinsgithub.ui.theme.Dimens
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import kotlinx.coroutines.launch

/**
 * A screen that allows users to search for GitHub repositories.
 *
 * @param onSearch Initial search or refresh.
 * @param onLoadMore Triggered for infinite scrolling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: GithubUiState,
    repos: List<Repo>,
    query: String,
    language: String,
    isLoadingMore: Boolean,
    isLastPage: Boolean,
    listState: LazyListState,
    onQueryChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onSearch: (String, String?, Boolean) -> Unit,
    onLoadMore: () -> Unit,
    onRepoClick: (Repo) -> Unit,
    onDispose: () -> Unit = {}
) {
    DisposableEffect(Unit) {
        onDispose { onDispose() }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = uiState is GithubUiState.Loading
    val scope = rememberCoroutineScope()
    
    // Track if we should ensure we're at the top after a new search completes
    var shouldScrollToTop by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is GithubUiState.Success && shouldScrollToTop) {
            // Final check to ensure we are at the top when data arrives
            listState.animateScrollToItem(0)
            shouldScrollToTop = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.search_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(Dimens.PaddingLarge)) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacerMedium)
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            label = { Text(stringResource(R.string.search_keywords_label)) },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        )
                        OutlinedTextField(
                            value = language,
                            onValueChange = onLanguageChange,
                            label = { Text(stringResource(R.string.search_language_label)) },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        label = { Text(stringResource(R.string.search_keywords_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacerMedium))
                    OutlinedTextField(
                        value = language,
                        onValueChange = onLanguageChange,
                        label = { Text(stringResource(R.string.search_language_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacerMedium))
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onSearch(query, language.ifBlank { null }, false)
                        shouldScrollToTop = true
                        // Smoothly animate to top immediately for better visual feedback
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = query.isNotBlank() && !isLoading
                ) {
                    Text(stringResource(R.string.search_button))
                }
                
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.PaddingSmall)
                    )
                }
            }

            val currentUiState = uiState
            if (currentUiState is GithubUiState.Error) {
                Text(
                    text = currentUiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(Dimens.PaddingLarge)
                )
            }

            RepoList(
                repos = repos,
                listState = listState,
                isRefreshing = currentUiState is GithubUiState.Refreshing,
                isLoadingMore = isLoadingMore,
                isLastPage = isLastPage,
                onRefresh = {
                    if (query.isNotBlank()) {
                        onSearch(query, language.ifBlank { null }, true)
                    }
                },
                onLoadMore = onLoadMore,
                onRepoClick = onRepoClick
            )
        }
    }
}

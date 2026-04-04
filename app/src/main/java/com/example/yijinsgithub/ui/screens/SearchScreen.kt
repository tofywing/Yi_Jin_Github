package com.example.yijinsgithub.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.yijinsgithub.R
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.ui.components.RepoList
import com.example.yijinsgithub.ui.theme.Dimens
import com.example.yijinsgithub.ui.viewmodel.GithubUiState

/**
 * A screen that allows users to search for GitHub repositories by keywords and language.
 * It adapts its layout based on the screen orientation (Portrait vs Landscape).
 *
 * @param uiState The current UI state from the ViewModel.
 * @param repos The list of search result repositories to display.
 * @param onSearch Callback triggered when the search button is clicked with query and language.
 * @param onRepoClick Callback triggered when a repository item is clicked.
 * @param onDispose Callback to clean up resources when leaving the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: GithubUiState,
    repos: List<Repo>,
    onSearch: (String, String?) -> Unit,
    onRepoClick: (Repo) -> Unit,
    onDispose: () -> Unit = {}
) {
    // Cancel async work when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    var query by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                            onValueChange = { query = it },
                            label = { Text(stringResource(R.string.search_keywords_label)) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = language,
                            onValueChange = { language = it },
                            label = { Text(stringResource(R.string.search_language_label)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(R.string.search_keywords_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacerMedium))
                    OutlinedTextField(
                        value = language,
                        onValueChange = { language = it },
                        label = { Text(stringResource(R.string.search_language_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacerMedium))
                Button(
                    onClick = { onSearch(query, language.ifBlank { null }) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = query.isNotBlank()
                ) {
                    Text(stringResource(R.string.search_button))
                }
            }

            val currentUiState = uiState
            if (currentUiState is GithubUiState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (currentUiState is GithubUiState.Error) {
                Text(
                    text = currentUiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(Dimens.PaddingLarge)
                )
            }

            RepoList(
                repos = repos,
                onRepoClick = onRepoClick
            )
        }
    }
}

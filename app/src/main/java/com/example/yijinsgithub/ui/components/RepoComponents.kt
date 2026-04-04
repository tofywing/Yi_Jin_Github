package com.example.yijinsgithub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.yijinsgithub.R
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.ui.theme.Dimens
import kotlinx.coroutines.launch

/**
 * A reusable list component that displays a collection of GitHub repositories.
 * Includes pull-to-refresh functionality and a floating action button to scroll back to the top.
 *
 * @param repos The list of repository data models to display.
 * @param modifier Modifier to be applied to the list container.
 * @param isRefreshing Whether the list is currently in a refreshing state.
 * @param onRefresh Optional callback triggered when the user performs a pull-to-refresh gesture.
 * @param onRepoClick Callback triggered when a specific repository item is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoList(
    repos: List<Repo>,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    onRepoClick: (Repo) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onRefresh?.invoke() },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = repos, key = { it.id }) { repo ->
                RepoItem(repo = repo, onClick = { onRepoClick(repo) })
            }

            if (repos.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.reached_bottom),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.PaddingLarge),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Dimens.PaddingLarge)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(Dimens.AvatarSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top"
                )
            }
        }
    }
}

/**
 * A single repository item displayed as a Card.
 * Shows owner avatar, repository name, description, stars, forks, and primary language.
 *
 * @param repo The repository data model containing information to display.
 * @param onClick Callback triggered when the card is clicked.
 */
@Composable
fun RepoItem(repo: Repo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingMedium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.PaddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = repo.owner.avatarUrl,
                contentDescription = stringResource(id = R.string.repo_owner_avatar_desc),
                modifier = Modifier
                    .size(Dimens.AvatarSmall)
                    .clip(MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(Dimens.SpacerExtraLarge))
            Column {
                Text(
                    text = repo.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                repo.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = Dimens.MaxLinesRepoDescription
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.SpacerSmall))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐ ${repo.stars}", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(Dimens.SpacerLarge))
                    Text(text = "🍴 ${repo.forks}", style = MaterialTheme.typography.labelMedium)
                    repo.language?.let { language ->
                        Spacer(modifier = Modifier.width(Dimens.SpacerLarge))
                        Text(
                            text = language,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

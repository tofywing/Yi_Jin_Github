package com.example.yijinsgithub.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yijinsgithub.R
import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.ui.theme.Dimens
import com.example.yijinsgithub.ui.viewmodel.GithubUiState
import com.example.yijinsgithub.ui.viewmodel.UserState

/**
 * Screen that displays the user's GitHub profile information, statistics, and repositories.
 * It also allows the user to log out and create issues in their repositories.
 *
 * @param uiState The current state of the UI (loading, success, error, etc.).
 * @param userState The current authentication state and user data.
 * @param onLogout Callback triggered when the user clicks the logout button.
 * @param onCreateIssue Callback triggered to create a new issue in a specific repository.
 * @param onDispose Callback for cleaning up resources when the composable leaves the composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: GithubUiState,
    userState: UserState,
    onLogout: () -> Unit,
    onCreateIssue: (String, String, String, String) -> Unit,
    onDispose: () -> Unit = {}
) {
    val context = LocalContext.current
    val successMessage = stringResource(R.string.issue_created_success)
    var issueSubmitted by remember { mutableStateOf(false) }

    // Handle success feedback via Toast
    LaunchedEffect(uiState) {
        if (uiState is GithubUiState.Success && issueSubmitted) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            issueSubmitted = false
        }
    }

    // Cancel async work when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    var showIssueDialog by remember { mutableStateOf(false) }

    when (val currentUserState = userState) {
        is UserState.Authenticated -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.profile_title)) },
                        actions = {
                            IconButton(onClick = onLogout) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = stringResource(R.string.logout_content_description)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showIssueDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.create_issue_content_description)
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header section
                    Row(
                        modifier = Modifier.padding(Dimens.PaddingLarge),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentUserState.user.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(Dimens.AvatarLarge)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpacerExtraLarge))
                        Column {
                            Text(
                                currentUserState.user.name ?: currentUserState.user.login,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "@${currentUserState.user.login}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            currentUserState.user.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(bio, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Stats Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Dimens.PaddingLarge,
                                vertical = Dimens.PaddingSmall
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.PaddingMedium),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = stringResource(R.string.label_followers),
                                count = currentUserState.user.followers
                            )
                            StatItem(
                                label = stringResource(R.string.label_following),
                                count = currentUserState.user.following
                            )
                            StatItem(
                                label = stringResource(R.string.label_repos),
                                count = currentUserState.user.publicRepos
                            )
                        }
                    }

                    // Additional Info Section
                    Column(modifier = Modifier.padding(Dimens.PaddingLarge)) {
                        currentUserState.user.company?.takeIf { it.isNotBlank() }?.let { company ->
                            InfoRow(icon = Icons.Default.Person, text = company)
                        }
                        currentUserState.user.location?.takeIf { it.isNotBlank() }
                            ?.let { location ->
                                InfoRow(icon = Icons.Default.LocationOn, text = location)
                            }
                        currentUserState.user.blog?.takeIf { it.isNotBlank() }?.let { blog ->
                            InfoRow(
                                icon = Icons.Default.Email,
                                text = blog
                            ) // Using Email icon for blog as a placeholder
                        }
                    }
                }

                if (showIssueDialog) {
                    IssueDialog(
                        repos = currentUserState.repos,
                        onDismiss = { showIssueDialog = false },
                        onSubmit = { repo, title, body ->
                            issueSubmitted = true
                            onCreateIssue(currentUserState.user.login, repo, title, body)
                            showIssueDialog = false
                        }
                    )
                }
            }
        }

        is UserState.Anonymous -> {
            // Should not be here in ProfileScreen, but handled for completeness
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.auth_required_title))
            }
        }
    }
}

/**
 * A small UI component to display a single statistic (e.g., Followers, Following).
 *
 * @param label The name of the statistic.
 * @param count The numerical value of the statistic.
 */
@Composable
fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * A row component used to display an icon followed by a text string, typically for profile info.
 *
 * @param icon The icon to display.
 * @param text The text to display next to the icon.
 */
@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Dimens.SpacerMedium))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * A dialog that allows the user to select a repository and input details to create a new issue.
 *
 * @param repos The list of repositories the user can create an issue in.
 * @param onDismiss Callback when the dialog is dismissed or cancelled.
 * @param onSubmit Callback when the user clicks the submit button with the issue details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDialog(
    repos: List<Repo>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRepo by remember { mutableStateOf(repos.firstOrNull()?.name ?: "") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_issue_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.target_repository_label),
                    style = MaterialTheme.typography.labelLarge
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.PaddingSmall)
                ) {
                    OutlinedTextField(
                        value = selectedRepo,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        repos.forEach { repo ->
                            DropdownMenuItem(
                                text = { Text(repo.name) },
                                onClick = {
                                    selectedRepo = repo.name
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.issue_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.SpacerMedium))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(stringResource(R.string.issue_description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedRepo, title, body) },
                enabled = title.isNotBlank() && selectedRepo.isNotBlank()
            ) {
                Text(stringResource(R.string.submit_issue_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

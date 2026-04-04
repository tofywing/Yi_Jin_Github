package com.example.yijinsgithub.data.repository

import com.example.yijinsgithub.data.model.*
import com.example.yijinsgithub.data.remote.GithubService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository class that handles data operations for GitHub-related features.
 * It acts as a mediator between the [GithubService] (Remote Data Source) and the rest of the app.
 *
 * @property service The Retrofit service used to make network requests.
 */
class GithubRepository(
    private val service: GithubService
) {
    /**
     * Fetches a list of popular repositories on GitHub, defined by having more than 10,000 stars.
     *
     * @return A list of popular [Repo] objects.
     */
    suspend fun getPopularRepositories(): List<Repo> {
        return service.searchRepositories("stars:>10000").items
    }

    /**
     * Searches for repositories based on a query string and an optional programming language.
     *
     * @param query The search keywords.
     * @param language Optional language filter (e.g., "Kotlin", "Java").
     * @return A list of [Repo] objects matching the search criteria.
     */
    suspend fun searchRepositories(query: String, language: String? = null): List<Repo> {
        val q = if (language != null) "$query language:$language" else query
        return service.searchRepositories(q).items
    }

    /**
     * Retrieves the profile information for the user associated with the provided PAT.
     *
     * @param token The GitHub Personal Access Token.
     * @return A [User] object representing the authenticated user.
     */
    suspend fun getCurrentUser(token: String): User {
        return service.getCurrentUser("Bearer $token")
    }

    /**
     * Fetches the list of repositories owned by the authenticated user.
     *
     * @param token The GitHub Personal Access Token.
     * @return A list of [Repo] objects belonging to the user.
     */
    suspend fun getUserRepositories(token: String): List<Repo> {
        return service.getUserRepositories("Bearer $token")
    }

    /**
     * Creates a new issue in a specific GitHub repository.
     *
     * @param token The GitHub Personal Access Token.
     * @param owner The username or organization that owns the repository.
     * @param repo The name of the repository.
     * @param title The title of the new issue.
     * @param body The descriptive text of the new issue.
     * @return An [IssueResponse] confirming the issue creation.
     */
    suspend fun createIssue(
        token: String,
        owner: String,
        repo: String,
        title: String,
        body: String
    ): IssueResponse {
        return service.createIssue("Bearer $token", owner, repo, IssueRequest(title, body))
    }
}

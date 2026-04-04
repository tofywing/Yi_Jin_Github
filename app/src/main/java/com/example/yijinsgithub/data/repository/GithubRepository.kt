package com.example.yijinsgithub.data.repository

import com.example.yijinsgithub.common.Constants.DEFAULT_PAGE
import com.example.yijinsgithub.common.Constants.DEFAULT_PER_PAGE
import com.example.yijinsgithub.data.model.*
import com.example.yijinsgithub.data.remote.GithubService

/**
 * Repository class that handles data operations for GitHub-related features.
 */
class GithubRepository(
    private val service: GithubService
) {
    /**
     * Fetches popular repositories.
     */
    suspend fun getPopularRepositories(page: Int = DEFAULT_PAGE): List<Repo> {
        return service.searchRepositories("stars:>10000", page = page).items
    }

    /**
     * Searches for repositories with pagination support.
     */
    suspend fun searchRepositories(
        query: String, 
        language: String? = null, 
        page: Int = DEFAULT_PAGE,
        perPage: Int = DEFAULT_PER_PAGE
    ): List<Repo> {
        val q = if (language != null) "$query language:$language" else query
        return service.searchRepositories(q, page = page, perPage = perPage).items
    }

    suspend fun getCurrentUser(token: String): User {
        return service.getCurrentUser("Bearer $token")
    }

    /**
     * Fetches user repositories with pagination support.
     */
    suspend fun getUserRepositories(
        token: String, 
        page: Int = DEFAULT_PAGE,
        perPage: Int = DEFAULT_PER_PAGE
    ): List<Repo> {
        return service.getUserRepositories("Bearer $token", page = page, perPage = perPage)
    }

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

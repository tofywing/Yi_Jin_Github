package com.example.yijinsgithub.data.remote

import com.example.yijinsgithub.data.model.*
import retrofit2.http.*

/**
 * Retrofit service interface for interacting with the GitHub REST API.
 */
interface GithubService {
    /**
     * Searches for repositories using a text query.
     *
     * @param query The search keywords, optionally including qualifiers like `language:kotlin`.
     * @param sort The field to sort the results by (default is "stars").
     * @param order The sort order, "asc" or "desc" (default is "desc").
     * @return A [RepoSearchResponse] containing the list of matching repositories.
     */
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc"
    ): RepoSearchResponse

    /**
     * Fetches the profile information for the authenticated user.
     *
     * @param token The GitHub Personal Access Token in the format "Bearer <token>".
     * @return The [User] object for the authenticated user.
     */
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): User

    /**
     * Lists repositories for the authenticated user.
     *
     * @param token The GitHub Personal Access Token in the format "Bearer <token>".
     * @return A list of [Repo] objects owned by the user.
     */
    @GET("user/repos")
    suspend fun getUserRepositories(
        @Header("Authorization") token: String
    ): List<Repo>

    /**
     * Creates a new issue in a specified repository.
     *
     * @param token The GitHub Personal Access Token in the format "Bearer <token>".
     * @param owner The account owner of the repository (case-insensitive).
     * @param repo The name of the repository (case-insensitive).
     * @param issue The [IssueRequest] object containing the title and body of the issue.
     * @return An [IssueResponse] confirming the creation of the issue.
     */
    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body issue: IssueRequest
    ): IssueResponse

    /**
     * Fetches a list of public repositories on GitHub.
     * This is typically used to display a default list of popular repos.
     *
     * @return A list of public [Repo] objects.
     */
    @GET("repositories")
    suspend fun getPublicRepositories(): List<Repo>
}

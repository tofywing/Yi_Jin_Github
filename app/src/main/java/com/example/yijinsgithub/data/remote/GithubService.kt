package com.example.yijinsgithub.data.remote

import com.example.yijinsgithub.common.Constants.DEFAULT_ORDER
import com.example.yijinsgithub.common.Constants.DEFAULT_PAGE
import com.example.yijinsgithub.common.Constants.DEFAULT_PER_PAGE
import com.example.yijinsgithub.common.Constants.DEFAULT_SORT
import com.example.yijinsgithub.data.model.*
import retrofit2.http.*

/**
 * Retrofit service interface for interacting with the GitHub REST API.
 */
interface GithubService {
    /**
     * Searches for repositories using a text query.
     *
     * @param query The search keywords.
     * @param sort The field to sort the results by.
     * @param order The sort order.
     * @param perPage Number of results per page.
     * @param page Page number of the results to fetch.
     */
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = DEFAULT_SORT,
        @Query("order") order: String = DEFAULT_ORDER,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE,
        @Query("page") page: Int = DEFAULT_PAGE
    ): RepoSearchResponse

    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): User

    /**
     * Lists repositories for the authenticated user with pagination.
     */
    @GET("user/repos")
    suspend fun getUserRepositories(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE,
        @Query("page") page: Int = DEFAULT_PAGE
    ): List<Repo>

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body issue: IssueRequest
    ): IssueResponse

    /**
     * Fetches a list of public repositories on GitHub.
     */
    @GET("repositories")
    suspend fun getPublicRepositories(
        @Query("since") since: Int? = null
    ): List<Repo>
}

package com.example.yijinsgithub.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a GitHub repository.
 *
 * @property id Unique identifier for the repository.
 * @property name Name of the repository.
 * @property fullName Full name of the repository, including the owner (e.g., "owner/repo").
 * @property description A short description of the repository.
 * @property owner The user who owns the repository.
 * @property stars Number of stars (stargazers) the repository has received.
 * @property forks Number of times the repository has been forked.
 * @property language The primary programming language used in the repository.
 * @property htmlUrl The web URL of the repository on GitHub.
 */
@Serializable
data class Repo(
    val id: Long,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    val owner: User,
    @SerialName("stargazers_count") val stars: Int,
    @SerialName("forks_count") val forks: Int,
    val language: String? = null,
    @SerialName("html_url") val htmlUrl: String
)

/**
 * Data class representing a GitHub user or organization.
 *
 * @property login The user's handle or login name.
 * @property id Unique identifier for the user.
 * @property avatarUrl URL to the user's profile image.
 * @property htmlUrl The web URL of the user's profile on GitHub.
 * @property name The display name of the user.
 * @property bio The user's biography text.
 * @property publicRepos Number of public repositories owned by the user.
 * @property followers Number of users following this user.
 * @property following Number of users this user is following.
 * @property location The user's geographical location.
 * @property company The user's organization or company.
 * @property blog The user's personal website or blog URL.
 */
@Serializable
data class User(
    val login: String,
    val id: Long,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("html_url") val htmlUrl: String,
    val name: String? = null,
    val bio: String? = null,
    @SerialName("public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val location: String? = null,
    val company: String? = null,
    val blog: String? = null
)

/**
 * Data class representing the response from a repository search.
 *
 * @property totalCount Total number of repositories matching the search query.
 * @property items The list of repositories found in the current page of results.
 */
@Serializable
data class RepoSearchResponse(
    @SerialName("total_count") val totalCount: Int,
    val items: List<Repo>
)

/**
 * Data class representing the request body for creating a new issue.
 *
 * @property title The title of the issue.
 * @property body The descriptive text of the issue.
 */
@Serializable
data class IssueRequest(
    val title: String,
    val body: String
)

/**
 * Data class representing the response received after creating an issue.
 *
 * @property id Unique identifier for the created issue.
 * @property number The issue number within the repository.
 * @property title The title of the created issue.
 * @property body The body text of the created issue.
 */
@Serializable
data class IssueResponse(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String? = null
)

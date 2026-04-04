package com.example.yijinsgithub

import com.example.yijinsgithub.data.model.*
import com.example.yijinsgithub.data.remote.GithubService
import com.example.yijinsgithub.data.repository.GithubRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@OptIn(ExperimentalCoroutinesApi::class)
class GithubRepositoryTest {

    private lateinit var repository: GithubRepository
    private val service = mock(GithubService::class.java)

    private val mockUser = User(
        login = "testuser",
        id = 1L,
        avatarUrl = "avatar",
        htmlUrl = "html",
        name = "Test User"
    )

    private val mockRepo = Repo(
        id = 1L,
        name = "repo",
        fullName = "owner/repo",
        description = null,
        owner = mockUser,
        stars = 20000,
        forks = 100,
        language = "Kotlin",
        htmlUrl = "url"
    )

    @Before
    fun setup() {
        repository = GithubRepository(service)
    }

    @Test
    fun `test getPopularRepositories calls service search`() = runTest {
        val response = RepoSearchResponse(1, listOf(mockRepo))
        `when`(service.searchRepositories("stars:>10000")).thenReturn(response)
        
        val result = repository.getPopularRepositories()
        
        assertEquals(1, result.size)
        assertEquals("repo", result[0].name)
    }

    @Test
    fun `test searchRepositories with language`() = runTest {
        val response = RepoSearchResponse(1, listOf(mockRepo))
        `when`(service.searchRepositories("kotlin language:Kotlin")).thenReturn(response)
        
        val result = repository.searchRepositories("kotlin", "Kotlin")
        
        assertEquals(1, result.size)
    }

    @Test
    fun `test getCurrentUser calls service with Bearer token`() = runTest {
        val token = "my_token"
        `when`(service.getCurrentUser("Bearer $token")).thenReturn(mockUser)
        
        val result = repository.getCurrentUser(token)
        
        assertEquals(mockUser, result)
    }

    @Test
    fun `test getUserRepositories calls service with Bearer token`() = runTest {
        val token = "my_token"
        val repos = listOf(mockRepo)
        `when`(service.getUserRepositories("Bearer $token")).thenReturn(repos)
        
        val result = repository.getUserRepositories(token)
        
        assertEquals(repos, result)
    }

    @Test
    fun `test createIssue calls service with Bearer token and request body`() = runTest {
        val token = "my_token"
        val owner = "owner"
        val repo = "repo"
        val title = "title"
        val body = "body"
        val issueResponse = IssueResponse(1L, 1, title, body)
        
        `when`(service.createIssue(eq("Bearer $token"), eq(owner), eq(repo), any()))
            .thenReturn(issueResponse)
        
        val result = repository.createIssue(token, owner, repo, title, body)
        
        assertEquals(issueResponse, result)
    }
}

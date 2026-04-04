package com.example.yijinsgithub

import com.example.yijinsgithub.data.model.Repo
import com.example.yijinsgithub.data.model.RepoSearchResponse
import com.example.yijinsgithub.data.model.User
import com.example.yijinsgithub.data.remote.GithubService
import com.example.yijinsgithub.data.repository.GithubRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class GithubRepositoryTest {

    private lateinit var repository: GithubRepository
    private val service = mock(GithubService::class.java)

    @Before
    fun setup() {
        repository = GithubRepository(service)
    }

    @Test
    fun `test getPopularRepositories calls service search`() = runTest {
        val user = User("owner", 1L, "", "")
        val repo = Repo(1L, "repo", "owner/repo", null, user, 20000, 100, "Kotlin", "url")
        val response = RepoSearchResponse(1, listOf(repo))
        
        `when`(service.searchRepositories("stars:>10000")).thenReturn(response)
        
        val result = repository.getPopularRepositories()
        
        assertEquals(1, result.size)
        assertEquals("repo", result[0].name)
    }

    @Test
    fun `test searchRepositories with language`() = runTest {
        val user = User("owner", 1L, "", "")
        val repo = Repo(1L, "repo", "owner/repo", null, user, 100, 10, "Kotlin", "url")
        val response = RepoSearchResponse(1, listOf(repo))
        
        `when`(service.searchRepositories("kotlin language:Kotlin")).thenReturn(response)
        
        val result = repository.searchRepositories("kotlin", "Kotlin")
        
        assertEquals(1, result.size)
    }
}

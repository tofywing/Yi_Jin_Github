package com.example.yijinsgithub.data.remote

import com.example.yijinsgithub.common.Constants
import com.example.yijinsgithub.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds the GitHub Personal Access Token to the Authorization header.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip adding token if the header is already present (manually set in Service)
        if (originalRequest.header(Constants.AUTH_HEADER_KEY) != null) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            tokenManager.token.firstOrNull()
        }

        return if (!token.isNullOrBlank()) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header(Constants.AUTH_HEADER_KEY, "${Constants.AUTH_TOKEN_PREFIX}$token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}

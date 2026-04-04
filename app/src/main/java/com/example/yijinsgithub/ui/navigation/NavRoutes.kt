package com.example.yijinsgithub.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Represents the different screens and navigation routes in the application.
 *
 * @property route The string representation of the navigation route.
 */
sealed class Screen(val route: String) {
    /**
     * The home screen displaying popular or user repositories.
     */
    object Home : Screen("home")

    /**
     * The search screen for finding repositories by keyword and language.
     */
    object Search : Screen("search")

    /**
     * The user's profile screen.
     */
    object Profile : Screen("profile")

    /**
     * The repository detail screen which takes an encoded URL as an argument.
     */
    object RepoDetail : Screen("repo_detail/{url}") {
        /**
         * Creates a navigation route for the RepoDetail screen with an encoded URL.
         *
         * @param url The raw repository URL to be encoded and passed as an argument.
         * @return The formatted route string.
         */
        fun createRoute(url: String): String {
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            return "repo_detail/$encodedUrl"
        }
    }
}

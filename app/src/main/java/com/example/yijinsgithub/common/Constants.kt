package com.example.yijinsgithub.common

object Constants {
    const val GITHUB_BASE_URL = "https://api.github.com/"
    const val MEDIA_TYPE_JSON = "application/json"

    // DataStore & Security
    const val DATASTORE_NAME = "settings"
    const val TOKEN_KEY_NAME = "github_token_encrypted"
    const val KEY_STORE_PROVIDER = "AndroidKeyStore"
    const val KEY_ALIAS = "GithubAppKey"
    const val CRYPTO_TRANSFORMATION = "AES/GCM/NoPadding"

    // WebView
    const val WEBVIEW_USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
    const val AUTH_HEADER_KEY = "Authorization"
    const val AUTH_TOKEN_PREFIX = "token "
    const val BLANK_PAGE = "about:blank"

    // Pagination
    const val DEFAULT_PER_PAGE = 20
    const val DEFAULT_PAGE = 1
    const val DEFAULT_SORT = "stars"
    const val DEFAULT_ORDER = "desc"
    const val DEFAULT_USER_REPO_SORT = "pushed"
}

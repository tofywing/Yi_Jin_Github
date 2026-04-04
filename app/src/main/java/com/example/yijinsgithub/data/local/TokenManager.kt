package com.example.yijinsgithub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.yijinsgithub.common.Constants
import com.example.yijinsgithub.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Manages the storage and retrieval of the GitHub Personal Access Token (PAT).
 * It uses Jetpack DataStore for persistence and [CryptoManager] to encrypt/decrypt the token.
 *
 * @property context The application context used to access DataStore.
 */
class TokenManager(private val context: Context) {

    private val cryptoManager = CryptoManager()

    companion object {
        /**
         * The key used to store the encrypted token in DataStore.
         */
        private val TOKEN_KEY = stringPreferencesKey(Constants.TOKEN_KEY_NAME)
    }

    /**
     * A flow that emits the decrypted token whenever it changes in the DataStore.
     * Returns null if no token is stored or if decryption fails.
     */
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]?.let { cryptoManager.decrypt(it) }
    }

    /**
     * Encrypts and saves the provided token to the DataStore.
     *
     * @param token The raw GitHub Personal Access Token to save.
     */
    suspend fun saveToken(token: String) {
        val encrypted = cryptoManager.encrypt(token)
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = encrypted
        }
    }

    /**
     * Removes the token from the DataStore, effectively logging the user out.
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}

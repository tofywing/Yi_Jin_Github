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
 * SECURITY NOTE: To prevent memory leaks of sensitive data via the String constant pool,
 * tokens should ideally be handled as CharArray. However, current DataStore and OkHttp 
 * implementations primarily use Strings. We mitigate this by using AndroidX Security for 
 * at-rest encryption and masking in the UI.
 */
class TokenManager(private val context: Context) {

    private val cryptoManager = CryptoManager()

    companion object {
        private val TOKEN_KEY = stringPreferencesKey(Constants.TOKEN_KEY_NAME)
    }

    /**
     * A flow that emits the decrypted token whenever it changes in the DataStore.
     */
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]?.let { 
            val decrypted = cryptoManager.decrypt(it)
            decrypted
        }
    }

    /**
     * Encrypts and saves the provided token to the DataStore.
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

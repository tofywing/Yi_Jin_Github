package com.example.yijinsgithub.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.yijinsgithub.common.Constants.CRYPTO_TRANSFORMATION
import com.example.yijinsgithub.common.Constants.KEY_ALIAS
import com.example.yijinsgithub.common.Constants.KEY_STORE_PROVIDER
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * A manager class responsible for encrypting and decrypting sensitive data using the Android KeyStore system.
 * It uses the AES algorithm in GCM mode for secure encryption, ensuring data integrity and confidentiality.
 */
class CryptoManager {

    /**
     * Retrieves the existing secret key from the Android KeyStore or creates a new one if it doesn't exist.
     * The key is configured for AES/GCM encryption with no padding.
     *
     * @return The [SecretKey] used for encryption and decryption.
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER).apply { load(null) }
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        // If key doesn't exist, generate a new one in the Android KeyStore
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts the [data] string and returns a Base64 encoded string containing the IV and ciphertext.
     * The IV (Initialization Vector) is prepended to the ciphertext.
     *
     * @param data The raw string to be encrypted.
     * @return A Base64 encoded string representing the encrypted data.
     */
    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(CRYPTO_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        // IV must be saved along with the ciphertext for decryption
        val combined = cipher.iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts the [encryptedData] Base64 string and returns the original plain text.
     * It extracts the IV from the first 12 bytes of the decoded data.
     *
     * @param encryptedData The Base64 encoded string to be decrypted.
     * @return The original plain text string.
     */
    fun decrypt(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
        val iv = combined.sliceArray(0 until 12) // GCM default IV length is 12 bytes
        val cipherText = combined.sliceArray(12 until combined.size)

        val cipher = Cipher.getInstance(CRYPTO_TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
        return String(cipher.doFinal(cipherText))
    }
}

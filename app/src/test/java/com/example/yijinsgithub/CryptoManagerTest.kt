package com.example.yijinsgithub

import com.example.yijinsgithub.security.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setup() {
        // Use a dummy provider name to trigger the test-friendly key generation
        cryptoManager = CryptoManager(keyStoreProvider = "TestProvider")
    }

    @Test
    fun `test encryption and decryption symmetry`() {
        val originalData = "ghp_very_secret_token_12345"
        
        val encrypted = cryptoManager.encrypt(originalData)
        assertNotEquals(originalData, encrypted)
        
        val decrypted = cryptoManager.decrypt(encrypted)
        assertEquals(originalData, decrypted)
    }

    @Test
    fun `test encrypt returns different results for same input due to random IV`() {
        val data = "same_data"
        val encrypted1 = cryptoManager.encrypt(data)
        val encrypted2 = cryptoManager.encrypt(data)
        
        // GCM should use a different IV for each encryption
        assertNotEquals(encrypted1, encrypted2)
        
        assertEquals(data, cryptoManager.decrypt(encrypted1))
        assertEquals(data, cryptoManager.decrypt(encrypted2))
    }
}

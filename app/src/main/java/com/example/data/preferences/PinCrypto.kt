package com.example.data.preferences

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinCrypto {
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH_BITS = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    data class HashedPin(
        val hashBase64: String,
        val saltBase64: String
    )

    fun createHash(pin: String): HashedPin {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(pin, salt)
        return HashedPin(
            hashBase64 = Base64.getEncoder().encodeToString(hash),
            saltBase64 = Base64.getEncoder().encodeToString(salt)
        )
    }

    fun verifyPin(pin: String, expectedHashBase64: String, saltBase64: String): Boolean {
        if (pin.isBlank() || expectedHashBase64.isBlank() || saltBase64.isBlank()) return false
        return try {
            val salt = Base64.getDecoder().decode(saltBase64)
            val expectedHash = Base64.getDecoder().decode(expectedHashBase64)
            val candidateHash = pbkdf2(pin, salt)
            MessageDigest.isEqual(candidateHash, expectedHash)
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun pbkdf2(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

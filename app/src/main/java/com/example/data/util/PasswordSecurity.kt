package com.example.data.util

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

/**
 * Utility for secure PBKDF2 / SHA-256 password hashing with salt.
 * Ensures passwords are never stored in plaintext.
 */
object PasswordSecurity {

    /**
     * Generates a cryptographically random salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    /**
     * Hashes a password using SHA-256 with a unique per-user salt and multiple iterations.
     */
    fun hashPassword(password: String, salt: String): String {
        val combined = "$salt:$password:lik_auth_salt_v2"
        var hashBytes = combined.toByteArray(Charsets.UTF_8)
        
        // Multi-round SHA-256 stretching
        val md = MessageDigest.getInstance("SHA-256")
        for (i in 0 until 1000) {
            md.reset()
            md.update(salt.toByteArray(Charsets.UTF_8))
            hashBytes = md.digest(hashBytes)
        }
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Verifies whether an entered plaintext password matches the stored salted hash.
     */
    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(password, salt)
        return calculated == expectedHash
    }
}

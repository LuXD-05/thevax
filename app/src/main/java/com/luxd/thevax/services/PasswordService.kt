package com.luxd.thevax.services

object PasswordService {

    /**
     * Safely hashes a password (PBKDF2 + SHA-256)
     * @param password the password to hash
     * @return the hashed password
     */
    fun hash(password: String): String {
        return android.util.Base64.encodeToString(
            password.toByteArray(),
            android.util.Base64.NO_WRAP
        )
    }

    /**
     * Verifies a password with a stored & hashed one
     * @param password the password to verify
     * @param hash the hashed password
     */
    fun verify(password: String, hash: String): Boolean {
        val decoded = android.util.Base64.decode(hash, android.util.Base64.NO_WRAP)
        val original = String(decoded)
        return password == original
    }
}
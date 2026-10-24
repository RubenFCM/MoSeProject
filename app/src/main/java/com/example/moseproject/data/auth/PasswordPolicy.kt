package com.example.moseproject.data.auth

/** Public client-side guidance. Firebase's configured server policy remains authoritative. */
data class PasswordPolicy(
    val minLength: Int = 6,
    val maxLength: Int = 4096,
    val lowercase: Boolean = false,
    val uppercase: Boolean = false,
    val number: Boolean = false,
    val symbol: Boolean = false
) {
    init { require(minLength in 6..30 && maxLength in minLength..4096) }

    fun accepts(password: String): Boolean = password.length in minLength..maxLength &&
        (!lowercase || password.any { it in 'a'..'z' }) &&
        (!uppercase || password.any { it in 'A'..'Z' }) &&
        (!number || password.any { it in '0'..'9' }) &&
        (!symbol || password.any { it in SYMBOLS })

    private companion object {
        const val SYMBOLS = "^\$*.[]{}()?\"!@#%&/\\,><':;|_~"
    }
}

object EmailValidation {
    // Reject malformed addresses locally without querying whether an account exists.
    private val pattern = Regex("^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)+\$")
    fun accepts(email: String): Boolean = email.length <= 254 && email.substringBefore('@').length <= 64 && pattern.matches(email)
}

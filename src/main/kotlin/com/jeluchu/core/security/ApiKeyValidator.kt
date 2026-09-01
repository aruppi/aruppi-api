package com.jeluchu.core.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class ApiKeyValidator(expectedKey: String) {
    private val expectedKeyBytes = expectedKey.trim().toByteArray(StandardCharsets.UTF_8)

    val isConfigured: Boolean
        get() = expectedKeyBytes.isNotEmpty()

    fun isValid(providedKey: String?): Boolean {
        if (!isConfigured) return false

        val providedKeyBytes = providedKey
            ?.trim()
            ?.toByteArray(StandardCharsets.UTF_8)
            ?: return false

        return MessageDigest.isEqual(expectedKeyBytes, providedKeyBytes)
    }

    companion object {
        const val HEADER_NAME = "x-api-key"
        const val ENVIRONMENT_VARIABLE = "ARUPPI_API_KEY"

        fun fromEnvironment(): ApiKeyValidator =
            ApiKeyValidator(System.getenv(ENVIRONMENT_VARIABLE).orEmpty())
    }
}

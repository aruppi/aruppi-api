package com.jeluchu.core.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiKeyValidatorTest {
    @Test
    fun `accepts the configured key`() {
        val validator = ApiKeyValidator("test-key")

        assertTrue(validator.isConfigured)
        assertTrue(validator.isValid("test-key"))
    }

    @Test
    fun `rejects missing blank and different keys`() {
        val validator = ApiKeyValidator("test-key")

        assertFalse(validator.isValid(null))
        assertFalse(validator.isValid(""))
        assertFalse(validator.isValid("wrong-key"))
    }

    @Test
    fun `an unconfigured validator fails closed`() {
        val validator = ApiKeyValidator("")

        assertFalse(validator.isConfigured)
        assertFalse(validator.isValid("test-key"))
    }
}

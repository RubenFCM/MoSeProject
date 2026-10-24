package com.example.moseproject.auth

import com.example.moseproject.data.auth.EmailValidation
import com.example.moseproject.data.auth.PasswordPolicy
import org.junit.Test
import kotlin.test.*

class PasswordPolicyTest {
    @Test fun `default policy mirrors Firebase defaults and never trims spaces`() {
        val policy = PasswordPolicy()
        assertTrue(policy.accepts(" aaaa "))
        assertFalse(policy.accepts("short"))
        assertTrue(policy.accepts("a".repeat(4096)))
        assertFalse(policy.accepts("a".repeat(4097)))
    }
    @Test fun `configured policy enforces every requested requirement and exact symbol set`() {
        val policy = PasswordPolicy(8, 20, lowercase = true, uppercase = true, number = true, symbol = true)
        assertTrue(policy.accepts(" Good123! "))
        for (invalid in listOf("lower123!", "UPPER123!", "GoodNoNum!", "Good1234", "Aa1!", "Aa123456+", "Aa123456-")) {
            assertFalse(policy.accepts(invalid))
        }
        assertFailsWith<IllegalArgumentException> { PasswordPolicy(10, 8) }
    }
    @Test fun `email validation catches malformed addresses without an account lookup`() {
        assertTrue(EmailValidation.accepts("viewer+films@example.test"))
        for (invalid in listOf("", "viewer", "viewer@", "viewer@example", "viewer name@example.test", "viewer@@example.test", " viewer@example.test ")) {
            assertFalse(EmailValidation.accepts(invalid))
        }
    }
}

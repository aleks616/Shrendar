package com.example.client.register

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegisterValidatorTest {
    private val validator=RegisterValidator()

    @Test
    fun rejectsLoginsShorterThanFiveCharacters()=runTest {
        assertEquals("login_too_short",validator.validateLogin("1234"))
    }

    @Test
    fun rejectsLoginsLongerThanTwentyFiveCharacters()=runTest {
        assertEquals("login_too_long",validator.validateLogin("a".repeat(26)))
    }

    @Test
    fun rejectsAnEmptyEmailAddress()=runTest {
        assertEquals("email_invalid",validator.validateEmail(""))
    }

    @Test
    fun rejectsAnEmailAddressWithoutAnAtSign()=runTest {
        assertEquals("email_invalid",validator.validateEmail("user"))
    }

    @Test
    fun rejectsAnEmailAddressWithoutALocalPart()=runTest {
        assertEquals("email_invalid",validator.validateEmail("@example.com"))
    }

    @Test
    fun rejectsAnEmailAddressWithoutADomain()=runTest {
        assertEquals("email_invalid",validator.validateEmail("user@"))
    }

    @Test
    fun rejectsAnEmailAddressWithoutADomainSuffix()=runTest {
        assertEquals("email_invalid",validator.validateEmail("user@example"))
    }

    @Test
    fun rejectsAnEmailAddressWithAnEmptyDomainLabel()=runTest {
        assertEquals("email_invalid",validator.validateEmail("user@example..com"))
    }

    @Test
    fun acceptsAnEightCharacterPassword() {
        assertTrue(validator.isPasswordValid("Aa1!aaaa"))
    }

    @Test
    fun acceptsAThirtyTwoCharacterPassword() {
        assertTrue(validator.isPasswordValid("Aa1!"+"a".repeat(28)))
    }

    @Test
    fun rejectsPasswordWithoutLowercase() {
        assertFalse(validator.isPasswordValid("aa1!aaaa"))
    }

    @Test
    fun rejectsPasswordWithoutUppercase() {
        assertFalse(validator.isPasswordValid("AA1!AAAA"))
    }

    @Test
    fun rejectsPasswordWithoutDigit() {
        assertFalse(validator.isPasswordValid("Aa!aaaaa"))
    }

    @Test
    fun rejectsPasswordWithoutSymbol() {
        assertFalse(validator.isPasswordValid("Aa1aaaaa"))
    }

    @Test
    fun rejectsASevenCharacterPassword() {
        assertFalse(validator.isPasswordValid("Aa1!aaa"))
    }

    @Test
    fun rejectsAThirtyThreeCharacterPassword() {
        assertFalse(validator.isPasswordValid("Aa1!"+"a".repeat(29)))
    }

    @Test
    fun acceptsAValidPassword() {
        assertTrue(validator.isPasswordValid("Correct1!"))
    }
}

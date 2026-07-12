package org.aleks616.shrendar

import org.aleks616.shrendar.PasswordResetCodeGenerator.generatePasswordResetCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class PasswordResetCodeGeneratorTest {

    @Test
    fun generateVerificationCodeTest() {
        val code=generatePasswordResetCode()
        assertNotNull(code)
        assertEquals(7,code.length)
    }
}
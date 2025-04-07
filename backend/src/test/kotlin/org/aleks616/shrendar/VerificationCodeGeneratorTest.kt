package org.aleks616.shrendar

import org.aleks616.shrendar.VerificationCodeGenerator.generateVerificationCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class VerificationCodeGeneratorTest {

    @Test
    fun generateVerificationCodeTest() {
        val code=generateVerificationCode()
        assertNotNull(code)
        assertEquals(6,code.length)
    }
}
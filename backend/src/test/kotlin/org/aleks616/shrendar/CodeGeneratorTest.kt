package org.aleks616.shrendar

import org.aleks616.shrendar.securityCode.CodeGenerator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CodeGeneratorTest {
    @Test
    fun testGenerateCodeLength() {
        val code = CodeGenerator.generateCode(8)
        assertEquals(8, code.length)
    }

    @Test
    fun testGenerateNumericCode() {
        val code = CodeGenerator.generateCode(10, numericOnly = true)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun testGenerateAlphanumericCode() {
        val code = CodeGenerator.generateCode(100, numericOnly = false)
        assertTrue(code.any { it.isLetter() })
    }
}

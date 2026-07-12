package org.aleks616.shrendar

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
object PasswordResetCodeGenerator {
    private const val CHARACTERS="0123456789"
    private const val CODE_LENGTH=6
    private val random=SecureRandom()

    fun generatePasswordResetCode():String {
        val code=StringBuilder(CODE_LENGTH)
        for(i in 0 until CODE_LENGTH) {
            code.append(CHARACTERS[random.nextInt(CHARACTERS.length)])
        }
        return code.toString()
    }
}
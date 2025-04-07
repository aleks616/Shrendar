package org.aleks616.shrendar

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
object VerificationCodeGenerator{
    private const val CHARACTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private const val CODE_LENGTH=6
    private val random=SecureRandom()

    fun generateVerificationCode():String{
        val code=StringBuilder(CODE_LENGTH)
        for(i in 0 ..<CODE_LENGTH){
            code.append(CHARACTERS[random.nextInt(CHARACTERS.length)])
        }
        return code.toString()
    }
}
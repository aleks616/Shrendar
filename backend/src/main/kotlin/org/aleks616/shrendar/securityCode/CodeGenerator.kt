package org.aleks616.shrendar.securityCode

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
object CodeGenerator {
    private const val ALPHANUMERIC="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private const val NUMERIC="0123456789"
    private val random=SecureRandom()

    fun generateCode(length:Int=6,numericOnly:Boolean=false):String {
        val characters=if(numericOnly) NUMERIC else ALPHANUMERIC
        val code=StringBuilder(length)
        for(i in 0 until length) {
            code.append(characters[random.nextInt(characters.length)])
        }
        return code.toString()
    }
}
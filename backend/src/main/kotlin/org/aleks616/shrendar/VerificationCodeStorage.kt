package org.aleks616.shrendar

import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class VerificationCodeStorage{
    private val codes=mutableMapOf<String,String>()
    private val codeExpiry=mutableMapOf<String,Instant>()
    private val lastSentTime=mutableMapOf<String,Instant>()

    fun storeCode(email:String,code:String) {
        codes[email]=code
        codeExpiry[email]=Instant.now().plus(15,ChronoUnit.MINUTES)
        lastSentTime[email]=Instant.now()
    }

    fun validateCode(email:String,code:String):Boolean {
        return codes[email]==code&&Instant.now().isBefore(codeExpiry[email] ?: Instant.MIN)
    }

    fun canSendCode(email:String):Boolean {
        return lastSentTime[email]?.let {Instant.now().isAfter(it.plus(1,ChronoUnit.MINUTES))} ?: true
    }
}
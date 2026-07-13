package org.aleks616.shrendar.securityCode

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

class CodeStorage(
    private val expiryMinutes:Long=15,
    private val resendLimitMinutes:Long=1
) {
    private val codes=ConcurrentHashMap<String,String>()
    private val codeExpiry=ConcurrentHashMap<String,Instant>()
    private val lastSentTime=ConcurrentHashMap<String,Instant>()

    fun storeCode(key:String,code:String) {
        codes[key]=code
        codeExpiry[key]=Instant.now().plus(expiryMinutes,ChronoUnit.MINUTES)
        lastSentTime[key]=Instant.now()
    }

    fun validateCode(key:String,code:String):Boolean {
        val storedCode=codes[key]
        val expiry=codeExpiry[key]
        val isValid=storedCode==code&&expiry!=null&&Instant.now().isBefore(expiry)
        if(isValid) {
            codes.remove(key)
            codeExpiry.remove(key)
        }
        return isValid
    }

    fun canSendCode(key:String):Boolean {
        val lastSent=lastSentTime[key]?:return true
        return Instant.now().isAfter(lastSent.plus(resendLimitMinutes,ChronoUnit.MINUTES))
    }
}
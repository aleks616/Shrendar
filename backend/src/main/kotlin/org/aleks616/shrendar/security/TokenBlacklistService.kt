package org.aleks616.shrendar.security

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

@Service
class TokenBlacklistService {
    private val blacklistedTokens=ConcurrentHashMap<String,Long>()

    fun blacklistToken(token:String,expiryEpochSecond:Long) {
        blacklistedTokens[token]=expiryEpochSecond
    }

    fun isBlacklisted(token:String):Boolean {
        val expiry=blacklistedTokens[token]?:return false
        if(Instant.now().epochSecond>expiry) {
            blacklistedTokens.remove(token)
            return false
        }
        return true
    }

    fun cleanup() {
        val now=Instant.now().epochSecond
        blacklistedTokens.entries.removeIf {it.value<now}
    }
}

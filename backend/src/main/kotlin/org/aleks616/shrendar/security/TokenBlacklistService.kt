package org.aleks616.shrendar.security

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

@Service
class TokenBlacklistService {
    private val blacklistedTokens=ConcurrentHashMap<String,Long>()

    fun blacklistToken(token:String) {
        blacklistedTokens[token]=Instant.now().epochSecond
    }

    fun isBlacklisted(token:String):Boolean {
        return blacklistedTokens.containsKey(token)
    }

    /*fun cleanup() {
        val now=Instant.now().epochSecond
        blacklistedTokens.entries.removeIf {it.value<now}
    }*/
}

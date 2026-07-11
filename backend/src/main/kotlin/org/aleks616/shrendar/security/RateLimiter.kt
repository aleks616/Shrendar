package org.aleks616.shrendar.security

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimiter {
    private val storage=ConcurrentHashMap<String,MutableList<Long>>()

    fun allowRequest(key:String,maxRequests:Int,windowSeconds:Int):Boolean {
        val now=Instant.now().epochSecond
        val windowStart=now-windowSeconds
        val list=storage.computeIfAbsent(key) {mutableListOf()}
        synchronized(list) {
            val it=list.iterator()
            while(it.hasNext()) {
                if(it.next()<windowStart) it.remove()
            }
            return if(list.size<maxRequests) {
                list.add(now)
                true
            }
            else
                false

        }
    }
}
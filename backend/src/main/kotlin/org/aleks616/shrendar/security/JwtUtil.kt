package org.aleks616.shrendar.security

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant

object JwtUtil {
    private const val SECRET="shrendar-dev-secret-change-me"
    private val mapper=jacksonObjectMapper()

    private fun hmacSha256(data:ByteArray,secret:ByteArray):ByteArray {
        val mac=Mac.getInstance("HmacSHA256")
        val keySpec=SecretKeySpec(secret,"HmacSHA256")
        mac.init(keySpec)
        return mac.doFinal(data)
    }

    fun validateToken(token:String):String? {
        try {
            val parts=token.split('.')
            if(parts.size!=3) return null
            val headerB=parts[0]
            val payloadB=parts[1]
            val sigB=parts[2]
            val signingInput=(headerB+"."+payloadB).toByteArray(UTF_8)
            val expectedSig=hmacSha256(signingInput,SECRET.toByteArray(UTF_8))
            val expectedSigB=Base64.getUrlEncoder().withoutPadding().encodeToString(expectedSig)
            if(!expectedSigB.equals(sigB)) return null
            val payloadJson=String(Base64.getUrlDecoder().decode(payloadB),StandardCharsets.UTF_8)
            val payload=mapper.readValue(payloadJson,Map::class.java)
            val exp=(payload["exp"] as Number).toLong()
            val sub=payload["sub"] as String
            if(Instant.now().epochSecond>exp) return null
            return sub
        }
        catch(e:Exception) {
            return null
        }
    }

    fun getExpiration(token:String):Long? {
        try {
            val parts=token.split('.')
            if(parts.size!=3) return null
            val payloadB=parts[1]
            val payloadJson=String(Base64.getUrlDecoder().decode(payloadB),StandardCharsets.UTF_8)
            val payload=mapper.readValue(payloadJson,Map::class.java)
            return (payload["exp"] as Number).toLong()
        }
        catch(e:Exception) {
            return null
        }
    }

    fun createToken(subject:String,validitySeconds:Long=3600L*24*60):String {
        val header=mapper.writeValueAsString(mapOf("alg" to "HS256","typ" to "JWT"))
        val exp=Instant.now().epochSecond+validitySeconds
        val payload=mapper.writeValueAsString(mapOf("sub" to subject,"exp" to exp))
        val headerB=Base64.getUrlEncoder().withoutPadding().encodeToString(header.toByteArray(UTF_8))
        val payloadB=Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray(UTF_8))
        val signingInput=(headerB+"."+payloadB).toByteArray(UTF_8)
        val sig=hmacSha256(signingInput,SECRET.toByteArray(UTF_8))
        val sigB=Base64.getUrlEncoder().withoutPadding().encodeToString(sig)
        return "$headerB.$payloadB.$sigB"
    }
}

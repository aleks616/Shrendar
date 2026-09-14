package org.aleks616.shrendar.security

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class JwtUtilTest {
    @Test
    fun `validateToken returns null when a signed payload cannot be parsed`() {
        assertNull(JwtUtil.validateToken(signedToken("not-json")))
    }

    private fun signedToken(payload:String):String {
        val header=Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray(StandardCharsets.UTF_8))
        val encodedPayload=Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        val signingInput="$header.$encodedPayload"
        val mac=Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec("shrendar-dev-secret-change-me".toByteArray(StandardCharsets.UTF_8),"HmacSHA256"))
        val signature=Base64.getUrlEncoder().withoutPadding()
            .encodeToString(mac.doFinal(signingInput.toByteArray(StandardCharsets.UTF_8)))
        return "$signingInput.$signature"
    }
}

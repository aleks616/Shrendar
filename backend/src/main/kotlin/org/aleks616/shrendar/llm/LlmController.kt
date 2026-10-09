package org.aleks616.shrendar.llm

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.security.RateLimiter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai")
class LlmController(
    private val service:LlmService,
    private val rateLimiter:RateLimiter,
) {

    @PostMapping("/translate")
    fun translate(@RequestBody request:TranslationRequestDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        return ResponseEntity.ok(service.translate(request))
    }
}

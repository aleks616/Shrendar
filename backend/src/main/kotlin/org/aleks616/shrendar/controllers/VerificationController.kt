package org.aleks616.shrendar.controllers

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.VerificationCodeGenerator
import org.aleks616.shrendar.VerificationCodeStorage
import org.aleks616.shrendar.repositories.UserRepository
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.services.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class VerificationController(
    @Autowired private val emailService:EmailService,
    private val verificationCodeGenerator:VerificationCodeGenerator,
    private val verificationCodeStorage:VerificationCodeStorage,
    private val userRepository:UserRepository,
    private val rateLimiter:RateLimiter
) {

    @PostMapping("/send-code")
    fun sendVerificationCode(@RequestParam email:String,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("sendcode:ip:$ip",10,60)) return ResponseEntity.status(429)
            .body("Too many requests from this IP address.")
        if(!rateLimiter.allowRequest("sendcode:email:$email",5,60)) return ResponseEntity.status(429)
            .body("Too many requests for this e-mail address.")
        if(!verificationCodeStorage.canSendCode(email)) return ResponseEntity.status(429)
            .body("Wait 1 minute before requesting new code")
        val code=verificationCodeGenerator.generateVerificationCode()
        verificationCodeStorage.storeCode(email,code)
        emailService.sendVerificationCode(email,code)
        return ResponseEntity.ok("Verification code sent.")
    }

    @PostMapping("/verify-code")
    fun verifyCode(@RequestParam email:String,@RequestParam code:String):ResponseEntity<String> {
        if(!verificationCodeStorage.validateCode(email,code)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid code")
        val user=userRepository.findAll().firstOrNull{it.email?.equals(email,ignoreCase=true)==true}?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found")
        user.verified=true
        userRepository.save(user)
        return ResponseEntity.ok("Verified")
    }
}
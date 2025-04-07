package org.aleks616.shrendar.controllers
import org.aleks616.shrendar.services.SendEmailService
import org.aleks616.shrendar.VerificationCodeGenerator
import org.aleks616.shrendar.VerificationCodeStorage
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
    @Autowired private val emailService:SendEmailService,
    private val verificationCodeGenerator:VerificationCodeGenerator,
    private val verificationCodeStorage:VerificationCodeStorage
){

    @PostMapping("/send-code")
    fun sendVerificationCode(@RequestParam email:String):ResponseEntity<String>{
        if(!verificationCodeStorage.canSendCode(email)) return ResponseEntity.status(429).body("Wait 1 minute before requesting new code")
        val code=verificationCodeGenerator.generateVerificationCode()
        verificationCodeStorage.storeCode(email,code)
        emailService.sendVerificationCode(email,code)
        return ResponseEntity.ok("Verification code sent")
    }

    @PostMapping("/verify-code")
    fun verifyCode(@RequestParam email:String,@RequestParam code:String):ResponseEntity<String>{
        return if(verificationCodeStorage.validateCode(email,code))
            ResponseEntity.ok("Verified")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid code")
    }
}
package org.aleks616.shrendar.services

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import jakarta.mail.internet.InternetAddress

@Service
class SendEmailService(
    private val mailSender:JavaMailSender
){
    fun sendVerificationCode(email:String,code:String){
        InternetAddress(email.trim()).apply {validate()}
        val message=SimpleMailMessage().apply {
            setTo(email.trim())
            subject="Verify Your Account"
            text="""
                Your verification code is: $code
                
                This code expires in 15 minutes.
                If you didn't request this, please ignore this email.
            """.trimIndent()
        }
        mailSender.send(message)
    }
}
package org.aleks616.shrendar.mail.service

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.io.File

@Service
class EmailService(
    private val mailSender:JavaMailSender
){
    fun sendVerificationCode(email:String,code:String){
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val languageCode="en" //todo: language param (with frontend/mobile)
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/verificationCode-$languageCode.html").readText().replace("\$code",code)
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Confirm your e-mail address"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")

        mailSender.send(mimeMessage)
    }

    fun sendAccountCreatedMessage(email:String){
        InternetAddress(email.trim()).apply {validate()}
        val message=SimpleMailMessage().apply {
            setTo(email.trim())
            subject="Account Created"
            text="""
                Your account has been created successfully.
                
                You can now log in into your account.
            """
        }
        mailSender.send(message)
    }

    fun sendPasswordResetMessage(email:String,code:String){
        InternetAddress(email.trim()).apply {validate()}
        val message=SimpleMailMessage().apply {
            setTo(email.trim())
            subject="Password Reset"
            text="""
                Your password reset code is: $code
                Or you can click this link: http://localhost:3000/reset-password?code=$code
                
                This code expires in 15 minutes.
                If you didn't request this, someone may be trying to access your account.
            """
        }
        mailSender.send(message)
    }
}
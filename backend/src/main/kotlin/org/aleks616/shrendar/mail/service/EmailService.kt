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
) {
    fun sendVerificationCode(email:String,code:String) {
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

    fun sendAccountCreatedMessage(email:String) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val languageCode="en" //todo: language param (with frontend/mobile)
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/accountVerified-$languageCode.html").readText()
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Account Created"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }

    fun sendPasswordResetMessage(email:String,code:String) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val languageCode="en" //todo: language param (with frontend/mobile)
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/passwordResetCode-$languageCode.html").readText().replace("\$code",code)
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Password reset"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }
}
package org.aleks616.shrendar.mail.service

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import org.aleks616.shrendar.common.model.SupportedLanguages
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.io.File

@Service
class EmailService(
    private val mailSender:JavaMailSender
) {
    fun sendVerificationCode(email:String,code:String,language:SupportedLanguages=SupportedLanguages.EN) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/verificationCode-$language.html").readText()
            .replace($$"$code",code)
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Confirm your e-mail address"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")

        mailSender.send(mimeMessage)
    }

    fun sendAccountCreatedMessage(email:String,language:SupportedLanguages=SupportedLanguages.EN) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/accountVerified-$language.html").readText()
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Account Created"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }

    fun sendPasswordResetMessage(email:String,code:String,language:SupportedLanguages=SupportedLanguages.EN) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/passwordResetCode-$language.html").readText()
            .replace($$"$code",code)
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Password reset"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }

    fun sendPasswordHasBeenChangedMessage(email:String,language:SupportedLanguages=SupportedLanguages.EN) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/passwordChanged-$language.html").readText()
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Password has been changed"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }

    fun sendAccountScheduledForDeletionMessage(email:String,language:SupportedLanguages=SupportedLanguages.EN) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val content=
            File("src/main/kotlin/org/aleks616/shrendar/mail/html/accountDeletionScheduled-$language.html").readText()
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Account will be deleted"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }

    fun sendAccountDeletionCancelledMessage(email:String,language:SupportedLanguages=SupportedLanguages.EN) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val content=
            File("src/main/kotlin/org/aleks616/shrendar/mail/html/accountDeletionCanceled-$language.html").readText()
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Account won't be deleted"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }

    fun sendAccountDeletedMessage(email:String,language:SupportedLanguages=SupportedLanguages.EN) {
        val address=email.trim()
        InternetAddress(address).apply {validate()}
        val content=File("src/main/kotlin/org/aleks616/shrendar/mail/html/accountDeleted-$language.html").readText()
        val mimeMessage=mailSender.createMimeMessage()
        mimeMessage.subject="Account has been deleted"
        mimeMessage.setRecipient(Message.RecipientType.TO,InternetAddress(address))
        mimeMessage.setContent(content,"text/html; charset=utf-8")
        mailSender.send(mimeMessage)
    }
}
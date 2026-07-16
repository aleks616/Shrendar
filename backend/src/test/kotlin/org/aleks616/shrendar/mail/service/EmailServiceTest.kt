package org.aleks616.shrendar.mail.service

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mail.javamail.JavaMailSender

class EmailServiceTest {
    private lateinit var mailSender:JavaMailSender
    private lateinit var emailService:EmailService
    private lateinit var mimeMessage:MimeMessage

    @BeforeEach
    fun setUp() {
        mailSender=mock(JavaMailSender::class.java)
        emailService=EmailService(mailSender)
        mimeMessage=mock(MimeMessage::class.java)
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
    }

    @Test
    fun testSendVerificationCode() {
        val email="test@example.com"
        val code="123456"

        emailService.sendVerificationCode(email,code)

        verify(mailSender).send(mimeMessage)
        verify(mimeMessage).setRecipient(eq(Message.RecipientType.TO),eq(InternetAddress(email)))
        verify(mimeMessage).subject="Confirm your e-mail address"
    }

    @Test
    fun testSendAccountCreatedMessage() {
        val email="test@example.com"

        emailService.sendAccountCreatedMessage(email)

        verify(mailSender).send(mimeMessage)
        verify(mimeMessage).subject="Account Created"
    }

    @Test
    fun testSendPasswordResetMessage() {
        val email="test@example.com"
        val code="RESET123"

        emailService.sendPasswordResetMessage(email,code)

        verify(mailSender).send(mimeMessage)
        verify(mimeMessage).subject="Password reset"
    }

    @Test
    fun testSendPasswordHasBeenChangedMessage() {
        val email="test@example.com"

        emailService.sendPasswordHasBeenChangedMessage(email)

        verify(mailSender).send(mimeMessage)
        verify(mimeMessage).subject="Account Created"
    }

    @Test
    fun testInvalidEmailThrowsException() {
        val email="invalid-email"
        assertThrows(Exception::class.java) {
            emailService.sendVerificationCode(email,"123")
        }
    }
}

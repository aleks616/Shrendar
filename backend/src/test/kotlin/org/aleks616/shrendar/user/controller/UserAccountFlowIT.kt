package org.aleks616.shrendar.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.aleks616.shrendar.securityCode.CodeStorage
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.ResetPassword
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAccountFlowIT {

    @Autowired
    private lateinit var mockMvc:MockMvc

    @Autowired
    private lateinit var userRepository:UserRepository

    @Autowired
    private lateinit var rankRepository:RankRepository

    @Autowired
    @Qualifier("registrationCodeStorage")
    private lateinit var registrationCodeStorage:CodeStorage

    @Autowired
    @Qualifier("passwordResetCodeStorage")
    private lateinit var passwordResetCodeStorage:CodeStorage

    private var objectMapper:ObjectMapper = ObjectMapper().findAndRegisterModules()

    @MockitoBean
    private lateinit var mailSender:JavaMailSender

    @Autowired
    private lateinit var userLogRepository: org.aleks616.shrendar.user.repository.UserLogRepository

    @BeforeEach
    fun setup() {
        val mimeMessage=mock(jakarta.mail.internet.MimeMessage::class.java)
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        userLogRepository.deleteAll()
        userRepository.deleteAll()
        if(!rankRepository.existsById(1)) {
            rankRepository.save(Rank().apply {
                id=1
                name="Newbie"
                minXp=0
            })
        }

        clearCodeStorage(registrationCodeStorage)
        clearCodeStorage(passwordResetCodeStorage)
    }

    private fun clearCodeStorage(storage:CodeStorage) {
        val codesField=CodeStorage::class.java.getDeclaredField("codes")
        codesField.isAccessible=true
        (codesField.get(storage) as MutableMap<*,*>).clear()

        val expiryField=CodeStorage::class.java.getDeclaredField("codeExpiry")
        expiryField.isAccessible=true
        (expiryField.get(storage) as MutableMap<*,*>).clear()

        val lastSentField=CodeStorage::class.java.getDeclaredField("lastSentTime")
        lastSentField.isAccessible=true
        (lastSentField.get(storage) as MutableMap<*,*>).clear()
    }

    @Test
    fun `full registration flow should work`() {
        val registerRequest=UserAccountController.RegisterRequest(
            login="testuser",
            displayName="Test User",
            email="test@example.com",
            password="password123"
        )

        mockMvc.post("/api/user-account/register") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(registerRequest)
        }.andExpect {
            status {isOk()}
        }

        val codesField=CodeStorage::class.java.getDeclaredField("codes")
        codesField.isAccessible=true
        val codes=codesField.get(registrationCodeStorage) as Map<String,String>
        val code=codes["test@example.com"]
        assertNotNull(code,"Verification code should be stored")

        mockMvc.post("/api/user-account/register/confirm") {
            param("code",code!!)
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(registerRequest)
        }.andExpect {
            status {isOk()}
        }

        val user=userRepository.findByEmail("test@example.com")
        assertNotNull(user)
        assertTrue(user?.verified==true)
        assertEquals("testuser",user?.login)
    }

    @Test
    fun `full password reset flow should work`() {
        val email="reset@example.com"
        val login="resetuser"
        val oldPassword="oldPassword"

        val regReq=UserAccountController.RegisterRequest(login,"Reset User",email,oldPassword)
        mockMvc.post("/api/user-account/register") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(regReq)
        }
        val codeField=CodeStorage::class.java.getDeclaredField("codes")
        codeField.isAccessible=true
        val regCode=(codeField.get(registrationCodeStorage) as Map<String,String>)[email]

        mockMvc.post("/api/user-account/register/confirm") {
            param("code",regCode!!)
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(regReq)
        }

        mockMvc.post("/api/user-account/requestPasswordReset") {
            param("accountKey",email)
        }.andExpect {
            status {isOk()}
        }

        val resetCode=(codeField.get(passwordResetCodeStorage) as Map<String,String>)[email]
        assertNotNull(resetCode,"Reset code should be stored")

        val newPassword="newPassword123"
        val resetRequest=ResetPassword(email,newPassword)

        mockMvc.post("/api/user-account/resetPassword") {
            param("code",resetCode!!)
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(resetRequest)
        }.andExpect {
            status {isOk()}
        }

        val loginReq=UserAccountController.LoginRequest(login,null,newPassword)
        mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(loginReq)
        }.andExpect {
            status {isOk()}
        }
    }
}

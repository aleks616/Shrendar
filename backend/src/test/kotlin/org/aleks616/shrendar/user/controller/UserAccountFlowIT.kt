package org.aleks616.shrendar.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.aleks616.shrendar.securityCode.CodeStorage
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.ResetPassword
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.aleks616.shrendar.user.service.UserService
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

    private var objectMapper:ObjectMapper=ObjectMapper().findAndRegisterModules()

    @MockitoBean
    private lateinit var mailSender:JavaMailSender

    @Autowired
    private lateinit var rateLimiter:org.aleks616.shrendar.security.RateLimiter

    @Autowired
    private lateinit var userLogRepository:org.aleks616.shrendar.user.repository.UserLogRepository

    @Autowired
    private lateinit var userPasswordHistoryRepository:org.aleks616.shrendar.user.repository.UserPasswordHistoryRepository

    @BeforeEach
    fun setup() {
        val mimeMessage=mock(jakarta.mail.internet.MimeMessage::class.java)
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        userPasswordHistoryRepository.deleteAll()
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

        val storageField=org.aleks616.shrendar.security.RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<*,*>).clear()
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

    @Autowired
    private lateinit var userService:UserService

    @Test
    fun `should update username and handle 90-day restriction`() {
        val email="user@example.com"
        val login="user1"
        registerAndConfirm(login,email)

        mockMvc.post("/api/user-account/updateUsername") {
            param("email",email)
            param("newUsername","newDisplayName")
        }.andExpect {
            status {isOk()}
        }

        val user=userRepository.findByEmail(email)
        assertEquals("newDisplayName",user?.username)

        mockMvc.post("/api/user-account/updateUsername") {
            param("email",email)
            param("newUsername","anotherName")
        }.andExpect {
            status {isBadRequest()}
        }
    }

    @Test
    fun `should update email`() {
        val email="old@example.com"
        val login="emailuser"
        registerAndConfirm(login,email)

        val newEmail="new@example.com"
        mockMvc.post("/api/user-account/updateEmail") {
            param("email",email)
            param("newEmail",newEmail)
        }.andExpect {
            status {isOk()}
        }

        assertNull(userRepository.findByEmail(email))
        assertNotNull(userRepository.findByEmail(newEmail))
    }

    @Test
    fun `should add birthday and handle restrictions`() {
        val email="birth@example.com"
        registerAndConfirm("birthuser",email)

        val youngDate=LocalDate.now().minusYears(10)
        mockMvc.post("/api/user-account/addBirthday") {
            param("email",email)
            param("date",youngDate.toString())
        }.andExpect {
            status {isBadRequest()}
        }

        val validDate=LocalDate.now().minusYears(20)
        mockMvc.post("/api/user-account/addBirthday") {
            param("email",email)
            param("date",validDate.toString())
        }.andExpect {
            status {isOk()}
        }

        val user=userRepository.findByEmail(email)
        assertEquals(validDate,user?.birthDate)

        mockMvc.post("/api/user-account/addBirthday") {
            param("email",email)
            param("date",validDate.minusDays(1).toString())
        }.andExpect {
            status {isBadRequest()}
        }
    }

    @Test
    fun `account deletion flow should work`() {
        val email="delete@example.com"
        val password="password123"
        registerAndConfirm("deleteuser",email,password)

        val loginRequest=UserAccountController.LoginRequest(null,email,password)
        mockMvc.post("/api/user-account/deleteAccount") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status {isOk()}
        }

        val user=userRepository.findByEmail(email)
        assertNotNull(user)
        val userLog=userLogRepository.findById(user!!.id!!).get()
        assertNotNull(userLog.accountDeletionScheduledTime)

        mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status {isOk()}
        }

        val userLogAfterLogin=userLogRepository.findById(user.id!!).get()
        assertNull(userLogAfterLogin.accountDeletionScheduledTime)

        userService.requestDeletion(email)
        val userLogAfterSecondRequest=userLogRepository.findById(user.id!!).get()
        userLogAfterSecondRequest.accountDeletionScheduledTime=Instant.now().minus(22,ChronoUnit.DAYS)
        userLogRepository.save(userLogAfterSecondRequest)

        userService.checkAccountScheduledToBeDeleted()

        assertNull(userRepository.findByEmail(email))
    }

    @Test
    fun `login and logout flow should work`() {
        val email="login@example.com"
        val password="password123"
        registerAndConfirm("loginuser",email,password)

        val loginReq=UserAccountController.LoginRequest(null,email,password)
        val result=mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(loginReq)
        }.andExpect {
            status {isOk()}
        }.andReturn()

        val responseMap=objectMapper.readValue(result.response.contentAsString,Map::class.java)
        val token=responseMap["token"] as String
        assertNotNull(token)

        mockMvc.post("/api/user-account/logout") {
            header("Authorization","Bearer $token")
        }.andExpect {
            status {isOk()}
        }
    }

    @Test
    fun `login check should return true for existing login`() {
        val login="existuser"
        registerAndConfirm(login,"exist@example.com")

        mockMvc.get("/api/user-account/loginCheck") {
            param("login",login)
        }.andExpect {
            status {isOk()}
            content {string("true")}
        }
    }

    @Test
    fun `email check should return true for existing email`() {
        val email="exist@example.com"
        registerAndConfirm("existuser",email)

        mockMvc.get("/api/user-account/emailCheck") {
            param("email",email)
        }.andExpect {
            status {isOk()}
            content {string("true")}
        }
    }

    @Test
    fun `get users should return list of users`() {
        val login="existuser"
        registerAndConfirm(login,"exist@example.com")

        mockMvc.get("/api/user-account/users")
            .andExpect {
                status {isOk()}
            }.andExpect {
                jsonPath("$[?(@.login == '$login')]") {exists()}
            }
    }

    @Test
    fun `register IP rate limit should work`() {
        val registerRequest=UserAccountController.RegisterRequest("rateuser","Rate User","rate@example.com","pass")

        repeat(10) {
            mockMvc.post("/api/user-account/register") {
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(registerRequest)
                with {it.apply {remoteAddr="1.2.3.4"}}
            }
        }
        mockMvc.post("/api/user-account/register") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(registerRequest)
            with {it.apply {remoteAddr="1.2.3.4"}}
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `login IP rate limit should work`() {
        val loginReq=UserAccountController.LoginRequest(null,"rate@example.com","pass")
        repeat(10) {
            mockMvc.post("/api/user-account/login") {
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(loginReq)
                with {it.apply {remoteAddr="5.6.7.8"}}
            }
        }
        mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(loginReq)
            with {it.apply {remoteAddr="5.6.7.8"}}
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `register rate limit should work`() {
        val email="rate@example.com"
        val registerRequest=UserAccountController.RegisterRequest("rateuser","Rate User",email,"pass")

        repeat(5) {
            mockMvc.post("/api/user-account/register") {
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(registerRequest)
            }
        }
        mockMvc.post("/api/user-account/register") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(registerRequest)
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `register confirm rate limit should work`() {
        val registerRequest=UserAccountController.RegisterRequest("rateuser","Rate User","rate@example.com","pass")
        repeat(10) {
            mockMvc.post("/api/user-account/register/confirm") {
                param("code","1234")
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(registerRequest)
            }
        }
        mockMvc.post("/api/user-account/register/confirm") {
            param("code","1234")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(registerRequest)
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `password reset rate limit should work`() {
        val email="rate@example.com"
        mockMvc.post("/api/user-account/requestPasswordReset") {
            param("accountKey",email)
        }
        mockMvc.post("/api/user-account/requestPasswordReset") {
            param("accountKey",email)
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `reset password rate limit should work`() {
        val email="rate@example.com"
        repeat(2) {
            mockMvc.post("/api/user-account/resetPassword") {
                param("code","1234")
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(ResetPassword(email,"newpass"))
            }
        }
        mockMvc.post("/api/user-account/resetPassword") {
            param("code","1234")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(ResetPassword(email,"newpass"))
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `login rate limit should work`() {
        val email="rate@example.com"
        val loginReq=UserAccountController.LoginRequest(null,email,"pass")
        repeat(5) {
            mockMvc.post("/api/user-account/login") {
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(loginReq)
            }
        }
        mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(loginReq)
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `request password reset should return bad request for unknown account`() {
        mockMvc.post("/api/user-account/requestPasswordReset") {
            param("accountKey","nonexistent@example.com")
        }.andExpect {
            status {isBadRequest()}
        }
    }

    @Test
    fun `request password reset should return bad request during cooldown`() {
        val email="cooldown@example.com"
        registerAndConfirm("cooluser",email)

        mockMvc.post("/api/user-account/requestPasswordReset") {
            param("accountKey",email)
        }.andExpect {
            status {isOk()}
        }

        val storageField=org.aleks616.shrendar.security.RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<String,Any>).remove("reset:acct:$email")

        mockMvc.post("/api/user-account/requestPasswordReset") {
            param("accountKey",email)
        }.andExpect {
            status {isBadRequest()}
            content {string("Could not send password reset code, try again in 5 minutes")}
        }
    }

    @Test
    fun `login should return unauthorized for invalid credentials`() {
        registerAndConfirm("erroruser","error@example.com")
        val invalidLogin=UserAccountController.LoginRequest("erroruser",null,"wrongpass")
        mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(invalidLogin)
        }.andExpect {
            status {isUnauthorized()}
        }
    }

    @Test
    fun `update username should return not found for unknown account`() {
        mockMvc.post("/api/user-account/updateUsername") {
            param("email","nonexistent@example.com")
            param("newUsername","new")
        }.andExpect {
            status {isNotFound()}
        }
    }

    @Test
    fun `update username should return conflict for existing username`() {
        registerAndConfirm("erroruser","error@example.com")
        registerAndConfirm("otheruser","other@example.com")
        mockMvc.post("/api/user-account/updateUsername") {
            param("email","error@example.com")
            param("newUsername","otheruser")
        }.andExpect {
            status {isConflict()}
        }
    }

    @Test
    fun `update email should return not found for unknown account`() {
        mockMvc.post("/api/user-account/updateEmail") {
            param("email","nonexistent@example.com")
            param("newEmail","new@example.com")
        }.andExpect {
            status {isNotFound()}
        }
    }

    @Test
    fun `update email should return conflict for existing email`() {
        registerAndConfirm("erroruser","error@example.com")
        registerAndConfirm("otheruser","other@example.com")
        mockMvc.post("/api/user-account/updateEmail") {
            param("email","error@example.com")
            param("newEmail","other@example.com")
        }.andExpect {
            status {isConflict()}
        }
    }

    @Test
    fun `add birthday should return not found for unknown account`() {
        mockMvc.post("/api/user-account/addBirthday") {
            param("email","nonexistent@example.com")
            param("date","2000-01-01")
        }.andExpect {
            status {isNotFound()}
        }
    }

    @Test
    fun `login should return bad request for empty credentials`() {
        val emptyLogin=UserAccountController.LoginRequest("","","pass")
        mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(emptyLogin)
        }.andExpect {
            status {isUnauthorized()}
        }
    }

    @Test
    fun `request password reset should work when using login`() {
        val email="loginreset@example.com"
        val login="loginresetuser"
        registerAndConfirm(login,email)

        mockMvc.post("/api/user-account/requestPasswordReset") {
            param("accountKey",login)
        }.andExpect {
            status {isOk()}
        }

        val resetCodeField=CodeStorage::class.java.getDeclaredField("codes")
        resetCodeField.isAccessible=true
        val resetCode=(resetCodeField.get(passwordResetCodeStorage) as Map<String,String>)[login]
        assertNotNull(resetCode,"Reset code should be stored under login key")
    }

    @Test
    fun `doesLoginExist should return true for existing login`() {
        val login="loginexist"
        registerAndConfirm(login,"loginexist@example.com")
        assertTrue(userService.doesAccountExist(login))
    }

    @Test
    fun `login should return unauthorized for missing credentials`() {
        val loginReq=UserAccountController.LoginRequest(null,null,"pass")
        mockMvc.post("/api/user-account/login") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(loginReq)
        }.andExpect {
            status {isUnauthorized()}
            jsonPath("$.error") {value("Invalid credentials")}
        }
    }


    private fun registerAndConfirm(login:String,email:String,password:String="password") {
        val regReq=UserAccountController.RegisterRequest(login,login,email,password)
        mockMvc.post("/api/user-account/register") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(regReq)
        }
        val codeField=CodeStorage::class.java.getDeclaredField("codes")
        codeField.isAccessible=true
        val code=(codeField.get(registrationCodeStorage) as Map<String,String>)[email]

        mockMvc.post("/api/user-account/register/confirm") {
            param("code",code!!)
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(regReq)
        }
    }
}

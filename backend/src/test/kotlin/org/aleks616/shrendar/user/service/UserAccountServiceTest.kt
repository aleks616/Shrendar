package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.mail.service.EmailService
import org.aleks616.shrendar.securityCode.CodeStorage
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.RegisterRequestDto
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UserLog
import org.aleks616.shrendar.user.model.UserPasswordHistory
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserLogRepository
import org.aleks616.shrendar.user.repository.UserPasswordHistoryRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mockStatic
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Optional

class UserAccountServiceTest {
    private val users=mock(UserRepository::class.java)
    private val userLogs=mock(UserLogRepository::class.java)
    private val ranks=mock(RankRepository::class.java)
    private val passwordHistory=mock(UserPasswordHistoryRepository::class.java)
    private val registrationCodes=mock(CodeStorage::class.java)
    private val resetCodes=mock(CodeStorage::class.java)
    private val emailService=mock(EmailService::class.java)
    private val encoder=mock(BCryptPasswordEncoder::class.java)
    private val xpService=mock(XpService::class.java)
    private lateinit var service:UserAccountService

    @BeforeEach
    fun setup() {
        service=UserAccountService(
            users,userLogs,ranks,passwordHistory,registrationCodes,resetCodes,emailService,encoder,xpService
        )
    }

    @Test
    fun `addBio saves the bio for an existing user`() {
        val user=User().apply {login="bio-user"}
        `when`(users.findByLogin("bio-user")).thenReturn(user)

        service.addBio("Updated bio","bio-user")

        assertEquals("Updated bio",user.bio)
        verify(users).save(user)
    }

    @Test
    fun `addBio throws when the user does not exist`() {
        `when`(users.findByLogin("missing")).thenReturn(null)

        assertThrows(IllegalStateException::class.java) {service.addBio("Updated bio","missing")}

        verify(users,never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `getUsersDto maps all user fields`() {
        val user=User().apply {
            id=7
            login="tester"
            username="Test User"
            passwordHash="hash"
            email="tester@example.com"
            rank=Rank().apply {id=2; name="Member"}
            birthDate=LocalDate.of(2000,2,29)
            xp=42
            verified=true
        }
        `when`(users.findAll()).thenReturn(listOf(user))

        val result=service.getUsersDto().single()

        assertEquals(7,result.id)
        assertEquals("tester",result.login)
        assertEquals("Test User",result.username)
        assertEquals("hash",result.passwordHash)
        assertEquals("tester@example.com",result.email)
        assertEquals(2,result.ranks?.id)
        assertEquals("Member",result.ranks?.name)
        assertEquals("2000-02-29",result.birthDate)
        assertEquals(42,result.xp)
        assertEquals(true,result.verified)
    }

    @Test
    fun `authenticate rejects a deleted account`() {
        val user=User().apply {id=7; login="deleted"; deleted=true}
        `when`(users.findByLogin("deleted")).thenReturn(user)

        assertEquals(null,service.authenticate(org.aleks616.shrendar.user.model.LoginRequestDto("deleted",null,"password")))

        verifyNoInteractions(userLogs,emailService,encoder,xpService)
    }

    @Test
    fun `authenticate awards xp when the previous login was not today`() {
        val user=User().apply {
            id=7
            login="tester"
            passwordHash="hash"
            deleted=false
        }
        val userLog=org.aleks616.shrendar.user.model.UserLog()
        val yesterday=Instant.parse("2026-09-13T12:00:00Z")
        val today=Instant.parse("2026-09-14T12:00:00Z")
        val utc=ZoneId.of("UTC")
        val nextDay=LocalDate.of(2026,9,14)
        `when`(users.findByLogin("tester")).thenReturn(user)
        `when`(userLogs.findById(7)).thenReturn(Optional.of(userLog))
        `when`(encoder.matches("password","hash")).thenReturn(true)

        mockStatic(Instant::class.java).use { mockedInstant ->
            mockedInstant.`when`<Instant> {Instant.now()}.thenReturn(yesterday,today,today)
            mockStatic(LocalDate::class.java).use { mockedDate ->
                mockedDate.`when`<LocalDate> {LocalDate.now(utc)}.thenReturn(nextDay)

                assertEquals("tester",service.authenticate(org.aleks616.shrendar.user.model.LoginRequestDto("tester",null,"password")))
            }
        }

        verify(xpService).increaseUserXp("tester",6)
    }

    @Test
    fun `initiateRegistration rejects the anonymous user login`() {
        assertEquals(false,service.initiateRegistration(RegisterRequestDto("anonymousUser","Anonymous","user@example.com","password")))
        verifyNoInteractions(users,registrationCodes,emailService)
    }

    @Test
    fun `initiateRegistration rejects an existing login`() {
        `when`(users.findAll()).thenReturn(listOf(User().apply {login="existing"; email="other@example.com"}))

        assertEquals(false,service.initiateRegistration(RegisterRequestDto("existing","User","user@example.com","password")))
        verifyNoInteractions(registrationCodes,emailService)
    }

    @Test
    fun `initiateRegistration rejects an existing email`() {
        `when`(users.findAll()).thenReturn(listOf(User().apply {login="other"; email="user@example.com"}))

        assertEquals(false,service.initiateRegistration(RegisterRequestDto("new-user","User","user@example.com","password")))
        verifyNoInteractions(registrationCodes,emailService)
    }

    @Test
    fun `requestPasswordReset returns false when the account does not exist`() {
        `when`(users.findAll()).thenReturn(emptyList())

        assertEquals(false,service.requestPasswordReset("missing"))
        verifyNoInteractions(resetCodes,emailService)
    }

    @Test
    fun `requestPasswordReset returns false when the matching login cannot be loaded`() {
        `when`(users.findAll()).thenReturn(listOf(User().apply {login="tester"}))
        `when`(resetCodes.canSendCode("tester")).thenReturn(true)
        `when`(users.findByLogin("tester")).thenReturn(null)

        assertEquals(false,service.requestPasswordReset("tester"))
        verifyNoInteractions(emailService)
    }

    @Test
    fun `requestPasswordReset returns false when the matching login has no email`() {
        `when`(users.findAll()).thenReturn(listOf(User().apply {login="tester"}))
        `when`(resetCodes.canSendCode("tester")).thenReturn(true)
        `when`(users.findByLogin("tester")).thenReturn(User().apply {login="tester"; email=null})

        assertEquals(false,service.requestPasswordReset("tester"))
        verifyNoInteractions(emailService)
    }

    @Test
    fun `changePassword returns false when no user has the email`() {
        `when`(resetCodes.validateCode("user@example.com","code")).thenReturn(true)
        `when`(users.findAll()).thenReturn(emptyList())

        assertEquals(false,service.changePassword("user@example.com","new-password","code"))
    }

    @Test
    fun `changePassword returns false when the password was used before`() {
        val user=User().apply {id=7; email="user@example.com"}
        val history=UserPasswordHistory().apply {password="old-hash"}
        `when`(resetCodes.validateCode("user@example.com","code")).thenReturn(true)
        `when`(users.findAll()).thenReturn(listOf(user))
        `when`(passwordHistory.findAllByUserId(7)).thenReturn(listOf(history))
        `when`(encoder.encode("new-password")).thenReturn("old-hash")

        assertEquals(false,service.changePassword("user@example.com","new-password","code"))
        verify(users,never()).save(user)
    }

    @Test
    fun `changeUsername returns false when the user does not exist`() {
        `when`(users.findByEmail("missing@example.com")).thenReturn(null)

        assertEquals(false,service.changeUsername("missing@example.com","New Name"))
    }

    @Test
    fun `changeUsername returns false when changed less than 90 days ago`() {
        val user=User().apply {id=7; email="user@example.com"}
        val log=UserLog().apply {displayNameChangedTime=Instant.now().minusSeconds(89 * 24 * 60 * 60)}
        `when`(users.findByEmail("user@example.com")).thenReturn(user)
        `when`(userLogs.findById(7)).thenReturn(Optional.of(log))

        assertEquals(false,service.changeUsername("user@example.com","New Name"))
    }

    @Test
    fun `addBirthday returns false when the user does not exist`() {
        `when`(users.findByEmail("missing@example.com")).thenReturn(null)

        assertEquals(false,service.addBirthday("missing@example.com",LocalDate.of(2000,1,1)))
    }

    @Test
    fun `addBirthday returns false when changed less than 180 days ago`() {
        val user=User().apply {id=7; email="user@example.com"}
        val log=UserLog().apply {birthdayChangedTime=Instant.now().minusSeconds(179 * 24 * 60 * 60)}
        `when`(users.findByEmail("user@example.com")).thenReturn(user)
        `when`(userLogs.findById(7)).thenReturn(Optional.of(log))

        assertEquals(false,service.addBirthday("user@example.com",LocalDate.of(2000,1,1)))
        verify(users,never()).save(user)
    }

    @Test
    fun `requestDeletion returns false when the user does not exist`() {
        `when`(users.findByEmail("missing@example.com")).thenReturn(null)

        assertEquals(false,service.requestDeletion("missing@example.com"))
    }

    @Test
    fun `checkAccountScheduledToBeDeleted keeps accounts scheduled less than 21 days ago`() {
        val user=User().apply {id=7; email="user@example.com"}
        val log=UserLog().apply {accountDeletionScheduledTime=Instant.now().minusSeconds(20 * 24 * 60 * 60)}
        `when`(users.findAll()).thenReturn(listOf(user))
        `when`(userLogs.findById(7)).thenReturn(Optional.of(log))

        service.checkAccountScheduledToBeDeleted()

        verify(users,never()).save(user)
        verifyNoInteractions(emailService)
    }
}

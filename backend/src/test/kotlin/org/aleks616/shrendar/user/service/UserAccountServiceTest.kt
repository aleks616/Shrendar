package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.mail.service.EmailService
import org.aleks616.shrendar.securityCode.CodeStorage
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
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
}

package org.aleks616.shrendar.userban.service

import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.UserRepository
import org.aleks616.shrendar.userban.model.BanDto
import org.aleks616.shrendar.userban.model.UserBansDto
import org.aleks616.shrendar.userban.model.UsersBan
import org.aleks616.shrendar.userban.repository.UsersBanRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.Duration
import java.time.Instant

class UserBanServiceTest {
    private val bans=mock(UsersBanRepository::class.java)
    private val users=mock(UserRepository::class.java)
    private lateinit var service:UserBanService
    private lateinit var banned:User
    private lateinit var moderator:User

    @BeforeEach fun setup() {
        service=UserBanService(bans,users)
        banned=user(1,"banned",1)
        moderator=user(2,"mod",10)
    }

    @Test fun `isBanned supports user ids and logins`() {
        `when`(bans.findIfUserBanned(1)).thenReturn(1)
        `when`(bans.findIfUserBanned("banned")).thenReturn(0)
        assertTrue(service.isBanned(1))
        assertFalse(service.isBanned("banned"))
    }

    @Test fun `active bans and appeals map ban and user details`() {
        val ban=ban()
        `when`(bans.findByUntilIsAfterNow()).thenReturn(mutableListOf(ban))
        `when`(bans.findByActiveAndHasAppeal()).thenReturn(mutableListOf(ban))
        val active=service.getActiveBans().single()
        val appealed=service.getBansWithAppeal().single()
        assertEquals(1,active.userId)
        assertEquals("mod",active.byLogin)
        assertEquals(Duration.ofHours(2),active.length)
        assertEquals("appeal",appealed.appealReason)
    }

    @Test fun `user and moderator ban histories map details and empty histories`() {
        `when`(bans.findByUser_Id(1)).thenReturn(mutableListOf(ban()))
        `when`(bans.findByBy_Id(2)).thenReturn(mutableListOf(ban()))
        assertEquals("banned",service.getAllBansOfUser(1).userLogin)
        assertEquals("mod",service.getBansByMod(2).userLogin)
        assertEquals(1,service.getBansByMod(2).bans!!.single().byId)
        `when`(bans.findByUser_Id(9)).thenReturn(mutableListOf())
        assertEquals(UserBansDto(),service.getAllBansOfUser(9))
    }

    @Test fun `getAllBansOfUser returns empty DTO when no bans exist`() {
        `when`(bans.findByUser_Id(9)).thenReturn(mutableListOf())

        assertEquals(UserBansDto(),service.getAllBansOfUser(9))
    }

    @Test fun `getBansByMod returns empty DTO when no bans exist`() {
        `when`(bans.findByBy_Id(9)).thenReturn(mutableListOf())

        assertEquals(UserBansDto(),service.getBansByMod(9))
    }

    @Test fun `current ban returns mapped and raw data or rejects missing bans`() {
        val ban=ban()
        `when`(bans.findCurrentUserBanData(1)).thenReturn(ban)
        assertEquals("reason",service.getCurrentUserBan(1).description)
        assertSame(ban,service.getCurrentUserBanData(1))
        `when`(bans.findCurrentUserBanData(9)).thenReturn(null)
        assertThrows<IllegalStateException> {service.getCurrentUserBan(9)}
        assertThrows<IllegalStateException> {service.getCurrentUserBanData(9)}
    }

    @Test fun `banUser saves requested target moderator and duration`() {
        `when`(users.findByLogin("mod")).thenReturn(moderator)
        `when`(users.findUserById(1)).thenReturn(banned)
        service.banUser(BanDto(1,2,"reason"),"mod")
        val saved=ArgumentCaptor.forClass(UsersBan::class.java)
        verify(bans).save(saved.capture())
        assertSame(banned,saved.value.user)
        assertSame(moderator,saved.value.by)
        assertEquals("reason",saved.value.description)
        assertEquals(Duration.ofHours(2),Duration.between(saved.value.at,saved.value.until))
    }

    @Test fun `banUser throws when moderator does not exist`() {
        `when`(users.findByLogin("mod")).thenReturn(null)

        assertThrows<IllegalStateException> {
            service.banUser(BanDto(1,2,"reason"),"mod")
        }
    }

    @Test fun `banUser throws when user to ban does not exist`() {
        `when`(users.findByLogin("mod")).thenReturn(moderator)
        `when`(users.findUserById(1)).thenReturn(null)

        assertThrows<IllegalStateException> {
            service.banUser(BanDto(1,2,"reason"),"mod")
        }
    }

    @Test fun `appeal rejects unbanned unknown and already appealed users then saves valid appeal`() {
        `when`(bans.findIfUserBanned("banned")).thenReturn(0)
        assertThrows<IllegalStateException> {service.appealBan("appeal","banned")}
        `when`(bans.findIfUserBanned("banned")).thenReturn(1)
        `when`(users.findByLogin("banned")).thenReturn(null)
        assertThrows<IllegalStateException> {service.appealBan("appeal","banned")}
        `when`(users.findByLogin("banned")).thenReturn(banned)
        val existing=ban().apply {appealReason="previous"}
        `when`(bans.findCurrentUserBanData(1)).thenReturn(existing)
        assertThrows<IllegalStateException> {service.appealBan("appeal","banned")}
        existing.appealReason=null
        service.appealBan("appeal","banned")
        assertEquals("appeal",existing.appealReason)
        verify(bans).save(existing)
    }

    @Test fun `cancel rejects invalid states and marks a valid ban appealed`() {
        `when`(bans.findIfUserBanned(1)).thenReturn(0)
        assertThrows<IllegalStateException> {service.cancelBan(1,"mod")}
        `when`(bans.findIfUserBanned(1)).thenReturn(1)
        `when`(users.findUserById(1)).thenReturn(null)
        assertThrows<IllegalStateException> {service.cancelBan(1,"mod")}
        `when`(users.findUserById(1)).thenReturn(banned)
        `when`(bans.findCurrentUserBanData(1)).thenReturn(ban())
        `when`(users.findByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {service.cancelBan(1,"mod")}
        val current=ban()
        `when`(bans.findCurrentUserBanData(1)).thenReturn(current)
        `when`(users.findByLogin("mod")).thenReturn(moderator)
        service.cancelBan(1,"mod")
        assertTrue(current.appealed)
        assertSame(moderator,current.appealedBy)
        verify(bans).save(current)
    }

    private fun user(id:Int,login:String,rank:Int)=User().apply {
        this.id=id; this.login=login; username="$login name"; this.rank=Rank().apply {this.id=rank}
    }
    private fun ban()=UsersBan().apply {
        id=3; user=banned; by=moderator; at=Instant.parse("2026-01-01T00:00:00Z"); until=at.plus(Duration.ofHours(2))
        description="reason"; appealReason="appeal"
    }
}

package org.aleks616.shrendar.userban.controller

import org.aleks616.shrendar.exception.RankTooLowException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.UserAccountService
import org.aleks616.shrendar.userban.model.BanDto
import org.aleks616.shrendar.userban.model.BansDto
import org.aleks616.shrendar.userban.model.UserBansDto
import org.aleks616.shrendar.userban.service.UserBanService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class UserBanControllerTest {
    private val bans=mock(UserBanService::class.java)
    private val accounts=mock(UserAccountService::class.java)
    private val controller=UserBanController(bans,accounts)

    @BeforeEach fun setup() {
        SecurityContextHolder.getContext().authentication=UsernamePasswordAuthenticationToken("mod",null,emptyList())
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(10))
        `when`(accounts.doesUserExist(anyInt())).thenReturn(false)
    }
    @AfterEach fun clear() = SecurityContextHolder.clearContext()

    @Test fun `check delegates only when the controller existence condition permits it`() {
        `when`(bans.isBanned(3)).thenReturn(true)
        assertTrue(controller.isBanned(3))
        `when`(accounts.doesUserExist(3)).thenReturn(true)
        assertThrows<IllegalStateException> {controller.isBanned(3)}
    }

    @Test fun `getActiveBans throws when authentication is missing`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.getActiveBans()}
    }

    @Test fun `getBansWithAppeal throws when authentication is missing`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.getBansWithAppeal()}
    }

    @Test fun `getAllBansOfUser throws when authentication is missing`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.getAllBansOfUser(3)}
    }

    @Test fun `getCurrentUserBanData throws when authentication is missing`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.getCurrentUserBanData(3)}
    }

    @Test fun `getBansByMod throws when authentication is missing`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.getBansByMod(3)}
    }

    @Test fun `banUser throws when authentication is missing`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.banUser(BanDto(3,1,"reason"))}
    }

    @Test fun `appealBan throws when authentication is missing`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.appealBan("reason")}
    }

    @Test fun `getActiveBans throws when authenticated user cannot be found`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {controller.getActiveBans()}
    }

    @Test fun `getBansWithAppeal throws when authenticated user cannot be found`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {controller.getBansWithAppeal()}
    }

    @Test fun `getAllBansOfUser throws when authenticated moderator cannot be found`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {controller.getAllBansOfUser(3)}
    }

    @Test fun `getCurrentUserBanData throws when authenticated moderator cannot be found`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {controller.getCurrentUserBanData(3)}
    }

    @Test fun `getBansByMod throws when authenticated moderator cannot be found`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {controller.getBansByMod(3)}
    }

    @Test fun `banUser throws when authenticated user cannot be found`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {controller.banUser(BanDto(3,1,"reason"))}
    }

    @Test fun `getAllBansOfUser throws when target existence guard is true`() {
        `when`(accounts.doesUserExist(3)).thenReturn(true)
        assertThrows<IllegalStateException> {controller.getAllBansOfUser(3)}
    }

    @Test fun `getCurrentUserBanData throws when target existence guard is true`() {
        `when`(accounts.doesUserExist(3)).thenReturn(true)
        assertThrows<IllegalStateException> {controller.getCurrentUserBanData(3)}
    }

    @Test fun `getBansByMod throws when moderator existence guard is true`() {
        `when`(accounts.doesUserExist(3)).thenReturn(true)
        assertThrows<IllegalStateException> {controller.getBansByMod(3)}
    }

    @Test fun `banUser throws when target existence guard is true`() {
        `when`(accounts.doesUserExist(3)).thenReturn(true)
        assertThrows<IllegalStateException> {controller.banUser(BanDto(3,1,"reason"))}
    }

    @Test fun `getActiveBans rejects a moderator below rank 10`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertThrows<RankTooLowException> {controller.getActiveBans()}
    }

    @Test fun `getBansWithAppeal rejects a moderator below rank 10`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertThrows<RankTooLowException> {controller.getBansWithAppeal()}
    }

    @Test fun `getCurrentUserBanData rejects a moderator below rank 10`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertThrows<RankTooLowException> {controller.getCurrentUserBanData(3)}
    }

    @Test fun `getBansByMod rejects a moderator below rank 10`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertThrows<RankTooLowException> {controller.getBansByMod(3)}
    }

    @Test fun `getBansWithAppeal wraps a service exception`() {
        `when`(bans.getBansWithAppeal()).thenThrow(IllegalStateException("broken"))
        val exception=assertThrows<IllegalStateException> {controller.getBansWithAppeal()}
        assertEquals("something_wrong. broken",exception.message)
    }

    @Test fun `banUser rejects a missing target id`() {
        val result=controller.banUser(BanDto(null,1,"reason"))
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("User id duration and description are required",result.body)
        verifyNoInteractions(bans)
    }

    @Test fun `banUser rejects a missing duration`() {
        val result=controller.banUser(BanDto(3,null,"reason"))
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("User id duration and description are required",result.body)
        verifyNoInteractions(bans)
    }

    @Test fun `banUser rejects a missing description`() {
        val result=controller.banUser(BanDto(3,1,null))
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("User id duration and description are required",result.body)
        verifyNoInteractions(bans)
    }

    @Test fun `banUser rejects an empty description`() {
        val result=controller.banUser(BanDto(3,1,""))
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("User id duration and description are required",result.body)
        verifyNoInteractions(bans)
    }

    @Test fun `moderator ban queries delegate and wrap active query errors`() {
        val active=listOf(BansDto(id=1))
        val history=UserBansDto(userId=3)
        `when`(bans.getActiveBans()).thenReturn(active)
        `when`(bans.getBansWithAppeal()).thenReturn(active)
        `when`(bans.getAllBansOfUser(3)).thenReturn(history)
        `when`(bans.getBansByMod(4)).thenReturn(history)
        assertEquals(active,controller.getActiveBans())
        assertEquals(active,controller.getBansWithAppeal())
        assertEquals(history,controller.getAllBansOfUser(3))
        assertEquals(history,controller.getBansByMod(4))
        `when`(bans.getActiveBans()).thenThrow(IllegalStateException("broken"))
        assertEquals("something_wrong. broken",assertThrows<IllegalStateException> {controller.getActiveBans()}.message)
    }

    @Test fun `moderator ban queries reject missing authentication moderator rank and target under current existence guard`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.getActiveBans()}
        SecurityContextHolder.getContext().authentication=UsernamePasswordAuthenticationToken("mod",null,emptyList())
        `when`(accounts.getUserByLogin("mod")).thenReturn(null)
        assertThrows<IllegalStateException> {controller.getBansWithAppeal()}
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertThrows<RankTooLowException> {controller.getAllBansOfUser(3)}
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(10))
        `when`(accounts.doesUserExist(3)).thenReturn(true)
        assertThrows<IllegalStateException> {controller.getBansByMod(3)}
    }

    @Test fun `current ban delegates and wraps service errors`() {
        controller.getCurrentUserBanData(3)
        verify(bans).getCurrentUserBan(3)
        doThrow(IllegalStateException("none")).`when`(bans).getCurrentUserBan(3)
        assertEquals("something_wrong. none",assertThrows<IllegalStateException> {controller.getCurrentUserBanData(3)}.message)
    }

    @Test fun `ban validates moderator input and target before saving`() {
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertEquals(HttpStatus.NOT_FOUND,controller.banUser(BanDto(3,1,"reason")).statusCode)
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(10))
        assertEquals(HttpStatus.BAD_REQUEST,controller.banUser(BanDto(3,null,"")).statusCode)
        `when`(accounts.doesUserExist(3)).thenReturn(true)
        assertThrows<IllegalStateException> {controller.banUser(BanDto(3,1,"reason"))}
        `when`(accounts.doesUserExist(3)).thenReturn(false)
        assertEquals(HttpStatus.OK,controller.banUser(BanDto(3,1,"reason")).statusCode)
        verify(bans).banUser(BanDto(3,1,"reason"),"mod")
    }

    @Test fun `appeal and cancellation report service failures and successes`() {
        assertEquals(HttpStatus.OK,controller.appealBan("reason").statusCode)
        doThrow(IllegalStateException("bad")).`when`(bans).appealBan("bad","mod")
        assertEquals(HttpStatus.BAD_REQUEST,controller.appealBan("bad").statusCode)
        assertEquals(HttpStatus.OK,controller.cancelBan(3).statusCode)
        doThrow(IllegalStateException("bad")).`when`(bans).cancelBan(4,"mod")
        assertEquals(HttpStatus.BAD_REQUEST,controller.cancelBan(4).statusCode)
        SecurityContextHolder.clearContext()
        assertEquals(HttpStatus.BAD_REQUEST,controller.cancelBan(3).statusCode)
    }

    @Test fun `exception handlers return their documented status`() {
        assertEquals(HttpStatus.BAD_REQUEST,controller.handleBasicException(IllegalStateException("bad")).statusCode)
        assertEquals(HttpStatus.FORBIDDEN,controller.handleRankTooLowException().statusCode)
    }

    private fun user(rank:Int)=User().apply {login="mod"; this.rank=Rank().apply {id=rank}}
}

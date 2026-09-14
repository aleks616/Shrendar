package org.aleks616.shrendar.userreport.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.exception.RankTooLowException
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.UserAccountService
import org.aleks616.shrendar.userreport.model.ReportRequestDto
import org.aleks616.shrendar.userreport.model.ReportsByUserDto
import org.aleks616.shrendar.userreport.model.UsersReportDto
import org.aleks616.shrendar.userreport.service.UserReportService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class UserReportControllerTest {
    private val accounts=mock(UserAccountService::class.java)
    private val limiter=mock(RateLimiter::class.java)
    private val reports=mock(UserReportService::class.java)
    private val controller=UserReportController(accounts,limiter,reports)
    private val request=mock(HttpServletRequest::class.java)

    @BeforeEach fun setup() {
        SecurityContextHolder.getContext().authentication=UsernamePasswordAuthenticationToken("mod",null,emptyList())
        `when`(request.remoteAddr).thenReturn("127.0.0.1")
        `when`(limiter.allowRequest(anyString(),anyInt(),eq(60))).thenReturn(true)
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(10))
    }
    @AfterEach fun clear() = SecurityContextHolder.clearContext()

    @Test fun `report user handles IP login reason and service paths`() {
        `when`(limiter.allowRequest(startsWith("reg:ip:"),anyInt(),eq(60))).thenReturn(false)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,controller.reportUser(ReportRequestDto(2,"reason"),request).statusCode)
        `when`(limiter.allowRequest(startsWith("reg:ip:"),anyInt(),eq(60))).thenReturn(true)
        `when`(limiter.allowRequest(startsWith("login:acct:"),anyInt(),eq(60))).thenReturn(false)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,controller.reportUser(ReportRequestDto(2,"reason"),request).statusCode)
        `when`(limiter.allowRequest(startsWith("login:acct:"),anyInt(),eq(60))).thenReturn(true)
        assertEquals(HttpStatus.BAD_REQUEST,controller.reportUser(ReportRequestDto(2,null),request).statusCode)
        doThrow(IllegalStateException("bad")).`when`(reports).reportUser(ReportRequestDto(2,"bad"),"mod")
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,controller.reportUser(ReportRequestDto(2,"bad"),request).statusCode)
        assertEquals(HttpStatus.OK,controller.reportUser(ReportRequestDto(2,"reason"),request).statusCode)
    }

    @Test fun `reportUser uses unknown for a missing remote IP address`() {
        `when`(request.remoteAddr).thenReturn(null)
        `when`(limiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

        assertEquals(HttpStatus.OK,controller.reportUser(ReportRequestDto(2,"reason"),request).statusCode)
        verify(limiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
    }

    @Test fun `reportUser throws when authentication is missing`() {
        SecurityContextHolder.clearContext()

        assertThrows<IllegalStateException> {
            controller.reportUser(ReportRequestDto(2,"reason"),request)
        }
    }

    @Test fun `getReportsByUserId throws when authentication is missing`() {
        SecurityContextHolder.clearContext()

        assertThrows<IllegalStateException> {controller.getReportsByUserId(2)}
    }

    @Test fun `getUnresolvedReports throws when authentication is missing`() {
        SecurityContextHolder.clearContext()

        assertThrows<IllegalStateException> {controller.getUnresolvedReports()}
    }

    @Test fun `resolve throws when authentication is missing`() {
        SecurityContextHolder.clearContext()

        assertThrows<IllegalStateException> {controller.resolve(1)}
    }

    @Test fun `can report delegates and protected endpoints enforce authentication and rank`() {
        `when`(reports.canReport(2,"mod")).thenReturn(true)
        assertTrue(controller.canReport(2))
        val expected=ReportsByUserDto(userId=2)
        `when`(reports.getUserReportsByUserId(2)).thenReturn(expected)
        `when`(reports.getNotResolvedReports()).thenReturn(listOf(UsersReportDto(id=1)))
        assertEquals(expected,controller.getReportsByUserId(2))
        assertEquals(1,controller.getUnresolvedReports().size)
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertThrows<RankTooLowException> {controller.getReportsByUserId(2)}
        assertThrows<RankTooLowException> {controller.getUnresolvedReports()}
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {controller.canReport(2)}
    }

    @Test fun `resolve enforces rank and reports service outcome`() {
        assertEquals(HttpStatus.OK,controller.resolve(1).statusCode)
        doThrow(IllegalStateException("bad")).`when`(reports).resolveReport(2)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,controller.resolve(2).statusCode)
        `when`(accounts.getUserByLogin("mod")).thenReturn(user(9))
        assertThrows<RankTooLowException> {controller.resolve(3)}
    }

    @Test fun `forbidden handler returns forbidden response`() {
        assertEquals(HttpStatus.FORBIDDEN,controller.handleForbiddenException(RankTooLowException("no")).statusCode)
    }

    private fun user(rank:Int)=User().apply {login="mod"; this.rank=Rank().apply {id=rank}}
}

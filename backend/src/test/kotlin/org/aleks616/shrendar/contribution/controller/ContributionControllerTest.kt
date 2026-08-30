package org.aleks616.shrendar.contribution.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.model.Table
import org.aleks616.shrendar.contribution.model.*
import org.aleks616.shrendar.contribution.service.ContributionRevertService
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.exception.*
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.user.service.UserAccountService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import kotlin.test.assertEquals

class ContributionControllerTest {
    private val contributionService=mock(ContributionService::class.java)
    private val revertService=mock(ContributionRevertService::class.java)
    private val rateLimiter=mock(RateLimiter::class.java)
    private val userAccountService=mock(UserAccountService::class.java)
    private val controller=ContributionController(contributionService,revertService,rateLimiter,userAccountService)
    private val request=mock(HttpServletRequest::class.java)

    @BeforeEach
    fun setup() {
        SecurityContextHolder.getContext().authentication=
            UsernamePasswordAuthenticationToken("user",null,emptyList())
        `when`(request.remoteAddr).thenReturn("127.0.0.1")
        `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_BASIC),eq(60))).thenReturn(true)
        `when`(userAccountService.doesUserExist(7)).thenReturn(true)
    }

    @AfterEach
    fun tearDown() = SecurityContextHolder.clearContext()

    @Test
    fun `getContributions should delegate to service`() {
        val contributions=listOf(Contribution())
        `when`(contributionService.getAll()).thenReturn(contributions)
        assertSame(contributions,controller.getContributions())
    }

    @Test
    fun `confirmContributionRequest should succeed`() {
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Confirmation successful",result.body)
        verify(contributionService).confirmDataChangeRequest(1,"user")
    }

    @Test
    fun `confirmContributionRequest should work if IP is unknown`() {
        `when`(request.remoteAddr).thenReturn(null)
        `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.OK,result.statusCode)
        verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
    }

    @Test
    fun `confirmContributionRequest should reject unauthenticated request`() {
        SecurityContextHolder.clearContext()
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(rateLimiter,contributionService)
    }

    @Test
    fun `confirmContributionRequest should return too many requests for IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this IP",result.body)
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `confirmContributionRequest should return too many requests for login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this user",result.body)
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `confirmContributionRequest should return forbidden for rank exception`() {
        doAnswer {throw RankTooLowToConfirmContributionException("rank")}
            .`when`(contributionService).confirmDataChangeRequest(1,"user")
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("RankTooLowToConfirmContributionException rank",result.body)
    }

    @Test
    fun `confirmContributionRequest should return internal server error for unexpected exception`() {
        doThrow(IllegalStateException("broken"))
            .`when`(contributionService).confirmDataChangeRequest(1,"user")
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("Something went wrong. broken",result.body)
    }

    @Test
    fun `revertAddRequest should succeed`() {
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Addition reverted successful",result.body)
        verify(revertService).revertAddition(1,"user")
    }

    @Test
    fun `revertAddRequest should work if IP is unknown`() {
        `when`(request.remoteAddr).thenReturn(null)
        `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.OK,result.statusCode)
        verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
    }

    @Test
    fun `revertAddRequest should reject unauthenticated request`() {
        SecurityContextHolder.clearContext()
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(rateLimiter,revertService)
    }

    @Test
    fun `revertAddRequest should return too many requests for IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this IP",result.body)
        verifyNoInteractions(revertService)
    }

    @Test
    fun `revertAddRequest should return too many requests for login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this user",result.body)
        verifyNoInteractions(revertService)
    }

    @Test
    fun `revertAddRequest should return forbidden for rank exception`() {
        doAnswer {throw RankTooLowToRevertConfirmedContributionException("rank")}
            .`when`(revertService).revertAddition(1,"user")
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("RankTooLowToRevertConfirmedContributionException rank",result.body)
    }

    @Test
    fun `revertAddRequest should return internal server error for unexpected exception`() {
        doThrow(IllegalStateException("broken")).`when`(revertService).revertAddition(1,"user")
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("Something went wrong. broken",result.body)
    }

    @Test
    fun `handleLimitExceededException should return bad request`() {
        val result=controller.handleLimitExceededException(IllegalStateException("limit"))
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Something went wrong. limit",result.body)
    }

    @Test
    fun `getContributionsByRequestingUser should return contributions`() {
        val expected=listOf(ContributionDto(userId=7))
        `when`(contributionService.getContributionsByRequestingUser(7)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByRequestingUser(7))
    }

    @Test
    fun `getContributionsByRequestingUser should throw IllegalStateException for unknown user`() {
        `when`(userAccountService.doesUserExist(7)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {controller.getContributionsByRequestingUser(7)}
        assertEquals("user with id 7 doesn't exist",exception.message)
    }


    @Test
    fun `getContributionsByRequestingUser should wrap service exception`() {
        `when`(contributionService.getContributionsByRequestingUser(7)).thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {controller.getContributionsByRequestingUser(7)}
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByConfirmingUser should delegate to service`() {
        val expected=listOf(ContributionDto(confirmedBy=7))
        `when`(contributionService.getContributionsByConfirmingUser(7)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByConfirmingUser(7))
    }

    @Test
    fun `getContributionsByConfirmingUser should throw IllegalStateException for unknown user`() {
        `when`(userAccountService.doesUserExist(7)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByConfirmingUser(7)
        }
        assertEquals("user with id 7 doesn't exist",exception.message)
    }

    @Test
    fun `getContributionsByTableName should throw Exception for non-existent table`() {
        val exception=assertThrows<IllegalArgumentException> {
            controller.getContributionsByTableName("other")
        }
        assertEquals("table \"other\" does not exist",exception.message)
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId should throw Exception for non-existent table`() {
        assertThrows<IllegalArgumentException> {
            controller.getContributionsByTableNameAndChangedRecordId("other",3)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId should throw Exception for non-existent table`() {
        val exception=assertThrows<IllegalArgumentException> {
            controller.getLastChangesByTableAndChangedRecordId("other",3)
        }
        assertEquals("table \"other\" does not exist",exception.message)
        verifyNoInteractions(contributionService)
    }


    @Test
    fun `getContributionsByChangedAtBetween should wrap service exception`() {
        val date=LocalDate.now()
        `when`(contributionService.getContributionsByChangedAtBetween(date,date))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByChangedAtBetween(date,date)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween should wrap service exception`() {
        val date=LocalDate.now()
        `when`(contributionService.getContributionsByRequestingUserAndChangedAtBetween(date,date,7))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(
                date,date,7
            )
        }
        assertEquals("broken",exception.message)
    }


    @Test
    fun `getContributionsByConfirmingUser should wrap service exception`() {
        `when`(contributionService.getContributionsByConfirmingUser(7))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByConfirmingUser(7)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByTableName should throw IllegalArgumentException for invalid table`() {
        assertThrows<IllegalArgumentException> {controller.getContributionsByTableName("unknown")}
    }

    @Test
    fun `getContributionsByTableName should wrap service exception`() {
        `when`(contributionService.getContributionsByTableName("artist"))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByTableName("artist")
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByTableName should delegate valid table`() {
        val expected=listOf(ContributionDto(changedTable="artist"))
        `when`(contributionService.getContributionsByTableName("artist")).thenReturn(expected)
        assertSame(expected,controller.getContributionsByTableName("artist"))
        verify(contributionService).getContributionsByTableName("artist")
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId should delegate valid table`() {
        val expected=listOf(ContributionDto(changedRecordId=3))
        `when`(contributionService.getContributionsByTableNameAndChangedRecordId("artist",3)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByTableNameAndChangedRecordId("artist",3))
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId should throw IllegalArgumentException for invalid table`() {
        assertThrows<IllegalArgumentException> {
            controller.getContributionsByTableNameAndChangedRecordId("unknown",3)
        }
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId should wrap service exception`() {
        `when`(contributionService.getContributionsByTableNameAndChangedRecordId("artist",3))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByTableNameAndChangedRecordId("artist",3)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId should delegate valid table`() {
        val expected=ContributionHistoryDto(table="artist")
        `when`(contributionService.getLastChangesByTableNameAndChangedRecordId("artist",3)).thenReturn(expected)
        assertSame(expected,controller.getLastChangesByTableAndChangedRecordId("artist",3))
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId should throw IllegalArgumentException for invalid table`() {
        assertThrows<IllegalArgumentException> {
            controller.getLastChangesByTableAndChangedRecordId("unknown",3)
        }
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId should wrap service exception`() {
        `when`(contributionService.getLastChangesByTableNameAndChangedRecordId("artist",3))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getLastChangesByTableAndChangedRecordId("artist",3)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByChangedAtBetween should throw IllegalStateException for reversed dates`() {
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByChangedAtBetween(LocalDate.of(2025,2,1),LocalDate.of(2025,1,1))
        }
        assertEquals("start date cannot be after end date",exception.message)
    }

    @Test
    fun `getContributionsByChangedAtBetween should delegate valid dates`() {
        val start=LocalDate.of(2025,1,1)
        val end=LocalDate.of(2025,2,1)
        val expected=listOf(ContributionDto())
        `when`(contributionService.getContributionsByChangedAtBetween(start,end)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByChangedAtBetween(start,end))
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween should throw IllegalStateException for unknown user`() {
        `when`(userAccountService.doesUserExist(7)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(LocalDate.now(),LocalDate.now(),7)
        }
        assertEquals("user with id 7 doesn't exist",exception.message)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween should delegate valid dates`() {
        val start=LocalDate.of(2025,1,1)
        val end=LocalDate.of(2025,2,1)
        val expected=listOf(ContributionDto())
        `when`(contributionService.getContributionsByRequestingUserAndChangedAtBetween(start,end,7)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByRequestingUserAndChangedAtBetween(start,end,7))
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween should throw IllegalStateException for reversed dates`() {
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(
                LocalDate.of(2025,2,1),LocalDate.of(2025,1,1),7
            )
        }
    }

    @Test
    fun `getContributionsByRequestingUserAndAction should delegate valid action`() {
        val expected=listOf(ContributionDto(action=Action.CREATE))
        `when`(contributionService.getContributionsByActionAndRequestingUser(7,Action.CREATE)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByRequestingUserAndAction(7,Action.CREATE))
    }

    @Test
    fun `getContributionsByRequestingUserAndAction should wrap service exception`() {
        `when`(contributionService.getContributionsByActionAndRequestingUser(7,Action.CREATE))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndAction(7,Action.CREATE)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByTableName should accept every supported table`() {
        Table.entries.forEach {table ->
            `when`(contributionService.getContributionsByTableName(table.name.lowercase())).thenReturn(emptyList())
            controller.getContributionsByTableName(table.name)
        }
        verify(contributionService,times(Table.entries.size)).getContributionsByTableName(anyString())
    }
}

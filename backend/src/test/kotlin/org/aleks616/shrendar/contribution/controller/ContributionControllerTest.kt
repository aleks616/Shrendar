package org.aleks616.shrendar.contribution.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.model.Table
import org.aleks616.shrendar.contribution.model.*
import org.aleks616.shrendar.contribution.service.ContributionRevertService
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.exception.*
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.user.service.UserService
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

class ContributionControllerTest {
    private val contributionService=mock(ContributionService::class.java)
    private val revertService=mock(ContributionRevertService::class.java)
    private val rateLimiter=mock(RateLimiter::class.java)
    private val userService=mock(UserService::class.java)
    private val controller=ContributionController(contributionService,revertService,rateLimiter,userService)
    private val request=mock(HttpServletRequest::class.java)

    @BeforeEach
    fun setup() {
        SecurityContextHolder.getContext().authentication=
            UsernamePasswordAuthenticationToken("user",null,emptyList())
        `when`(request.remoteAddr).thenReturn("127.0.0.1")
        `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_BASIC),eq(60))).thenReturn(true)
        `when`(userService.doesUserExist(7)).thenReturn(true)
    }

    @AfterEach
    fun tearDown() = SecurityContextHolder.clearContext()

    @Test
    fun `getContributions delegates to service`() {
        val contributions=listOf(Contribution())
        `when`(contributionService.getAll()).thenReturn(contributions)
        assertSame(contributions,controller.getContributions())
    }

    @Test
    fun `confirmContributionRequest succeeds`() {
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Confirmation successful",result.body)
        verify(contributionService).confirmDataChangeRequest(1,"user")
    }

    @Test
    fun `confirmContributionRequest rejects unauthenticated request`() {
        SecurityContextHolder.clearContext()
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(rateLimiter,contributionService)
    }

    @Test
    fun `confirmContributionRequest rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this IP",result.body)
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `confirmContributionRequest rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this user",result.body)
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `confirmContributionRequest maps rank exception`() {
        doAnswer {throw RankTooLowToConfirmContributionException("rank")}
            .`when`(contributionService).confirmDataChangeRequest(1,"user")
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("RankTooLowToConfirmContributionException rank",result.body)
    }

    @Test
    fun `confirmContributionRequest maps unexpected exception`() {
        doThrow(IllegalStateException("broken"))
            .`when`(contributionService).confirmDataChangeRequest(1,"user")
        val result=controller.confirmContributionRequest(1,request)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("Something went wrong. broken",result.body)
    }

    @Test
    fun `revertAddRequest succeeds`() {
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Addition reverted successful",result.body)
        verify(revertService).revertAddition(1,"user")
    }

    @Test
    fun `revertAddRequest rejects unauthenticated request`() {
        SecurityContextHolder.clearContext()
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(rateLimiter,revertService)
    }

    @Test
    fun `revertAddRequest rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this IP",result.body)
        verifyNoInteractions(revertService)
    }

    @Test
    fun `revertAddRequest rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this user",result.body)
        verifyNoInteractions(revertService)
    }

    @Test
    fun `revertAddRequest throws confirmed rank exception`() {
        doAnswer {throw RankTooLowToRevertConfirmedContributionException("rank")}
            .`when`(revertService).revertAddition(1,"user")
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("RankTooLowToRevertConfirmedContributionException rank",result.body)
    }

    @Test
    fun `revertAddRequest throws unexpected exception`() {
        doThrow(IllegalStateException("broken")).`when`(revertService).revertAddition(1,"user")
        val result=controller.revertAddRequest(1,request)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("Something went wrong. broken",result.body)
    }

    @Test
    fun `handleLimitExceededException returns bad request`() {
        val result=controller.handleLimitExceededException(IllegalStateException("limit"))
        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Something went wrong. limit",result.body)
    }

    @Test
    fun `getContributionsByRequestingUser succeeds`() {
        val expected=listOf(ContributionDto(userId=7))
        `when`(contributionService.getContributionsByRequestingUser(7)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByRequestingUser(7,request))
    }

    @Test
    fun `getContributionsByRequestingUser rejects unknown user`() {
        `when`(userService.doesUserExist(7)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {controller.getContributionsByRequestingUser(7,request)}
        assertEquals("user with id 7 doesn't exist",exception.message)
    }

    @Test
    fun `getContributionsByRequestingUser rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {controller.getContributionsByRequestingUser(7,request)}
        assertEquals("Too many requests from this user",exception.message)
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByRequestingUser rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUser(7,request)
        }
        assertEquals("Too many requests from this IP",exception.message)
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByRequestingUser rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUser(7,request)
        }
    }

    @Test
    fun `getContributionsByRequestingUser wraps service exception`() {
        `when`(contributionService.getContributionsByRequestingUser(7)).thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {controller.getContributionsByRequestingUser(7,request)}
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByConfirmingUser delegates to service`() {
        val expected=listOf(ContributionDto(confirmedBy=7))
        `when`(contributionService.getContributionsByConfirmingUser(7)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByConfirmingUser(7,request))
    }

    @Test
    fun `getContributionsByConfirmingUser rejects unknown user`() {
        `when`(userService.doesUserExist(7)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByConfirmingUser(7,request)
        }
        assertEquals("user with id 7 doesn't exist",exception.message)
    }

    @Test
    fun `getContributionsByConfirmingUser rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByConfirmingUser(7,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByConfirmingUser rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByConfirmingUser(7,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByConfirmingUser rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getContributionsByConfirmingUser(7,request)
        }
    }

    @Test
    fun `getContributionsByTableName rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByTableName("artist",request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getContributionsByTableNameAndChangedRecordId("artist",3,request)
        }
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByTableNameAndChangedRecordId("artist",3,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByTableNameAndChangedRecordId("artist",3,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getLastChangesByTableAndChangedRecordId("artist",3,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getLastChangesByTableAndChangedRecordId("artist",3,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getLastChangesByTableAndChangedRecordId("artist",3,request)
        }
    }

    @Test
    fun `getContributionsByChangedAtBetween rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByChangedAtBetween(LocalDate.now(),LocalDate.now(),request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByChangedAtBetween wraps service exception`() {
        val date=LocalDate.now()
        `when`(contributionService.getContributionsByChangedAtBetween(date,date))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByChangedAtBetween(date,date,request)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(
                LocalDate.now(),LocalDate.now(),7,request
            )
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(
                LocalDate.now(),LocalDate.now(),7,request
            )
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(
                LocalDate.now(),LocalDate.now(),7,request
            )
        }
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween wraps service exception`() {
        val date=LocalDate.now()
        `when`(contributionService.getContributionsByRequestingUserAndChangedAtBetween(date,date,7))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(
                date,date,7,request
            )
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByRequestingUserAndAction rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndAction(7,Action.CREATE,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByRequestingUserAndAction rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndAction(7,Action.CREATE,request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByRequestingUserAndAction rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndAction(7,Action.CREATE,request)
        }
    }

    @Test
    fun `getContributionsByConfirmingUser wraps service exception`() {
        `when`(contributionService.getContributionsByConfirmingUser(7))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByConfirmingUser(7,request)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByTableName rejects invalid table`() {
        assertThrows<IllegalArgumentException> {controller.getContributionsByTableName("unknown",request)}
    }

    @Test
    fun `getContributionsByTableName rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getContributionsByTableName("artist",request)
        }
    }

    @Test
    fun `getContributionsByTableName rejects IP timeout`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByTableName("artist",request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByTableName wraps service exception`() {
        `when`(contributionService.getContributionsByTableName("artist"))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByTableName("artist",request)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByTableName delegates valid table`() {
        val expected=listOf(ContributionDto(changedTable="artist"))
        `when`(contributionService.getContributionsByTableName("artist")).thenReturn(expected)
        assertSame(expected,controller.getContributionsByTableName("artist",request))
        verify(contributionService).getContributionsByTableName("artist")
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId delegates valid table`() {
        val expected=listOf(ContributionDto(changedRecordId=3))
        `when`(contributionService.getContributionsByTableNameAndChangedRecordId("artist",3)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByTableNameAndChangedRecordId("artist",3,request))
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId rejects invalid table`() {
        assertThrows<IllegalArgumentException> {
            controller.getContributionsByTableNameAndChangedRecordId("unknown",3,request)
        }
    }

    @Test
    fun `getContributionsByTableNameAndChangedRecordId wraps service exception`() {
        `when`(contributionService.getContributionsByTableNameAndChangedRecordId("artist",3))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByTableNameAndChangedRecordId("artist",3,request)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId delegates valid table`() {
        val expected=ContributionHistoryDto(table="artist")
        `when`(contributionService.getLastChangesByTableNameAndChangedRecordId("artist",3)).thenReturn(expected)
        assertSame(expected,controller.getLastChangesByTableAndChangedRecordId("artist",3,request))
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId rejects invalid table`() {
        assertThrows<IllegalArgumentException> {
            controller.getLastChangesByTableAndChangedRecordId("unknown",3,request)
        }
    }

    @Test
    fun `getLastChangesByTableAndChangedRecordId wraps service exception`() {
        `when`(contributionService.getLastChangesByTableNameAndChangedRecordId("artist",3))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getLastChangesByTableAndChangedRecordId("artist",3,request)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByChangedAtBetween rejects reversed dates`() {
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByChangedAtBetween(LocalDate.of(2025,2,1),LocalDate.of(2025,1,1),request)
        }
        assertEquals("start date cannot be after end date",exception.message)
    }

    @Test
    fun `getContributionsByChangedAtBetween delegates valid dates`() {
        val start=LocalDate.of(2025,1,1)
        val end=LocalDate.of(2025,2,1)
        val expected=listOf(ContributionDto())
        `when`(contributionService.getContributionsByChangedAtBetween(start,end)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByChangedAtBetween(start,end,request))
    }

    @Test
    fun `getContributionsByChangedAtBetween rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        assertThrows<IllegalStateException> {
            controller.getContributionsByChangedAtBetween(LocalDate.now(),LocalDate.now(),request)
        }
    }

    @Test
    fun `getContributionsByChangedAtBetween rejects login timeout`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)
        assertThrows<IllegalStateException> {
            controller.getContributionsByChangedAtBetween(LocalDate.now(),LocalDate.now(),request)
        }
        verifyNoInteractions(contributionService)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween rejects unknown user`() {
        `when`(userService.doesUserExist(7)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(LocalDate.now(),LocalDate.now(),7,request)
        }
        assertEquals("user with id 7 doesn't exist",exception.message)
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween delegates valid dates`() {
        val start=LocalDate.of(2025,1,1)
        val end=LocalDate.of(2025,2,1)
        val expected=listOf(ContributionDto())
        `when`(contributionService.getContributionsByRequestingUserAndChangedAtBetween(start,end,7)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByRequestingUserAndChangedAtBetween(start,end,7,request))
    }

    @Test
    fun `getContributionsByRequestingUserAndChangedAtBetween rejects reversed dates`() {
        assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndChangedAtBetween(
                LocalDate.of(2025,2,1),LocalDate.of(2025,1,1),7,request
            )
        }
    }

    @Test
    fun `getContributionsByRequestingUserAndAction delegates valid action`() {
        val expected=listOf(ContributionDto(action=Action.CREATE))
        `when`(contributionService.getContributionsByActionAndRequestingUser(7,Action.CREATE)).thenReturn(expected)
        assertSame(expected,controller.getContributionsByRequestingUserAndAction(7,Action.CREATE,request))
    }

    @Test
    fun `getContributionsByRequestingUserAndAction rejects invalid user`() {
        `when`(userService.doesUserExist(7)).thenReturn(false)
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndAction(7,Action.CREATE,request)
        }
        assertEquals("user with id 7 doesn't exist",exception.message)
    }

    @Test
    fun `getContributionsByRequestingUserAndAction wraps service exception`() {
        `when`(contributionService.getContributionsByActionAndRequestingUser(7,Action.CREATE))
            .thenThrow(IllegalArgumentException("broken"))
        val exception=assertThrows<IllegalStateException> {
            controller.getContributionsByRequestingUserAndAction(7,Action.CREATE,request)
        }
        assertEquals("broken",exception.message)
    }

    @Test
    fun `getContributionsByTableName accepts every supported table`() {
        Table.entries.forEach {table ->
            `when`(contributionService.getContributionsByTableName(table.name.lowercase())).thenReturn(emptyList())
            controller.getContributionsByTableName(table.name,request)
        }
        verify(contributionService,times(Table.entries.size)).getContributionsByTableName(anyString())
    }
}

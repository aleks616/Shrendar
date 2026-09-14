package org.aleks616.shrendar.event.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.event.model.EventAddDto
import org.aleks616.shrendar.event.service.EventService
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.userban.service.UserBanService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate

class EventControllerTest {

    @Nested
    inner class EventControllerUnitTest {
        private val eventService=mock(EventService::class.java)
        private val rateLimiter=mock(RateLimiter::class.java)
        private val userBanService=mock(UserBanService::class.java)
        private val bandService=mock(BandService::class.java)
        private val request=mock(HttpServletRequest::class.java)
        private val controller=EventController(rateLimiter,eventService,userBanService,bandService)
        private val dto=EventAddDto(bandId=1,date=LocalDate.of(2020,1,1),name="Concert",description="Description")

        @BeforeEach
        fun setupUnitTest() {
            SecurityContextHolder.getContext().authentication=
                UsernamePasswordAuthenticationToken("user",null,emptyList())
            `when`(request.remoteAddr).thenReturn("127.0.0.1")
            `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_BASIC),eq(60))).thenReturn(true)
            `when`(userBanService.isBanned("user")).thenReturn(false)
            `when`(bandService.doesBandExist(1)).thenReturn(true)
            `when`(eventService.doesEventExist(1)).thenReturn(true)
        }

        @Test
        fun `addEvent should return success`() {
            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(eventService).addEventRequest(dto,"user")
        }

        @Test
        fun `addEvent should return bad request when authentication is missing`() {
            SecurityContextHolder.clearContext()

            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `addEvent should return too many requests when IP rate limit is reached`() {
            `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            verify(rateLimiter,never()).allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `addEvent should return too many requests when login rate limit is reached`() {
            `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            assertEquals("Too many requests from this user",result.body)
        }

        @Test
        fun `addEvent should work if IP is unknown`() {
            `when`(request.remoteAddr).thenReturn(null)
            `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `addEvent should return forbidden when user is banned`() {
            `when`(userBanService.isBanned("user")).thenReturn(true)

            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        }

        @Test
        fun `addEvent should reject missing band id`() {
            val result=controller.addEvent(dto.copy(bandId=null),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(eventService)
        }

        @Test
        fun `addEvent should reject non-positive band id`() {
            val result=controller.addEvent(dto.copy(bandId=0),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(eventService)
        }

        @Test
        fun `addEvent should reject missing date`() {
            val result=controller.addEvent(dto.copy(date=null),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(eventService)
        }

        @Test
        fun `addEvent should reject empty name`() {
            val result=controller.addEvent(dto.copy(name=""),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(eventService)
        }

        @Test
        fun `addEvent should reject long name`() {
            val result=controller.addEvent(dto.copy(name="x".repeat(121)),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(eventService)
        }

        @Test
        fun `addEvent should reject missing band`() {
            val event=dto.copy(bandId=99)
            `when`(bandService.doesBandExist(99)).thenReturn(false)

            val result=controller.addEvent(event,request)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        }

        @Test
        fun `addEvent should return contribution limit error`() {
            doAnswer {throw ContributionLimitExceededException("limit reached")}
                .`when`(eventService).addEventRequest(dto,"user")

            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
            assertEquals("ContributionLimitExceededException limit reached",result.body)
        }

        @Test
        fun `addEvent should return unexpected error`() {
            doThrow(IllegalStateException("broken"))
                .`when`(eventService).addEventRequest(dto,"user")

            val result=controller.addEvent(dto,request)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
            assertEquals("An unexpected error occurred: broken",result.body)
        }

        @Test
        fun `editEvent should return success`() {
            val editDto=dto.copy(id=1)

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(eventService).editEventRequest(editDto,"user")
        }

        @Test
        fun `editEvent should return bad request when authentication is missing`() {
            SecurityContextHolder.clearContext()

            val result=controller.editEvent(dto.copy(id=1),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editEvent should return too many requests when IP rate limit is reached`() {
            `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.editEvent(dto.copy(id=1),request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            verify(rateLimiter,never()).allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `editEvent should work if IP is unknown`() {
            `when`(request.remoteAddr).thenReturn(null)
            `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

            val result=controller.editEvent(dto.copy(id=1),request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `editEvent should return too many requests when login rate limit is reached`() {
            `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.editEvent(dto.copy(id=1),request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        }

        @Test
        fun `editEvent should return forbidden when user is banned`() {
            `when`(userBanService.isBanned("user")).thenReturn(true)

            val result=controller.editEvent(dto.copy(id=1),request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        }

        @Test
        fun `editEvent should reject missing id`() {
            val result=controller.editEvent(dto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(eventService)
        }

        @Test
        fun `editEvent should reject nonexistent event`() {
            `when`(eventService.doesEventExist(99)).thenReturn(false)

            val result=controller.editEvent(dto.copy(id=99),request)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        }

        @Test
        fun `editEvent should reject missing band id`() {
            val editDto=dto.copy(id=1,bandId=null)

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editEvent should reject non-positive band id`() {
            val editDto=dto.copy(id=1,bandId=0)

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editEvent should reject missing date`() {
            val editDto=dto.copy(id=1,date=null)

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editEvent should reject empty name`() {
            val editDto=dto.copy(id=1,name="")

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editEvent should reject long name`() {
            val editDto=dto.copy(id=1,name="x".repeat(121))

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editEvent should reject missing band`() {
            val editDto=dto.copy(id=1,bandId=99)
            `when`(bandService.doesBandExist(99)).thenReturn(false)

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        }

        @Test
        fun `editEvent should return contribution limit error`() {
            val editDto=dto.copy(id=1)
            doAnswer {throw ContributionLimitExceededException("limit reached")}
                .`when`(eventService).editEventRequest(editDto,"user")

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        }

        @Test
        fun `editEvent should return unexpected error`() {
            val editDto=dto.copy(id=1)
            doThrow(IllegalStateException("broken"))
                .`when`(eventService).editEventRequest(editDto,"user")

            val result=controller.editEvent(editDto,request)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        }

        @Test
        fun `deleteEvent should return success`() {
            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(eventService).deleteEventRequest(1,"user")
        }

        @Test
        fun `deleteEvent should return bad request when authentication is missing`() {
            SecurityContextHolder.clearContext()

            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `deleteEvent should return too many requests when IP rate limit is reached`() {
            `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            verify(rateLimiter,never()).allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `deleteEvent should work if IP is unknown`() {
            `when`(request.remoteAddr).thenReturn(null)
            `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `deleteEvent should return too many requests when login rate limit is reached`() {
            `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        }

        @Test
        fun `deleteEvent should return forbidden when user is banned`() {
            `when`(userBanService.isBanned("user")).thenReturn(true)

            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        }

        @Test
        fun `deleteEvent should reject nonexistent event`() {
            `when`(eventService.doesEventExist(99)).thenReturn(false)

            val result=controller.deleteEvent(99,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `deleteEvent should return contribution limit error`() {
            doAnswer {throw ContributionLimitExceededException("limit reached")}
                .`when`(eventService).deleteEventRequest(1,"user")

            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        }

        @Test
        fun `deleteEvent should return unexpected error`() {
            doThrow(IllegalStateException("broken"))
                .`when`(eventService).deleteEventRequest(1,"user")

            val result=controller.deleteEvent(1,request)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        }

        @Test
        fun `validateEvent should accept valid event`() {
            assertNull(controller.validateEvent(dto))
        }

        @Test
        fun `validateEvent should reject missing band id`() {
            assertEquals(HttpStatus.BAD_REQUEST,controller.validateEvent(dto.copy(bandId=null))?.statusCode)
        }

        @Test
        fun `validateEvent should reject non-positive band id`() {
            assertEquals(HttpStatus.BAD_REQUEST,controller.validateEvent(dto.copy(bandId=0))?.statusCode)
        }

        @Test
        fun `validateEvent should reject missing date`() {
            assertEquals(HttpStatus.BAD_REQUEST,controller.validateEvent(dto.copy(date=null))?.statusCode)
        }

        @Test
        fun `validateEvent should reject empty name`() {
            assertEquals(HttpStatus.BAD_REQUEST,controller.validateEvent(dto.copy(name=""))?.statusCode)
        }

        @Test
        fun `validateEvent should reject long name`() {
            assertEquals(HttpStatus.BAD_REQUEST,controller.validateEvent(dto.copy(name="x".repeat(121)))?.statusCode)
        }

        @Test
        fun `validateEvent should reject date too far in future`() {
            assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.validateEvent(dto.copy(date=LocalDate.now().plusYears(3)))?.statusCode
            )
        }

        @Test
        fun `validateEvent should reject missing band`() {
            val invalid=dto.copy(bandId=99)
            `when`(bandService.doesBandExist(99)).thenReturn(false)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,controller.validateEvent(invalid)?.statusCode)
        }
    }
}

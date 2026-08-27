package org.aleks616.shrendar.artist.controller

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.service.CountryService
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.security.RateLimiter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class ArtistControllerTest {

    private val artistService:ArtistService=mock(ArtistService::class.java)
    private val countryService:CountryService=mock(CountryService::class.java)
    private val rateLimiter:RateLimiter=mock(RateLimiter::class.java)
    private val bandsMemberService=mock(BandsMemberService::class.java)
    private val artistController=ArtistController(artistService,rateLimiter,countryService,bandsMemberService)
    private val mockMvc:MockMvc=MockMvcBuilders.standaloneSetup(artistController).build()
    private val request=mock(HttpServletRequest::class.java)
    private val dto=ArtistAddDto(name="James Hetfield",gender='M')

    @BeforeEach
    fun setup() {
        SecurityContextHolder.getContext().authentication=
            UsernamePasswordAuthenticationToken("user",null,emptyList())
        `when`(request.remoteAddr).thenReturn("127.0.0.1")
        `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_BASIC),eq(60))).thenReturn(true)
        `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_HIGH),eq(60))).thenReturn(true)
    }

    @Test
    fun `getAll should return all artists`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getAll()).thenReturn(artists)

        mockMvc.get("/api/artist/")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getById should return artist for existing id`() {
        val artist=Artist().apply {id=1; name="James Hetfield"}
        `when`(artistService.getById(1)).thenReturn(artist)


        mockMvc.get("/api/artist/id/1") {}.andExpect {
            status {isOk()}
            content {json("{'id':1,'name':'James Hetfield'}")}
        }
    }

    @Test
    fun `getById should throw exception when service throws error`() {
        `when`(artistService.getById(999)).thenThrow(IllegalArgumentException("artist with id doesn't exist"))

        val exception=assertThrows<ServletException> {
            mockMvc.get("/api/artist/id/999")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: artist with id doesn't exist",
            exception.message
        )
    }

    @Test
    fun `getByIdWiki should return wiki data`() {
        val wikiData=ArtistWikiDto(name="James Hetfield")
        `when`(artistService.getByIdWiki(1)).thenReturn(wikiData)

        mockMvc.get("/api/artist/wiki/1")
            .andExpect {
                status {isOk()}
                content {json("{'name':'James Hetfield'}")}
            }
    }

    @Test
    fun `getByNameLike should return artists for valid name`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByNameLike("James")).thenReturn(artists)

        mockMvc.get("/api/artist/name") {
            param("name","James")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByNameLike should throw exception for short name`() {
        val exception=assertThrows<ServletException> {
            mockMvc.get("/api/artist/name?name=Ja")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: name has to be at least 3 characters",
            exception.message
        )
    }

    @Test
    fun `getByFirstName should return artists for valid name`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByFirstName("James")).thenReturn(artists)

        mockMvc.get("/api/artist/first-name") {
            param("name","James")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByFirstName should throw exception for short name`() {
        val exception=assertThrows<ServletException> {
            mockMvc.get("/api/artist/first-name") {
                param("name","H")
            }
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: name has to be at least 2 characters",
            exception.message
        )
    }

    @Test
    fun `getByLastName should return artists for valid name`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByLastName("Hetfield")).thenReturn(artists)

        mockMvc.get("/api/artist/last-name") {
            param("name","Hetfield")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByLastName should throw exception for short name`() {
        val exception=assertThrows<ServletException> {
            mockMvc.get("/api/artist/last-name") {
                param("name","d")
            }
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: name has to be at least 2 characters",
            exception.message
        )
    }

    @Test
    fun `getByBirthday should return artists for valid date`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByBirthday(8,3)).thenReturn(artists)

        mockMvc.get("/api/artist/birthday") {
            param("month","8")
            param("day","3")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByBirthday should throw exception for invalid date`() {
        val exception=assertThrows<ServletException> {
            mockMvc.get("/api/artist/birthday?month=13&day=1")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: invalid month or day",
            exception.message
        )
    }

    @Test
    fun `getByBirthdayBetween should return artists for valid dates`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByBirthdayBetween(1,1,12,31)).thenReturn(artists)

        mockMvc.get("/api/artist/birthdaybetween") {
            param("startMonth","1")
            param("startDay","1")
            param("endMonth","12")
            param("endDay","31")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByBirthdayBetween should throw exception for invalid dates`() {
        assertThrows<ServletException> {
            mockMvc.get("/api/artist/birthdaybetween?startMonth=13&startDay=1&endMonth=12&endDay=31")
        }
        assertThrows<ServletException> {
            mockMvc.get("/api/artist/birthdaybetween?startMonth=1&startDay=1&endMonth=1&endDay=32")
        }
    }

    @Test
    fun `getByBirthYear should return artists`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByBirthYear(1963)).thenReturn(artists)

        mockMvc.get("/api/artist/birthyear/1963")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getByBirthYearBetween should return artists`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByBirthYearBetween(1960,1970)).thenReturn(artists)

        mockMvc.get("/api/artist/birthyear/") {
            param("startYear","1960")
            param("endYear","1970")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getRecentArtistBirthdays should return artists`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getRecentBirthdays()).thenReturn(artists)

        mockMvc.get("/api/artist/recentBirthdays")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getByDeathDate should return artists for valid date`() {
        val artists=listOf(Artist().apply {id=2; name="Cliff Burton"})
        `when`(artistService.getByDeathDate(9,27)).thenReturn(artists)

        mockMvc.get("/api/artist/death") {
            param("month","9")
            param("day","27")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':2,'name':'Cliff Burton'}]")}
        }
    }

    @Test
    fun `getByDeathDate should throw exception for invalid date`() {
        val exception=assertThrows<ServletException> {
            mockMvc.get("/api/artist/death?month=2&day=30")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: invalid month or day",
            exception.message
        )
    }

    @Test
    fun `getRecentArtistDeathAnniversaries should return artists`() {
        val artists=listOf(Artist().apply {id=2; name="Cliff Burton"})
        `when`(artistService.getRecentDeathsAnniversaries()).thenReturn(artists)

        mockMvc.get("/api/artist/recentDeaths")
            .andExpect {
                status {isOk()}
                content {json("[{'id':2,'name':'Cliff Burton'}]")}
            }
    }

    @Test
    fun `getByCountry should return artists`() {
        val artists=listOf(Artist().apply {id=1; name="James Hetfield"})
        `when`(artistService.getByCountry(1)).thenReturn(artists)

        mockMvc.get("/api/artist/country/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `addArtist should return success`() {
        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(artistService).addArtistRequest(dto,"user")
    }

    @Test
    fun `addArtist should return bad request when name is missing`() {
        val result=artistController.addArtist(dto.copy(name=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `addArtist should return bad request when authentication is missing`() {
        SecurityContextHolder.clearContext()

        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addArtist should return too many requests when IP limit is reached`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        verify(rateLimiter,never()).allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)
    }

    @Test
    fun `addArtist should return too many requests when login limit is reached`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `addArtist should return validation error for invalid gender`() {
        val result=artistController.addArtist(dto.copy(gender='Z'),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `addArtist should return validation error for invalid country`() {
        `when`(countryService.doesCountryExist(99)).thenReturn(false)

        val result=artistController.addArtist(dto.copy(country=99),request)

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `addArtist should handle contribution limit exception`() {
        doAnswer {throw ContributionLimitExceededException("limit")}
            .`when`(artistService).addArtistRequest(dto,"user")

        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
    }

    @Test
    fun `addArtist should handle unexpected exception`() {
        doThrow(IllegalStateException("broken"))
            .`when`(artistService).addArtistRequest(dto,"user")

        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
    }

    @Test
    fun `addArtist should reject death date before artist is ten years old`() {
        val result=artistController.addArtist(
            dto.copy(
                birthDate=LocalDate.of(2000,1,1),
                deathDate=LocalDate.of(2005,1,1)
            ),
            request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addArtist should reject artist younger than ten years`() {
        val result=artistController.addArtist(
            dto.copy(birthDate=LocalDate.now().minusYears(5)),
            request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addArtist should reject death date in the future`() {
        val result=artistController.addArtist(
            dto.copy(deathDate=LocalDate.now().plusDays(1)),
            request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addArtist should accept death date that is not in the future`() {
        val result=artistController.addArtist(
            dto.copy(
                birthDate=LocalDate.of(1980,1,1),
                deathDate=LocalDate.of(2020,1,1)
            ),
            request
        )

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(artistService).addArtistRequest(
            dto.copy(
                birthDate=LocalDate.of(1980,1,1),
                deathDate=LocalDate.of(2020,1,1)
            ),
            "user"
        )
    }

    @Test
    fun `addArtist should accept artist who is at least ten years old`() {
        val validDto=dto.copy(birthDate=LocalDate.of(1980,1,1))

        val result=artistController.addArtist(validDto,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(artistService).addArtistRequest(validDto,"user")
    }

    @Test
    fun `addArtist should reject invalid image URL`() {
        val result=artistController.addArtist(dto.copy(artistImageUrl="not a url"),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addArtist should reject country id below one`() {
        val result=artistController.addArtist(dto.copy(country=0),request)

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        verifyNoInteractions(countryService)
    }

    @Test
    fun `editArtist should return success`() {
        val editDto=dto.copy(id=1)
        `when`(artistService.doesArtistExist(1)).thenReturn(true)

        val result=artistController.editArtist(editDto,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(artistService).editArtistRequest(editDto,"user")
    }

    @Test
    fun `editArtist should return bad request for missing required fields`() {
        val result=artistController.editArtist(dto.copy(id=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `editArtist should return bad request for missing artist`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(false)

        val result=artistController.editArtist(dto.copy(id=1),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editArtist should return too many requests when IP limit is reached`() {
        val editDto=dto.copy(id=1)
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=artistController.editArtist(editDto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        verify(artistService,never()).doesArtistExist(anyLong())
    }

    @Test
    fun `editArtist should return too many requests when login limit is reached`() {
        val editDto=dto.copy(id=1)
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=artistController.editArtist(editDto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        verify(artistService,never()).doesArtistExist(anyLong())
    }

    @Test
    fun `editArtist should handle contribution limit exception`() {
        val editDto=dto.copy(id=1)
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        doAnswer {throw ContributionLimitExceededException("limit")}
            .`when`(artistService).editArtistRequest(editDto,"user")

        val result=artistController.editArtist(editDto,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
    }

    @Test
    fun `editArtist should handle unexpected exception`() {
        val editDto=dto.copy(id=1)
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        doThrow(IllegalStateException("broken"))
            .`when`(artistService).editArtistRequest(editDto,"user")

        val result=artistController.editArtist(editDto,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
    }

    @Test
    fun `deleteArtist should return success`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(true)

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(artistService).deleteArtistRequest(1,"user")
    }

    @Test
    fun `deleteArtist should return bad request for missing artist`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(false)

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteArtist should return too many requests when IP limit is reached`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        verify(artistService,never()).doesArtistExist(anyLong())
    }

    @Test
    fun `deleteArtist should return too many requests when login limit is reached`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        verify(artistService,never()).doesArtistExist(anyLong())
    }

    @Test
    fun `deleteArtist should handle contribution limit exception`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        doAnswer {throw ContributionLimitExceededException("limit")}
            .`when`(artistService).deleteArtistRequest(1,"user")

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
    }

    @Test
    fun `deleteArtist should handle unexpected exception`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        doThrow(IllegalStateException("broken"))
            .`when`(artistService).deleteArtistRequest(1,"user")

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
    }

    @Test
    fun `favoriteArtist should return success`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(true)

        val result=artistController.favoriteArtist(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(artistService).toggleFavoriteArtist(1,"user")
    }

    @Test
    fun `favoriteArtist should return bad request for missing artist`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(false)

        val result=artistController.favoriteArtist(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `favoriteArtist should return too many requests when IP limit is reached`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=artistController.favoriteArtist(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        verify(artistService,never()).doesArtistExist(anyLong())
    }

    @Test
    fun `favoriteArtist should return too many requests when login limit is reached`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=artistController.favoriteArtist(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        verify(artistService,never()).doesArtistExist(anyLong())
    }
}

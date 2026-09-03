package org.aleks616.shrendar.artist.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.model.ArtistAnniversaryDto
import org.aleks616.shrendar.artist.model.ArtistGenreDto
import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.model.Country
import org.aleks616.shrendar.common.model.NameValue
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.common.service.CountryService
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.security.JwtUtil
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class ArtistControllerTest {

    private val artistService:ArtistService=mock(ArtistService::class.java)
    private val countryService:CountryService=mock(CountryService::class.java)
    private val rateLimiter:RateLimiter=mock(RateLimiter::class.java)
    private val bandsMemberService=mock(BandsMemberService::class.java)

    private val bandService=mock(BandService::class.java)
    private val artistController=ArtistController(
        artistService,
        rateLimiter,
        countryService,
        bandsMemberService,
        bandService
    )
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
        val artists=listOf(ArtistAnniversaryDto(id=1, name="James Hetfield"))
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
    fun `getArtistBands should return a list of artist bands`() {
        val artistBands=listOf(
            org.aleks616.shrendar.band.model.ArtistBandsStatusDto(
                artistId=1,
                artistName="James Hetfield",
                bandId=3,
                bandName="Metallica",
                current=true
            )
        )
        `when`(bandsMemberService.getArtistBandsList(1L)).thenReturn(artistBands)

        mockMvc.get("/api/artist/1/bands-data")
            .andExpect {
                status {isOk()}
                content { json("[{\"artistId\":1,\"artistName\":\"James Hetfield\",\"bandId\":3,\"bandName\":\"Metallica\",\"current\":true}]") }
            }
    }

    @Test
    fun `getArtistGenres should return an artist genre dto`() {
        val genreDto=ArtistGenreDto(
            artistId=1,
            artistName="James Hetfield",
            genres=listOf(NameValue("Thrash Metal", 1))
        )
        `when`(artistService.getArtistGenres(1L)).thenReturn(genreDto)

        mockMvc.get("/api/artist/genres/1")
            .andExpect {
                status {isOk()}
                content { json("{\"artistId\":1,\"artistName\":\"James Hetfield\",\"genres\":[{\"name\":\"Thrash Metal\",\"value\":1}]}" ) }
            }
    }

    @Test
    fun `getByDeathDate should return artists for valid date`() {
        val artists=listOf(ArtistAnniversaryDto(id=2,name="Cliff Burton"))
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
    fun `addArtist should work if IP is unknown`() {
        `when`(request.remoteAddr).thenReturn(null)
        `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
    }

    @Test
    fun `addArtist should return bad request when authentication is missing`() {
        SecurityContextHolder.clearContext()

        val result=artistController.addArtist(dto,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addArtist should return bad request when name is missing`() {
        val result=artistController.addArtist(dto.copy(name=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
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
    fun `editArtist should return bad request when authentication is missing`() {
        SecurityContextHolder.clearContext()

        val result=artistController.editArtist(dto.copy(id=1),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editArtist should work if IP is unknown`() {
        `when`(request.remoteAddr).thenReturn(null)
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

        val result=artistController.editArtist(dto.copy(id=1),request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
    }

    @Test
    fun `editArtist should return bad request for missing id`() {
        val result=artistController.editArtist(dto.copy(id=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `editArtist should return bad request for missing name`() {
        val result=artistController.editArtist(dto.copy(id=1,name=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `editArtist should return bad request for empty name`() {
        val result=artistController.editArtist(dto.copy(id=1,name=""),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `editArtist should return bad request for missing gender`() {
        val result=artistController.editArtist(dto.copy(id=1,gender=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(artistService)
    }

    @Test
    fun `editArtist should return bad request for non-existent artist`() {
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
    fun `deleteArtist should return bad request when authentication is missing`() {
        SecurityContextHolder.clearContext()

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteArtist should work if IP is unknown`() {
        `when`(request.remoteAddr).thenReturn(null)
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)).thenReturn(true)

        val result=artistController.deleteArtist(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_BASIC,60)
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
    fun `favoriteArtist should return bad request when authentication is missing`() {
        SecurityContextHolder.clearContext()

        val result=artistController.favoriteArtist(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `favoriteArtist should work if IP is unknown`() {
        `when`(request.remoteAddr).thenReturn(null)
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_HIGH,60)).thenReturn(true)

        val result=artistController.favoriteArtist(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_HIGH,60)
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

    @Test
    fun `favoriteArtist should handle unexpected exception`() {
        `when`(artistService.doesArtistExist(1)).thenReturn(true)
        doThrow(IllegalStateException("broken"))
            .`when`(artistService).toggleFavoriteArtist(1,"user")

        val result=artistController.favoriteArtist(1,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    inner class ArtistControllerIntegrationTest {

        @Autowired
        private lateinit var mockMvc: MockMvc

        @Autowired
        private lateinit var artistRepository: ArtistRepository

        @Autowired
        private lateinit var contributionRepository: ContributionRepository

        @Autowired
        private lateinit var userRepository: UserRepository

        @Autowired
        private lateinit var rankRepository: RankRepository

        @Autowired
        private lateinit var countryRepository: CountryRepository

        @Autowired
        private lateinit var rateLimiter: RateLimiter

        private val objectMapper = ObjectMapper().findAndRegisterModules()
        private lateinit var userToken: String

        @BeforeEach
        fun setupIntegration() {
            contributionRepository.deleteAll()
            artistRepository.deleteAll()
            userRepository.deleteAll()
            rankRepository.deleteAll()
            countryRepository.deleteAll()

            rankRepository.saveAndFlush(Rank().apply { id=1; name="Newbie"; minXp=0; allowedContributions=10 })
            userRepository.saveAndFlush(User().apply {
                login="user"
                username="User"
                email="user@example.com"
                passwordHash="hash"
                rank=rankRepository.findById(1).get()
                verified=true
            })
            userToken = JwtUtil.createToken("user")

            val adminUser = userRepository.findAll().first()
            contributionRepository.saveAndFlush(Contribution().apply {
                changeId=0
                user=adminUser
                action=Action.CREATE
                changedTable="seed"
                changedColumn="seed"
                newValue="seed"
                changedAt=java.time.LocalDateTime.now()
                confirmed=true
            })

            val storageField = RateLimiter::class.java.getDeclaredField("storage")
            storageField.isAccessible = true
            (storageField.get(rateLimiter) as MutableMap<*, *>).clear()
        }

        @Test
        fun `addArtist should work for authorized user`() {
            val countryId = countryRepository.saveAndFlush(Country().apply { name="USA" }).id!!
            val artistAddDto = ArtistAddDto(
                name="James Hetfield",
                birthDate=LocalDate.of(1963,8,3),
                gender='M',
                country=countryId,
                description="Metallica frontman"
            )

            mockMvc.post("/api/artist/add") {
                header("Authorization", "Bearer $userToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(artistAddDto)
            }.andExpect {
                status { isOk() }
                content { string("Artist addition request received") }
            }

            val artist = artistRepository.findAll().find { it.name == "James Hetfield" }
            assertNotNull(artist)

            val changeId = contributionRepository.findAll().find { it.changedTable == "artist" }?.changeId
            assertNotNull(changeId)
        }

        @Test
        fun `addArtist should return unauthorized for missing token`() {
            val artistAddDto = ArtistAddDto(name="Unknown")

            mockMvc.post("/api/artist/add") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(artistAddDto)
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `addArtist should return too many requests when rate limit reached`() {
            val artistAddDto = ArtistAddDto(name="Fast Artist")

            repeat(Utils.LIMIT_BASIC) {
                mockMvc.post("/api/artist/add") {
                    header("Authorization", "Bearer $userToken")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(artistAddDto)
                }
            }

            mockMvc.post("/api/artist/add") {
                header("Authorization", "Bearer $userToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(artistAddDto)
            }.andExpect {
                status { isTooManyRequests() }
            }
        }

        @Test
        fun `addArtist should fail when user reaches weekly contribution limit`() {
            val rank = rankRepository.findById(1).get()
            rank.allowedContributions = 0
            rankRepository.saveAndFlush(rank)

            val artistAddDto = ArtistAddDto(name="Limited Artist")

            mockMvc.post("/api/artist/add") {
                header("Authorization", "Bearer $userToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(artistAddDto)
            }.andExpect {
                status { isForbidden() }
                content { string("ContributionLimitExceededException You have reached your weekly limit. Limit for rank 1 is 0") }
            }
        }
    }
}

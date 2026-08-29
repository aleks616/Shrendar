package org.aleks616.shrendar.band.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.model.Country
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
import org.junit.jupiter.api.Assertions.assertNull
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
import kotlin.jvm.java

class BandControllerTest {

    private val bandService:BandService=mock(BandService::class.java)
    private val bandsMemberService:BandsMemberService=mock(BandsMemberService::class.java)
    private val countryService:CountryService=mock(CountryService::class.java)
    private val artistService:ArtistService=mock(ArtistService::class.java)
    private val rateLimiter:RateLimiter=mock(RateLimiter::class.java)
    private val bandController=BandController(bandService,bandsMemberService,rateLimiter,countryService,artistService)
    private val mockMvc:MockMvc=MockMvcBuilders.standaloneSetup(bandController).build()
    private val request=mock(HttpServletRequest::class.java)
    private val validBandDto=BandAddDto(name="Metallica",formedYear=1981,status=Status.ACTIVE,country=1,imageUrl=null)

    @BeforeEach
    fun setup() {
        SecurityContextHolder.getContext().authentication=
            UsernamePasswordAuthenticationToken("user",null,emptyList())
        `when`(request.remoteAddr).thenReturn("127.0.0.1")
        `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_BASIC),eq(60))).thenReturn(true)
        `when`(rateLimiter.allowRequest(anyString(),eq(20),eq(120))).thenReturn(true)
        `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_HIGH),eq(60))).thenReturn(true)
        `when`(countryService.doesCountryExist(1)).thenReturn(true)
        `when`(artistService.doesArtistExist(1L)).thenReturn(true)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {birthDate=LocalDate.of(1970,1,1)})
        `when`(bandService.doesBandExist(1)).thenReturn(true)
        `when`(bandService.doesBandExist(2)).thenReturn(true)
    }

    @Test
    fun `getAll should return all bands`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getAll()).thenReturn(bands)

        mockMvc.get("/api/band/")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica'}]")}
            }
    }

    @Test
    fun `getBand should return band by id`() {
        val band=BandDto(id=1,name="Metallica")
        `when`(bandService.getBandDataById(1)).thenReturn(band)

        mockMvc.get("/api/band/id/1")
            .andExpect {
                status {isOk()}
                content {json("{'id':1,'name':'Metallica'}")}
            }
    }

    @Test
    fun `getBandByIdWiki should return wiki data`() {
        val wikiData=BandWikiDto(name="Metallica")
        `when`(bandService.getBandByIdWiki(1)).thenReturn(wikiData)

        mockMvc.get("/api/band/wiki/1")
            .andExpect {
                status {isOk()}
                content {json("{'name':'Metallica'}")}
            }
    }

    @Test
    fun `getAllBandMembersWiki should return wiki members`() {
        val members=listOf(BandsMembersWikiDto(id=1,artistName="James Hetfield"))
        `when`(bandsMemberService.getAllBandMembersWiki(1)).thenReturn(members)

        mockMvc.get("/api/band/wiki/1/members")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getAllMembersOfBand should return members`() {
        val members=listOf(BandsMembersDto(id=1,artistName="James Hetfield"))
        `when`(bandsMemberService.getAllBandMembers(1)).thenReturn(members)

        mockMvc.get("/api/band/1/members")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getCurrentBandMembers should return current members`() {
        val members=listOf(BandsMembersDto(id=1,artistName="James Hetfield"))
        `when`(bandsMemberService.getCurrentBandMembers(1)).thenReturn(members)

        mockMvc.get("/api/band/1/members/current")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getPastBandMembers should return past members`() {
        val members=listOf(BandsMembersDto(id=2,artistName="Cliff Burton"))
        `when`(bandsMemberService.getPastBandMembers(1)).thenReturn(members)

        mockMvc.get("/api/band/1/members/past")
            .andExpect {
                status {isOk()}
                content {json("[{'id':2,'artistName':'Cliff Burton'}]")}
            }
    }

    @Test
    fun `getBandByNameLike should return matching bands`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getBandsByName("Meta")).thenReturn(bands)

        mockMvc.get("/api/band/name-like/Meta")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica'}]")}
            }
    }

    @Test
    fun `getBandsByNameExact should return exactly matching bands`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getBandsByNameExact("Metallica")).thenReturn(bands)

        mockMvc.get("/api/band/name-exact/Metallica")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica'}]")}
            }
    }

    @Test
    fun `getBandsByCountryName should return bands by country name`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getBandsByCountry("USA")).thenReturn(bands)

        mockMvc.get("/api/band/country") {
            param("name","USA")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'Metallica'}]")}
        }
    }

    @Test
    fun `getBandsByCountryId should return bands by country id`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getBandsByCountryId(1)).thenReturn(bands)

        mockMvc.get("/api/band/country/") {
            param("id","1")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'Metallica'}]")}
        }
    }

    @Test
    fun `getBandsByFoundedBetween should return bands for valid range`() {
        val bands=listOf(BandDto(id=1,name="Metallica",formedYear=1981))
        `when`(bandService.getBandsByFoundedBetween(1980,1990)).thenReturn(bands)

        mockMvc.get("/api/band/foundedBetween") {
            param("startYear","1980")
            param("endYear","1990")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'Metallica','formedYear':1981}]")}
        }
    }

    @Test
    fun `getBandsByFoundedBetween should reject null start and end both`() {
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: startYear and endYear cannot both be null",
            ex.message
        )
    }

    @Test
    fun `getBandsByFoundedBetween should reject reversed range`() {
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween?startYear=1990&endYear=1980")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: startYear cannot be greater than endYear",
            ex.message
        )
    }

    @Test
    fun `getBandsByFoundedBetween should reject future start year`() {
        val currentYear=LocalDate.now().year
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween?startYear=${currentYear+1}")
        }
        assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid startYear",ex.message)
    }

    @Test
    fun `getBandsByFoundedBetween should reject future end year`() {
        val currentYear=LocalDate.now().year
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween?endYear=${currentYear+1}")
        }
        assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid endYear",ex.message)
    }

    @Test
    fun `getBandsByStatus should return active band status`() {
        val bands=listOf(BandDto(id=1,name="Metallica",status=Status.ACTIVE))
        `when`(bandService.getBandsByStatus(Status.ACTIVE)).thenReturn(bands)

        mockMvc.get("/api/band/status/active")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica','status':'ACTIVE'}]")}
            }
    }

    @Test
    fun `getBandsByStatus should accept capitalized status`() {
        val bands=listOf(BandDto(id=1,name="Metallica",status=Status.ACTIVE))
        `when`(bandService.getBandsByStatus(Status.ACTIVE)).thenReturn(bands)

        mockMvc.get("/api/band/status/Active")
            .andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus should return disbanded band status`() {
        val bands=listOf(BandDto(id=2,name="Slayer",status=Status.DISBANDED))
        `when`(bandService.getBandsByStatus(Status.DISBANDED)).thenReturn(bands)

        mockMvc.get("/api/band/status/disbanded")
            .andExpect {
                status {isOk()}
                content {json("[{'id':2,'name':'Slayer','status':'DISBANDED'}]")}
            }
    }

    @Test
    fun `getBandsByStatus should handle on hold format`() {
        `when`(bandService.getBandsByStatus(Status.ON_HOLD)).thenReturn(emptyList())

        mockMvc.get("/api/band/status/on_hold").andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus should handle on hold with space format`() {
        `when`(bandService.getBandsByStatus(Status.ON_HOLD)).thenReturn(emptyList())

        mockMvc.get("/api/band/status/on hold").andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus should reject invalid status`() {
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/status/invalid")
        }
        assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid status",ex.message)
    }

    @Test
    fun `getBandsByArtistId should return bands for artist`() {
        val history=listOf(ArtistBandsHistoryDto(id=1,artistName="James Hetfield",bandName="Metallica"))
        `when`(bandsMemberService.getBandsByArtistId(1)).thenReturn(history)

        mockMvc.get("/api/band/artist/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield','bandName':'Metallica'}]")}
            }
    }

    @Test
    fun `getSimilarBands should return similar bands`() {
        val similar=listOf(BandGenreDto(id=2,name="Megadeth",similarity=0.9))
        `when`(bandService.getSimilarBands(1,5)).thenReturn(similar)

        mockMvc.get("/api/band/similar/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':2,'name':'Megadeth','similarity':0.9}]")}
            }
    }

    @Test
    fun `getSimilarBands should use default quantity when not provided`() {
        val similar=listOf(BandGenreDto(id=2,name="Megadeth",similarity=0.9))
        `when`(bandService.getSimilarBands(1,5)).thenReturn(similar)

        mockMvc.get("/api/band/similar/1")
            .andExpect {
                status {isOk()}
            }

        verify(bandService).getSimilarBands(1,5)
    }

    @Test
    fun `addBandRequest should reject missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandRequest should return too many requests for IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this IP",result.body)
    }

    @Test
    fun `addBandRequest should return too many requests for login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this user",result.body)
    }

    @Test
    fun `addBandRequest should reject missing required fields`() {
        val result=bandController.addBandRequest(BandAddDto(name="",status=null,imageUrl=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("At least band name and status are required to add a new band",result.body)
    }

    @Test
    fun `addBandRequest should reject future formed year`() {
        val result=bandController.addBandRequest(
            BandAddDto(name="Metallica",formedYear=LocalDate.now().year+1,status=Status.ACTIVE,imageUrl=null),
            request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band formed year cannot be in the future",result.body)
    }

    @Test
    fun `addBandRequest should succeed`() {
        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band addition request received",result.body)
        verify(bandService).addBandRequest(validBandDto,"user")
    }

    @Test
    fun `editBand should reject missing id`() {
        val result=bandController.editBand(BandAddDto(name="Metallica",status=Status.ACTIVE,imageUrl=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBand should return too many requests for IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBand should return too many requests for login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBand should reject non existing band`() {
        `when`(bandService.doesBandExist(1)).thenReturn(false)

        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band with id 1 does not exist",result.body)
    }

    @Test
    fun `editBand should succeed`() {
        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band edit request received",result.body)
        verify(bandService).editBandRequest(validBandDto.copy(id=1),"user")
    }

    @Test
    fun `deleteBand should reject missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteBand should return too many requests for IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBand should return too many requests for login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBand should reject non existing band`() {
        `when`(bandService.doesBandExist(1)).thenReturn(false)

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band with id 1 does not exist",result.body)
    }

    @Test
    fun `deleteBand should succeed`() {
        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band deletion request received",result.body)
        verify(bandService).deleteBandRequest(1,"user")
    }

    @Test
    fun `addBandMembersRequest should reject missing authentication`() {
        SecurityContextHolder.clearContext()
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest should return too many requests for IP rate limit`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",20,120)).thenReturn(false)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest should return too many requests for login rate limit`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("login:acct:user",20,120)).thenReturn(false)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest should reject missing required member info`() {
        val result=
            bandController.addBandMembersRequest(ArtistBandAddDto(artistId=1L,bandId=2,role=null,joinedYear=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest should reject duplicate member`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesSameMemberExist(member)).thenReturn(true)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Member with id, role and joined year or left year already exists",result.body)
    }

    @Test
    fun `addBandMembersRequest should succeed`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesSameMemberExist(member)).thenReturn(false)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandsMemberService).addBandMemberRequest(member,"user")
    }

    @Test
    fun `memberValidate should reject artist younger than 10 at join`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=2000)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {birthDate=LocalDate.of(1999,1,1)})

        val result=bandController.memberValidate(member)

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Artist has to be at least 10 years old when joining the band",result?.body)
    }

    @Test
    fun `memberValidate should reject invalid left year ordering`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=2005,leftYear=2004)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {
            birthDate=LocalDate.of(1970,1,1)
            deathDate=LocalDate.of(2000,1,1)
        })

        val result=bandController.memberValidate(member)

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Left year has to be the same or greater than joined year",result?.body)
    }

    @Test
    fun `editBandMembersRequest should reject missing id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981),
            request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should return too many requests for IP rate limit`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",20,120)).thenReturn(false)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should return too many requests for login rate limit`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("login:acct:user",20,120)).thenReturn(false)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should reject missing band member`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(false)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should succeed`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandsMemberService).editBandMemberRequest(member,"user")
    }

    @Test
    fun `deleteBandMembersRequest should reject missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest should return too many requests for IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest should return too many requests for login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest should reject non existing band member`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(false)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest should succeed`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(true)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandsMemberService).deleteBandMemberRequest(1L,"user")
    }

    @Test
    fun `favoriteBand should reject missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `favoriteBand should return too many requests for IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `favoriteBand should return too many requests for login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `favoriteBand should reject non existing band`() {
        `when`(bandService.doesBandExist(1)).thenReturn(false)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `favoriteBand should succeed`() {
        `when`(bandService.doesBandExist(1)).thenReturn(true)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band favorite toggled successfully",result.body)
        verify(bandService).toggleFavoriteBand(1,"user")
    }

    @Test
    fun `bandValidate should reject future formed year`() {
        val result=bandController.bandValidate(
            BandAddDto(
                name="Metallica",
                formedYear=LocalDate.now().year+1,
                status=Status.ACTIVE,
                imageUrl=null
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band formed year cannot be in the future",result?.body)
    }

    @Test
    fun `bandValidate should reject disbanded status without disbanded year`() {
        val result=bandController.bandValidate(BandAddDto(name="Metallica",status=Status.DISBANDED,imageUrl=null))

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Disbanded year is required if band status is disbanded",result?.body)
    }

    @Test
    fun `bandValidate should reject unknown country`() {
        `when`(countryService.doesCountryExist(99)).thenReturn(false)

        val result=bandController.bandValidate(BandAddDto(name="Metallica",status=Status.ACTIVE,country=99,imageUrl=null))

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Country with id 99 does not exist",result?.body)
    }

    @Test
    fun `bandValidate should reject invalid image URL`() {
        val result=bandController.bandValidate(BandAddDto(name="Metallica",status=Status.ACTIVE,imageUrl="bad-url"))

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Image url can't be more than 255 characters and has to be valid URL",result?.body)
    }

    @Test
    fun `getBandsByFoundedBetween should accept null end year`() {
        `when`(bandService.getBandsByFoundedBetween(1980,null)).thenReturn(emptyList())

        val result=bandController.getBandsByFoundedBetween(1980,null)

        assertEquals(emptyList<BandDto>(),result)
        verify(bandService).getBandsByFoundedBetween(1980,null)
    }

    @Test
    fun `statusStringToEnum should reject unknown status`() {
        val ex=assertThrows<IllegalArgumentException> {
            bandController.statusStringToEnum("not-a-status")
        }

        assertEquals("invalid status",ex.message)
    }

    @Test
    fun `getSimilarBands should use default quantity for null`() {
        `when`(bandService.getSimilarBands(1,5)).thenReturn(emptyList())

        val result=bandController.getSimilarBands(1,null)

        assertEquals(emptyList<BandGenreDto>(),result)
        verify(bandService).getSimilarBands(1,5)
    }

    @Test
    fun `addBandRequest should handle contribution limit exception`() {
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandService).addBandRequest(validBandDto,"user")

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `addBandRequest should handle unexpected exception`() {
        `when`(bandService.addBandRequest(validBandDto,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `editBand should reject missing name`() {
        val result=bandController.editBand(validBandDto.copy(id=1,name=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band id, name and status are required",result.body)
    }

    @Test
    fun `editBand should reject missing status`() {
        val result=bandController.editBand(validBandDto.copy(id=1,status=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band id, name and status are required",result.body)
    }

    @Test
    fun `editBand should accept valid band validation`() {
        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandService).editBandRequest(validBandDto.copy(id=1),"user")
    }

    @Test
    fun `editBand should handle contribution limit exception`() {
        val band=validBandDto.copy(id=1)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandService).editBandRequest(band,"user")

        val result=bandController.editBand(band,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `editBand should handle unexpected exception`() {
        val band=validBandDto.copy(id=1)
        `when`(bandService.editBandRequest(band,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.editBand(band,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `deleteBand should handle contribution limit exception`() {
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandService).deleteBandRequest(1,"user")

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `deleteBand should handle unexpected exception`() {
        `when`(bandService.deleteBandRequest(1,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `addBandMembersRequest should reject missing artist id`() {
        val result=bandController.addBandMembersRequest(
            ArtistBandAddDto(artistId=null,bandId=2,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest should reject missing band id`() {
        val result=bandController.addBandMembersRequest(
            ArtistBandAddDto(artistId=1L,bandId=null,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest should reject missing joined year`() {
        val result=bandController.addBandMembersRequest(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=null),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest should handle contribution limit exception`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandsMemberService).addBandMemberRequest(member,"user")

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `addBandMembersRequest should handle unexpected exception`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandsMemberService.addBandMemberRequest(member,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `editBandMembersRequest should reject missing member id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=null,artistId=1L,bandId=2,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should reject missing artist id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=null,bandId=2,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should reject missing band id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=1L,bandId=null,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should reject missing role`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role=null,joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest should reject missing joined year`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=null),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `memberValidate should return null for valid member`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)

        assertNull(bandController.memberValidate(member))
    }

    @Test
    fun `editBandMembersRequest should reject invalid member validation`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=LocalDate.now().year+1)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Joined year can't be in the future",result.body)
    }

    @Test
    fun `editBandMembersRequest should handle contribution limit exception`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandsMemberService).editBandMemberRequest(member,"user")

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `editBandMembersRequest should handle unexpected exception`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)
        `when`(bandsMemberService.editBandMemberRequest(member,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `deleteBandMembersRequest should handle contribution limit exception`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(true)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandsMemberService).deleteBandMemberRequest(1L,"user")

        val result=bandController.deleteBandMembersRequest(1L,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `deleteBandMembersRequest should handle unexpected exception`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(true)
        doAnswer { throw IllegalStateException("boom") }
            .`when`(bandsMemberService).deleteBandMemberRequest(1L,"user")

        val result=bandController.deleteBandMembersRequest(1L,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `favoriteBand should handle unexpected exception`() {
        doAnswer { throw IllegalStateException("boom") }
            .`when`(bandService).toggleFavoriteBand(1,"user")

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `bandValidate should reject formed year before 1901`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=1900,status=Status.ACTIVE,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band formed year cannot be before 1901",result?.body)
    }

    @Test
    fun `bandValidate should reject disbanded year before 1901`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=1901,disbandedYear=1900,status=Status.DISBANDED,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band disbanded year cannot be before 1901",result?.body)
    }

    @Test
    fun `bandValidate should reject disbanded year before formed year`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=2000,disbandedYear=1999,status=Status.DISBANDED,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band disbanded year cannot be before formed year",result?.body)
    }

    @Test
    fun `bandValidate should reject non disbanded status with disbanded year`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=1981,disbandedYear=2000,status=Status.ACTIVE,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band status must be disbanded if disbanded year is provided",result?.body)
    }

    @Test
    fun `bandValidate should accept valid disbanded band`() {
        val result=bandController.bandValidate(
            BandAddDto(
                name="Metallica",
                formedYear=1981,
                disbandedYear=2000,
                status=Status.DISBANDED,
                imageUrl="https://example.com"
            )
        )

        assertNull(result)
    }

    @Test
    fun `memberValidate should reject future left year`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981,leftYear=LocalDate.now().year+1)

        val result=bandController.memberValidate(member)

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Left year can't be in the future",result?.body)
    }

    @Test
    fun `memberValidate should reject artist who died before joining`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {
            birthDate=LocalDate.of(1950,1,1)
            deathDate=LocalDate.of(1980,1,1)
        })

        val result=bandController.memberValidate(member)

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Artist has to be alive when joining the band",result?.body)
    }

    @Test
    fun `memberValidate should reject artist leaving after death`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981,leftYear=2000)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {
            birthDate=LocalDate.of(1950,1,1)
            deathDate=LocalDate.of(2005,1,1)
        })

        val result=bandController.memberValidate(member)

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Artist has to leave the band when dying",result?.body)
    }

    @Test
    fun `memberValidate should accept an artist death date when left year is null`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981,leftYear=null)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {
            birthDate=LocalDate.of(1950,1,1)
            deathDate=LocalDate.of(2005,1,1)
        })

        val result=bandController.memberValidate(member)

        assertNull(result)
    }

    @Test
    fun `memberValidate should reject unknown artist`() {
        `when`(artistService.doesArtistExist(99L)).thenReturn(false)

        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=99L,bandId=2,role="Vocals",joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Artist with id 99 does not exist",result?.body)
    }

    @Test
    fun `memberValidate should reject unknown band`() {
        `when`(bandService.doesBandExist(99)).thenReturn(false)

        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=99,role="Vocals",joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band with id 99 does not exist",result?.body)
    }

    @Test
    fun `memberValidate should resolve artist from existing member when artist id is null`() {
        `when`(bandService.getBandMemberById(2)).thenReturn(BandsMembers().apply {
            artist=Artist().apply {id=1L}
        })

        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=null,bandId=2,role="Vocals",joinedYear=1981)
        )

        assertNull(result)
        verify(bandService).getBandMemberById(2)
        verify(artistService).getById(1L)
    }

    @Test
    fun `memberValidate should reject nickname longer than 255 characters`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,nickname="x".repeat(256),role="Vocals",joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Nickname can't be longer than 255 characters",result?.body)
    }

    @Test
    fun `memberValidate should reject role longer than 20 characters`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role="x".repeat(21),joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Role can't be longer than 20 characters. Input roles separately",result?.body)
    }

    @Test
    fun `bandValidate should accept null formed year`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=null,status=Status.ACTIVE,imageUrl=null)
        )

        assertNull(result)
    }

    @Test
    fun `bandValidate should reject image URL longer than 255 characters`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",status=Status.ACTIVE,imageUrl="https://${"x".repeat(250)}.com")
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Image url can't be more than 255 characters and has to be valid URL",result?.body)
    }

    @Test
    fun `memberValidate should accept null joined year`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=null)
        )

        assertNull(result)
    }

    @Test
    fun `memberValidate should accept null band id`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=null,role="Vocals",joinedYear=1981)
        )

        assertNull(result)
    }

    @Test
    fun `memberValidate should accept non null left year`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981,leftYear=2000)
        )

        assertNull(result)
    }

    @Test
    fun `memberValidate should accept null role`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role=null,joinedYear=1981)
        )

        assertNull(result)
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    inner class BandControllerIntegrationTest {

        @Autowired
        private lateinit var mockMvc: MockMvc

        @Autowired
        private lateinit var bandRepository: BandRepository

        @Autowired
        private lateinit var bandsMemberRepository: BandsMemberRepository

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
            bandsMemberRepository.deleteAll()
            bandRepository.deleteAll()
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
        fun `addBand should work for authorized user`() {
            val countryId = countryRepository.saveAndFlush(Country().apply { name="USA" }).id!!
            val bandAddDto = BandAddDto(
                name="Metallica",
                formedYear=1981,
                status=Status.ACTIVE,
                country=countryId,
                description="Thrash legends",
                imageUrl="https://image.com"
            )

            mockMvc.post("/api/band/add") {
                header("Authorization", "Bearer $userToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(bandAddDto)
            }.andExpect {
                status { isOk() }
                content { string("Band addition request received") }
            }

            val band = bandRepository.findAll().find { it.name == "Metallica" }
            assertNotNull(band)
        }

        @Test
        fun `addBand should return unauthorized for missing token`() {
            mockMvc.post("/api/band/add") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(BandAddDto(name="Unknown", imageUrl=null))
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        fun `addBandMember should work with null nickname`() {
            val band = bandRepository.saveAndFlush(Band().apply { name="Metallica" })
            val artist = artistRepository.saveAndFlush(Artist().apply { name="James Hetfield" })

            val memberAddDto = ArtistBandAddDto(
                bandId=band.id,
                artistId=artist.id,
                role="Vocals",
                joinedYear=1981,
                nickname=null
            )

            mockMvc.post("/api/band/member-add") {
                header("Authorization", "Bearer $userToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(memberAddDto)
            }.andExpect {
                status { isOk() }
                content { string("Band member addition request received") }
            }

            val member = bandsMemberRepository.findAll().find { it.artist?.id == artist.id && it.band?.id == band.id }
            assertNotNull(member)
            assertNull(member?.nickname)
        }

        @Test
        fun `addBandMember should work with nickname`() {
            val band = bandRepository.saveAndFlush(Band().apply { name="Metallica" })
            val artist = artistRepository.saveAndFlush(Artist().apply { name="James Hetfield" })

            val memberAddDto = ArtistBandAddDto(
                bandId=band.id,
                artistId=artist.id,
                role="Vocals",
                joinedYear=1981,
                nickname="Papa Het"
            )

            mockMvc.post("/api/band/member-add") {
                header("Authorization", "Bearer $userToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(memberAddDto)
            }.andExpect {
                status { isOk() }
            }

            val member = bandsMemberRepository.findAll().find { it.artist?.id == artist.id && it.band?.id == band.id }
            assertNotNull(member)
            assertEquals("Papa Het", member?.nickname)
        }

        @Test
        fun `addBandMember should return too many requests when rate limit reached`() {
            val band = bandRepository.saveAndFlush(Band().apply { name="Metallica" })
            val artist = artistRepository.saveAndFlush(Artist().apply { name="James Hetfield" })
            val memberAddDto = ArtistBandAddDto(bandId=band.id, artistId=artist.id, role="Vocals", joinedYear=1981)

            repeat(20) {
                mockMvc.post("/api/band/member-add") {
                    header("Authorization", "Bearer $userToken")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(memberAddDto)
                }
            }

            mockMvc.post("/api/band/member-add") {
                header("Authorization", "Bearer $userToken")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(memberAddDto)
            }.andExpect {
                status { isTooManyRequests() }
            }
        }
    }
}

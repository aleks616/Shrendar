package org.aleks616.shrendar.band.controller

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.service.CountryService
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.security.RateLimiter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
    fun `getAll returns all bands`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getAll()).thenReturn(bands)

        mockMvc.get("/api/band/")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica'}]")}
            }
    }

    @Test
    fun `getBand returns band by id`() {
        val band=BandDto(id=1,name="Metallica")
        `when`(bandService.getBandDataById(1)).thenReturn(band)

        mockMvc.get("/api/band/id/1")
            .andExpect {
                status {isOk()}
                content {json("{'id':1,'name':'Metallica'}")}
            }
    }

    @Test
    fun `getBandByIdWiki returns wiki data`() {
        val wikiData=BandWikiDto(name="Metallica")
        `when`(bandService.getBandByIdWiki(1)).thenReturn(wikiData)

        mockMvc.get("/api/band/wiki/1")
            .andExpect {
                status {isOk()}
                content {json("{'name':'Metallica'}")}
            }
    }

    @Test
    fun `getAllBandMembersWiki returns wiki members`() {
        val members=listOf(BandsMembersWikiDto(id=1,artistName="James Hetfield"))
        `when`(bandsMemberService.getAllBandMembersWiki(1)).thenReturn(members)

        mockMvc.get("/api/band/wiki/1/members")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getAllMembersOfBand returns members`() {
        val members=listOf(BandsMembersDto(id=1,artistName="James Hetfield"))
        `when`(bandsMemberService.getAllBandMembers(1)).thenReturn(members)

        mockMvc.get("/api/band/1/members")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getCurrentBandMembers returns current members`() {
        val members=listOf(BandsMembersDto(id=1,artistName="James Hetfield"))
        `when`(bandsMemberService.getCurrentBandMembers(1)).thenReturn(members)

        mockMvc.get("/api/band/1/members/current")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getPastBandMembers returns past members`() {
        val members=listOf(BandsMembersDto(id=2,artistName="Cliff Burton"))
        `when`(bandsMemberService.getPastBandMembers(1)).thenReturn(members)

        mockMvc.get("/api/band/1/members/past")
            .andExpect {
                status {isOk()}
                content {json("[{'id':2,'artistName':'Cliff Burton'}]")}
            }
    }

    @Test
    fun `getBandByNameLike returns matching bands`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getBandsByName("Meta")).thenReturn(bands)

        mockMvc.get("/api/band/name-like/Meta")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica'}]")}
            }
    }

    @Test
    fun `getBandsByNameExact returns exactly matching bands`() {
        val bands=listOf(BandDto(id=1,name="Metallica"))
        `when`(bandService.getBandsByNameExact("Metallica")).thenReturn(bands)

        mockMvc.get("/api/band/name-exact/Metallica")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica'}]")}
            }
    }

    @Test
    fun `getBandsByCountryName returns bands by country name`() {
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
    fun `getBandsByCountryId returns bands by country id`() {
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
    fun `getBandsByFoundedBetween returns bands for valid range`() {
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
    fun `getBandsByFoundedBetween rejects null start and end both`() {
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: startYear and endYear cannot both be null",
            ex.message
        )
    }

    @Test
    fun `getBandsByFoundedBetween rejects reversed range`() {
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween?startYear=1990&endYear=1980")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: startYear cannot be greater than endYear",
            ex.message
        )
    }

    @Test
    fun `getBandsByFoundedBetween rejects future start year`() {
        val currentYear=LocalDate.now().year
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween?startYear=${currentYear+1}")
        }
        assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid startYear",ex.message)
    }

    @Test
    fun `getBandsByFoundedBetween rejects future end year`() {
        val currentYear=LocalDate.now().year
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/foundedBetween?endYear=${currentYear+1}")
        }
        assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid endYear",ex.message)
    }

    @Test
    fun `getBandsByStatus returns active band status`() {
        val bands=listOf(BandDto(id=1,name="Metallica",status=Status.ACTIVE))
        `when`(bandService.getBandsByStatus(Status.ACTIVE)).thenReturn(bands)

        mockMvc.get("/api/band/status/active")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica','status':'ACTIVE'}]")}
            }
    }

    @Test
    fun `getBandsByStatus accepts capitalized status`() {
        val bands=listOf(BandDto(id=1,name="Metallica",status=Status.ACTIVE))
        `when`(bandService.getBandsByStatus(Status.ACTIVE)).thenReturn(bands)

        mockMvc.get("/api/band/status/Active")
            .andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus returns disbanded band status`() {
        val bands=listOf(BandDto(id=2,name="Slayer",status=Status.DISBANDED))
        `when`(bandService.getBandsByStatus(Status.DISBANDED)).thenReturn(bands)

        mockMvc.get("/api/band/status/disbanded")
            .andExpect {
                status {isOk()}
                content {json("[{'id':2,'name':'Slayer','status':'DISBANDED'}]")}
            }
    }

    @Test
    fun `getBandsByStatus handles on hold format`() {
        `when`(bandService.getBandsByStatus(Status.ON_HOLD)).thenReturn(emptyList())

        mockMvc.get("/api/band/status/on_hold").andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus handles on hold with space format`() {
        `when`(bandService.getBandsByStatus(Status.ON_HOLD)).thenReturn(emptyList())

        mockMvc.get("/api/band/status/on hold").andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus rejects invalid status`() {
        val ex=assertThrows<ServletException> {
            mockMvc.get("/api/band/status/invalid")
        }
        assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid status",ex.message)
    }

    @Test
    fun `getBandsByArtistId returns bands for artist`() {
        val history=listOf(ArtistBandsHistoryDto(id=1,artistName="James Hetfield",bandName="Metallica"))
        `when`(bandsMemberService.getBandsByArtistId(1)).thenReturn(history)

        mockMvc.get("/api/band/artist/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'artistName':'James Hetfield','bandName':'Metallica'}]")}
            }
    }

    @Test
    fun `getSimilarBands returns similar bands`() {
        val similar=listOf(BandGenreDto(id=2,name="Megadeth",similarity=0.9))
        `when`(bandService.getSimilarBands(1,5)).thenReturn(similar)

        mockMvc.get("/api/band/similar/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':2,'name':'Megadeth','similarity':0.9}]")}
            }
    }

    @Test
    fun `getSimilarBands uses default quantity when not provided`() {
        val similar=listOf(BandGenreDto(id=2,name="Megadeth",similarity=0.9))
        `when`(bandService.getSimilarBands(1,5)).thenReturn(similar)

        mockMvc.get("/api/band/similar/1")
            .andExpect {
                status {isOk()}
            }

        verify(bandService).getSimilarBands(1,5)
    }

    @Test
    fun `addBandRequest rejects missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandRequest rejects IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this IP",result.body)
    }

    @Test
    fun `addBandRequest rejects login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this user",result.body)
    }

    @Test
    fun `addBandRequest rejects missing required fields`() {
        val result=bandController.addBandRequest(BandAddDto(name="",status=null,imageUrl=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("At least band name and status are required to add a new band",result.body)
    }

    @Test
    fun `addBandRequest rejects future formed year`() {
        val result=bandController.addBandRequest(
            BandAddDto(name="Metallica",formedYear=LocalDate.now().year+1,status=Status.ACTIVE,imageUrl=null),
            request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band formed year cannot be in the future",result.body)
    }

    @Test
    fun `addBandRequest succeeds`() {
        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band addition request received",result.body)
        verify(bandService).addBandRequest(validBandDto,"user")
    }

    @Test
    fun `editBand rejects missing id`() {
        val result=bandController.editBand(BandAddDto(name="Metallica",status=Status.ACTIVE,imageUrl=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBand rejects IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBand rejects login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBand rejects non existing band`() {
        `when`(bandService.doesBandExist(1)).thenReturn(false)

        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band with id 1 does not exist",result.body)
    }

    @Test
    fun `editBand succeeds`() {
        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band edit request received",result.body)
        verify(bandService).editBandRequest(validBandDto.copy(id=1),"user")
    }

    @Test
    fun `deleteBand rejects missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteBand rejects IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBand rejects login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBand rejects non existing band`() {
        `when`(bandService.doesBandExist(1)).thenReturn(false)

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band with id 1 does not exist",result.body)
    }

    @Test
    fun `deleteBand succeeds`() {
        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band deletion request received",result.body)
        verify(bandService).deleteBandRequest(1,"user")
    }

    @Test
    fun `addBandMembersRequest rejects missing authentication`() {
        SecurityContextHolder.clearContext()
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest rejects IP rate limit`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",20,120)).thenReturn(false)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest rejects login rate limit`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("login:acct:user",20,120)).thenReturn(false)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest rejects missing required member info`() {
        val result=
            bandController.addBandMembersRequest(ArtistBandAddDto(artistId=1L,bandId=2,role=null,joinedYear=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest rejects duplicate member`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesSameMemberExist(member)).thenReturn(true)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Member with id, role and joined year or left year already exists",result.body)
    }

    @Test
    fun `addBandMembersRequest succeeds`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesSameMemberExist(member)).thenReturn(false)

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandsMemberService).addBandMemberRequest(member,"user")
    }

    @Test
    fun `memberValidate rejects artist younger than 10 at join`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=2000)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {birthDate=LocalDate.of(1999,1,1)})

        val result=bandController.memberValidate(member)

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Artist has to be at least 10 years old when joining the band",result?.body)
    }

    @Test
    fun `memberValidate rejects invalid left year ordering`() {
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
    fun `editBandMembersRequest rejects missing id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981),
            request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest rejects IP rate limit`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",20,120)).thenReturn(false)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest rejects login rate limit`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(rateLimiter.allowRequest("login:acct:user",20,120)).thenReturn(false)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest rejects missing band member`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(false)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest succeeds`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandsMemberService).editBandMemberRequest(member,"user")
    }

    @Test
    fun `deleteBandMembersRequest rejects missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest rejects IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest rejects login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest rejects non existing band member`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(false)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `deleteBandMembersRequest succeeds`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(true)

        val result=bandController.deleteBandMembersRequest(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandsMemberService).deleteBandMemberRequest(1L,"user")
    }

    @Test
    fun `favoriteBand rejects missing authentication`() {
        SecurityContextHolder.clearContext()

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `favoriteBand rejects IP rate limit`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `favoriteBand rejects login rate limit`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
    }

    @Test
    fun `favoriteBand rejects non existing band`() {
        `when`(bandService.doesBandExist(1)).thenReturn(false)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `favoriteBand succeeds`() {
        `when`(bandService.doesBandExist(1)).thenReturn(true)

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Band favorite toggled successfully",result.body)
        verify(bandService).toggleFavoriteBand(1,"user")
    }

    @Test
    fun `bandValidate rejects future formed year`() {
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
    fun `bandValidate rejects disbanded status without disbanded year`() {
        val result=bandController.bandValidate(BandAddDto(name="Metallica",status=Status.DISBANDED,imageUrl=null))

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Disbanded year is required if band status is disbanded",result?.body)
    }

    @Test
    fun `bandValidate rejects unknown country`() {
        `when`(countryService.doesCountryExist(99)).thenReturn(false)

        val result=bandController.bandValidate(BandAddDto(name="Metallica",status=Status.ACTIVE,country=99,imageUrl=null))

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Country with id 99 does not exist",result?.body)
    }

    @Test
    fun `bandValidate rejects invalid image URL`() {
        val result=bandController.bandValidate(BandAddDto(name="Metallica",status=Status.ACTIVE,imageUrl="bad-url"))

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Image url can't be more than 255 characters and has to be valid URL",result?.body)
    }

    @Test
    fun `getBandsByFoundedBetween accepts null end year`() {
        `when`(bandService.getBandsByFoundedBetween(1980,null)).thenReturn(emptyList())

        val result=bandController.getBandsByFoundedBetween(1980,null)

        assertEquals(emptyList<BandDto>(),result)
        verify(bandService).getBandsByFoundedBetween(1980,null)
    }

    @Test
    fun `statusStringToEnum rejects unknown status`() {
        val ex=assertThrows<IllegalArgumentException> {
            bandController.statusStringToEnum("not-a-status")
        }

        assertEquals("invalid status",ex.message)
    }

    @Test
    fun `getSimilarBands uses default quantity for null`() {
        `when`(bandService.getSimilarBands(1,5)).thenReturn(emptyList())

        val result=bandController.getSimilarBands(1,null)

        assertEquals(emptyList<BandGenreDto>(),result)
        verify(bandService).getSimilarBands(1,5)
    }

    @Test
    fun `addBandRequest handles contribution limit exception`() {
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandService).addBandRequest(validBandDto,"user")

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `addBandRequest handles unexpected exception`() {
        `when`(bandService.addBandRequest(validBandDto,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.addBandRequest(validBandDto,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `editBand rejects missing name`() {
        val result=bandController.editBand(validBandDto.copy(id=1,name=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band id, name and status are required",result.body)
    }

    @Test
    fun `editBand rejects missing status`() {
        val result=bandController.editBand(validBandDto.copy(id=1,status=null),request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Band id, name and status are required",result.body)
    }

    @Test
    fun `editBand accepts valid band validation`() {
        val result=bandController.editBand(validBandDto.copy(id=1),request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(bandService).editBandRequest(validBandDto.copy(id=1),"user")
    }

    @Test
    fun `editBand handles contribution limit exception`() {
        val band=validBandDto.copy(id=1)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandService).editBandRequest(band,"user")

        val result=bandController.editBand(band,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `editBand handles unexpected exception`() {
        val band=validBandDto.copy(id=1)
        `when`(bandService.editBandRequest(band,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.editBand(band,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `deleteBand handles contribution limit exception`() {
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandService).deleteBandRequest(1,"user")

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `deleteBand handles unexpected exception`() {
        `when`(bandService.deleteBandRequest(1,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.deleteBand(1,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `addBandMembersRequest rejects missing artist id`() {
        val result=bandController.addBandMembersRequest(
            ArtistBandAddDto(artistId=null,bandId=2,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest rejects missing band id`() {
        val result=bandController.addBandMembersRequest(
            ArtistBandAddDto(artistId=1L,bandId=null,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest rejects missing joined year`() {
        val result=bandController.addBandMembersRequest(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=null),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `addBandMembersRequest handles contribution limit exception`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandsMemberService).addBandMemberRequest(member,"user")

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `addBandMembersRequest handles unexpected exception`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandsMemberService.addBandMemberRequest(member,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.addBandMembersRequest(member,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `editBandMembersRequest rejects missing member id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=null,artistId=1L,bandId=2,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest rejects missing artist id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=null,bandId=2,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest rejects missing band id`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=1L,bandId=null,role="Vocals",joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest rejects missing role`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role=null,joinedYear=1981),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `editBandMembersRequest rejects missing joined year`() {
        val result=bandController.editBandMembersRequest(
            ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=null),request
        )

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
    }

    @Test
    fun `memberValidate returns null for valid member`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981)

        assertNull(bandController.memberValidate(member))
    }

    @Test
    fun `editBandMembersRequest rejects invalid member validation`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=LocalDate.now().year+1)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Joined year can't be in the future",result.body)
    }

    @Test
    fun `editBandMembersRequest handles contribution limit exception`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandsMemberService).editBandMemberRequest(member,"user")

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `editBandMembersRequest handles unexpected exception`() {
        val member=ArtistBandAddDto(id=10L,artistId=1L,bandId=2,role="Vocals",joinedYear=1981)
        `when`(bandService.doesBandMemberExist(10L)).thenReturn(true)
        `when`(bandsMemberService.editBandMemberRequest(member,"user"))
            .thenThrow(IllegalStateException("boom"))

        val result=bandController.editBandMembersRequest(member,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `deleteBandMembersRequest handles contribution limit exception`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(true)
        doAnswer { throw ContributionLimitExceededException("limit reached") }
            .`when`(bandsMemberService).deleteBandMemberRequest(1L,"user")

        val result=bandController.deleteBandMembersRequest(1L,request)

        assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        assertEquals("ContributionLimitExceededException limit reached",result.body)
    }

    @Test
    fun `deleteBandMembersRequest handles unexpected exception`() {
        `when`(bandsMemberService.doesBandMemberExist(1L)).thenReturn(true)
        doAnswer { throw IllegalStateException("boom") }
            .`when`(bandsMemberService).deleteBandMemberRequest(1L,"user")

        val result=bandController.deleteBandMembersRequest(1L,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `favoriteBand handles unexpected exception`() {
        doAnswer { throw IllegalStateException("boom") }
            .`when`(bandService).toggleFavoriteBand(1,"user")

        val result=bandController.favoriteBand(1,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: boom",result.body)
    }

    @Test
    fun `bandValidate rejects formed year before 1901`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=1900,status=Status.ACTIVE,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band formed year cannot be before 1901",result?.body)
    }

    @Test
    fun `bandValidate rejects disbanded year before 1901`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=1901,disbandedYear=1900,status=Status.DISBANDED,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band disbanded year cannot be before 1901",result?.body)
    }

    @Test
    fun `bandValidate rejects disbanded year before formed year`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=2000,disbandedYear=1999,status=Status.DISBANDED,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band disbanded year cannot be before formed year",result?.body)
    }

    @Test
    fun `bandValidate rejects non disbanded status with disbanded year`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=1981,disbandedYear=2000,status=Status.ACTIVE,imageUrl=null)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band status must be disbanded if disbanded year is provided",result?.body)
    }

    @Test
    fun `bandValidate accepts valid disbanded band`() {
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
    fun `memberValidate rejects future left year`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981,leftYear=LocalDate.now().year+1)

        val result=bandController.memberValidate(member)

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Left year can't be in the future",result?.body)
    }

    @Test
    fun `memberValidate rejects artist who died before joining`() {
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
    fun `memberValidate rejects artist leaving after death`() {
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
    fun `memberValidate accepts an artist death date when left year is null`() {
        val member=ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981,leftYear=null)
        `when`(artistService.getById(1L)).thenReturn(Artist().apply {
            birthDate=LocalDate.of(1950,1,1)
            deathDate=LocalDate.of(2005,1,1)
        })

        val result=bandController.memberValidate(member)

        assertNull(result)
    }

    @Test
    fun `memberValidate rejects unknown artist`() {
        `when`(artistService.doesArtistExist(99L)).thenReturn(false)

        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=99L,bandId=2,role="Vocals",joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Artist with id 99 does not exist",result?.body)
    }

    @Test
    fun `memberValidate rejects unknown band`() {
        `when`(bandService.doesBandExist(99)).thenReturn(false)

        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=99,role="Vocals",joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Band with id 99 does not exist",result?.body)
    }

    @Test
    fun `memberValidate resolves artist from existing member when artist id is null`() {
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
    fun `memberValidate rejects nickname longer than 255 characters`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,nickname="x".repeat(256),role="Vocals",joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Nickname can't be longer than 255 characters",result?.body)
    }

    @Test
    fun `memberValidate rejects role longer than 20 characters`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role="x".repeat(21),joinedYear=1981)
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Role can't be longer than 20 characters. Input roles separately",result?.body)
    }

    @Test
    fun `bandValidate accepts null formed year`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",formedYear=null,status=Status.ACTIVE,imageUrl=null)
        )

        assertNull(result)
    }

    @Test
    fun `bandValidate rejects image URL longer than 255 characters`() {
        val result=bandController.bandValidate(
            BandAddDto(name="Metallica",status=Status.ACTIVE,imageUrl="https://${"x".repeat(250)}.com")
        )

        assertEquals(HttpStatus.BAD_REQUEST,result?.statusCode)
        assertEquals("Image url can't be more than 255 characters and has to be valid URL",result?.body)
    }

    @Test
    fun `memberValidate accepts null joined year`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=null)
        )

        assertNull(result)
    }

    @Test
    fun `memberValidate accepts null band id`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=null,role="Vocals",joinedYear=1981)
        )

        assertNull(result)
    }

    @Test
    fun `memberValidate accepts non null left year`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role="Vocals",joinedYear=1981,leftYear=2000)
        )

        assertNull(result)
    }

    @Test
    fun `memberValidate accepts null role`() {
        val result=bandController.memberValidate(
            ArtistBandAddDto(artistId=1L,bandId=2,role=null,joinedYear=1981)
        )

        assertNull(result)
    }
}

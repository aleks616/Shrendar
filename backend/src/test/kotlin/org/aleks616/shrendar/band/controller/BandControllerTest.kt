package org.aleks616.shrendar.band.controller

import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class BandControllerTest {

    private val bandService:BandService=mock(BandService::class.java)
    private val bandsMemberService:BandsMemberService=mock(BandsMemberService::class.java)
    private val controller=BandController(bandService,bandsMemberService)
    private val mockMvc:MockMvc=MockMvcBuilders.standaloneSetup(controller).build()

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
    fun `getAllMembersOfBand should return all members`() {
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
    fun `getBandsByFoundedBetween should throw exception for invalid inputs`() {
        val currentYear=LocalDate.now().year

        assertThrows<IllegalArgumentException> {
            controller.getBandsByFoundedBetween(null,null)
        }.also {assertEquals("startYear and endYear cannot both be null",it.message)}

        assertThrows<IllegalArgumentException> {
            controller.getBandsByFoundedBetween(1990,1980)
        }.also {assertEquals("startYear cannot be greater than endYear",it.message)}

        assertThrows<IllegalArgumentException> {
            controller.getBandsByFoundedBetween(currentYear+1,null)
        }.also {assertEquals("invalid startYear",it.message)}

        assertThrows<IllegalArgumentException> {
            controller.getBandsByFoundedBetween(null,currentYear+1)
        }.also {assertEquals("invalid endYear",it.message)}
    }

    @Test
    fun `getBandsByStatus should return bands for valid status`() {
        val bands=listOf(BandDto(id=1,name="Metallica",status=Status.active))
        `when`(bandService.getBandsByStatus(Status.active)).thenReturn(bands)

        mockMvc.get("/api/band/status/active")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Metallica','status':'active'}]")}
            }

        mockMvc.get("/api/band/status/Active").andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus should handle various status formats`() {
        `when`(bandService.getBandsByStatus(Status.on_hold)).thenReturn(emptyList())

        mockMvc.get("/api/band/status/on_hold").andExpect {status {isOk()}}
        mockMvc.get("/api/band/status/on hold").andExpect {status {isOk()}}
    }

    @Test
    fun `getBandsByStatus should throw exception for invalid status`() {
        assertThrows<IllegalArgumentException> {
            controller.getBandsByStatus("invalid")
        }.also {assertEquals("invalid status",it.message)}
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
}

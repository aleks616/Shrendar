package org.aleks616.shrendar.artist.controller

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.service.ArtistService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ArtistControllerTest {

    private val artistService:ArtistService=mock(ArtistService::class.java)
    private val controller=ArtistController(artistService)
    private val mockMvc:MockMvc=MockMvcBuilders.standaloneSetup(controller).build()

    private fun createArtist(id:Int,name:String):Artist {
        val artist=Artist()
        artist.id=id
        artist.name=name
        return artist
    }

    @Test
    fun `getAll should return all artists`() {
        val id=(1..100).random()
        val artists=listOf(createArtist(id,"James Hetfield"))
        `when`(artistService.getAll()).thenReturn(artists)

        mockMvc.get("/api/artist/")
            .andExpect {
                status {isOk()}
                content {json("[{'id':$id,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getById should return artist for existing id`() {
        val artists=createArtist(1,"James Hetfield")
        `when`(artistService.getById(1)).thenReturn(artists)

        mockMvc.get("/api/artist/id/1") {}.andExpect {
            status {isOk()}
            content {json("{'id':1,'name':'James Hetfield'}")}
        }
    }

    @Test
    fun `getById should throw error when service throws error`() {
        `when`(artistService.getById(999)).thenThrow(IllegalArgumentException("artist with id doesn't exist"))

        val ex=assertThrows<IllegalArgumentException> {
            controller.getById(999)
        }
        assertEquals("artist with id doesn't exist",ex.message)
    }

    @Test
    fun `getByNameLike should return artists for valid name`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
        `when`(artistService.getByNameLike("James")).thenReturn(artists)

        mockMvc.get("/api/artist/name") {
            param("name","James")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByNameLike should throw error for short name`() {
        val ex=assertThrows<IllegalArgumentException> {
            controller.getByNameLike("Ja")
        }
        assertEquals("name has to be at least 3 characters",ex.message)
    }

    @Test
    fun `getByFirstName should return artists for valid name`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
        `when`(artistService.getByFirstName("James")).thenReturn(artists)

        mockMvc.get("/api/artist/first-name") {
            param("name","James")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByFirstName should throw error for short name`() {
        val ex=assertThrows<IllegalArgumentException> {
            controller.getByFirstName("Ja")
        }
        assertEquals("name has to be at least 2 characters",ex.message)
    }

    @Test
    fun `getByLastName should return artists for valid name`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
        `when`(artistService.getByLastName("Hetfield")).thenReturn(artists)

        mockMvc.get("/api/artist/last-name") {
            param("name","Hetfield")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'James Hetfield'}]")}
        }
    }

    @Test
    fun `getByLastName should throw error for short name`() {
        val ex=assertThrows<IllegalArgumentException> {
            controller.getByLastName("He")
        }
        assertEquals("name has to be at least 2 characters",ex.message)
    }

    @Test
    fun `getByBirthday should return artists for valid date`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
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
    fun `getByBirthday should throw error for invalid date`() {
        val ex=assertThrows<IllegalArgumentException> {
            controller.getByBirthday(13,1)
        }
        assertEquals("invalid month or day",ex.message)
    }

    @Test
    fun `getByBirthdayBetween should return artists for valid dates`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
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
    fun `getByBirthdayBetween should throw error for invalid dates`() {
        assertThrows<IllegalArgumentException> {
            controller.getByBirthdayBetween(13,1,12,31)
        }
        assertThrows<IllegalArgumentException> {
            controller.getByBirthdayBetween(1,1,1,32)
        }
    }

    @Test
    fun `getByBirthYear should return artists`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
        `when`(artistService.getByBirthYear(1963)).thenReturn(artists)

        mockMvc.get("/api/artist/birthyear/1963")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getByBirthYearBetween should return artists`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
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
        val artists=listOf(createArtist(1,"James Hetfield"))
        `when`(artistService.getRecentBirthdays()).thenReturn(artists)

        mockMvc.get("/api/artist/recentBirthdays")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getByDeathDate should return artists for valid date`() {
        val artists=listOf(createArtist(1,"Cliff Burton"))
        `when`(artistService.getByDeathDate(9,27)).thenReturn(artists)

        mockMvc.get("/api/artist/death") {
            param("month","9")
            param("day","27")
        }.andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'Cliff Burton'}]")}
        }
    }

    @Test
    fun `getByDeathDate should throw error for invalid date`() {
        val ex=assertThrows<IllegalArgumentException> {
            controller.getByDeathDate(2,30)
        }
        assertEquals("invalid month or day",ex.message)
    }

    @Test
    fun `getRecentArtistDeathAnniversaries should return artists`() {
        val artists=listOf(createArtist(1,"Cliff Burton"))
        `when`(artistService.getRecentDeathsAnniversaries()).thenReturn(artists)

        mockMvc.get("/api/artist/recentDeaths")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Cliff Burton'}]")}
            }
    }

    @Test
    fun `getByCountry should return artists`() {
        val artists=listOf(createArtist(1,"James Hetfield"))
        `when`(artistService.getByCountry(1)).thenReturn(artists)

        mockMvc.get("/api/artist/country/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }
}

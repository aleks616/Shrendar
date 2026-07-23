package org.aleks616.shrendar.artist.controller

import jakarta.servlet.ServletException
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.artist.service.ArtistService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArtistControllerTest {

    private val artistService:ArtistService=mock(ArtistService::class.java)

    @Autowired
    private lateinit var artistRepository:ArtistRepository

    @Autowired
    private lateinit var albumRepository:AlbumRepository

    @Autowired
    private lateinit var mockMvc:MockMvc

    @Autowired
    private lateinit var rateLimiter:org.aleks616.shrendar.security.RateLimiter

    @BeforeEach
    fun setup() {
        artistRepository.deleteAll()
        albumRepository.deleteAll()

        val storageField=org.aleks616.shrendar.security.RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<*,*>).clear()
        artistRepository.save(Artist().apply {name="James Hetfield"})
    }

    @Test
    fun `getAll should return all artists`() {
        artistRepository.save(Artist().apply {name="James Hetfield"})

        mockMvc.get("/api/artist/")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'James Hetfield'}]")}
            }
    }

    @Test
    fun `getById should return artist for existing id`() {
        artistRepository.save(Artist().apply {name="James Hetfield"})


        mockMvc.get("/api/artist/id/1") {}.andExpect {
            status {isOk()}
            content {json("{'id':1,'name':'James Hetfield'}")}
        }
    }

    @Test
    fun `getById should throw error when service throws error`() {
        `when`(artistService.getById(999)).thenThrow(IllegalArgumentException("artist with id doesn't exist"))

        val exception=assertThrows<ServletException> {
            mockMvc.get("/api/artist/id/999999")
        }
        assertEquals(
            "Request processing failed: java.lang.IllegalArgumentException: artist with id doesn't exist",
            exception.message
        )
    }

    @Test
    fun `getByIdWiki should return wiki data`() {
        artistRepository.save(Artist().apply {name="James Hetfield"})

        mockMvc.get("/api/artist/wiki/1")
            .andExpect {
                status {isOk()}
                content { json("{'id':1,'name':'James Hetfield'}") }
            }
    }

     @Test
     fun `getByNameLike should return artists for valid name`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

         mockMvc.get("/api/artist/name") {
             param("name","James")
         }.andExpect {
             status {isOk()}
             content {json("[{'id':1,'name':'James Hetfield'}]")}
         }
     }

     @Test
     fun `getByNameLike should throw error for short name`() {
         val exception=assertThrows<ServletException> {
             mockMvc.get("/api/artist?name=Ja")
         }
         assertEquals("Request processing failed: java.lang.IllegalArgumentException: name has to be at least 3 characters",exception.message)
     }

     @Test
     fun `getByFirstName should return artists for valid name`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

         mockMvc.get("/api/artist/first-name") {
             param("name","James")
         }.andExpect {
             status {isOk()}
             content {json("[{'id':1,'name':'James Hetfield'}]")}
         }
     }

     @Test
     fun `getByFirstName should throw error for short name`() {
         val exception=assertThrows<ServletException> {
             mockMvc.get("/api/artist/first-name") {
                 param("name","H")
             }
         }
         assertEquals("Request processing failed: java.lang.IllegalArgumentException: name has to be at least 2 characters",exception.message)
     }

     @Test
     fun `getByLastName should return artists for valid name`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

         mockMvc.get("/api/artist/last-name") {
             param("name","Hetfield")
         }.andExpect {
             status {isOk()}
             content {json("[{'id':1,'name':'James Hetfield'}]")}
         }
     }

     @Test
     fun `getByLastName should throw error for short name`() {
         val exception=assertThrows<ServletException> {
             mockMvc.get("/api/artist/last-name") {
                 param("name","d")
             }
         }
         assertEquals("Request processing failed: java.lang.IllegalArgumentException: name has to be at least 2 characters",exception.message)
     }

     @Test
     fun `getByBirthday should return artists for valid date`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

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
         val exception=assertThrows<ServletException> {
             mockMvc.get("/api/artist/birthday?month=13&day=1")
         }
         assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid month or day",exception.message)
     }

     @Test
     fun `getByBirthdayBetween should return artists for valid dates`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

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
         assertThrows<ServletException> {
             mockMvc.get("/api/artist/birthdaybetween?startMonth=13&startDay=1&endMonth=12&endDay=31")
         }
         assertThrows<ServletException> {
             mockMvc.get("/api/artist/birthdaybetween?startMonth=1&startDay=1&endMonth=1&endDay=32")
         }
     }

     @Test
     fun `getByBirthYear should return artists`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

         mockMvc.get("/api/artist/birthyear/1963")
             .andExpect {
                 status {isOk()}
                 content {json("[{'id':1,'name':'James Hetfield'}]")}
             }
     }

     @Test
     fun `getByBirthYearBetween should return artists`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

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
         artistRepository.save(Artist().apply {name="James Hetfield"})

         mockMvc.get("/api/artist/recentBirthdays")
             .andExpect {
                 status {isOk()}
                 content {json("[{'id':1,'name':'James Hetfield'}]")}
             }
     }

     @Test
     fun `getByDeathDate should return artists for valid date`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

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
         val exception=assertThrows<ServletException> {
             mockMvc.get("/api/artist/death?month=2&day=30")

         }
         assertEquals("Request processing failed: java.lang.IllegalArgumentException: invalid month or day",exception.message)
     }

     @Test
     fun `getRecentArtistDeathAnniversaries should return artists`() {
         artistRepository.save(Artist().apply {name="Cliff Burton"})

         mockMvc.get("/api/artist/recentDeaths")
             .andExpect {
                 status {isOk()}
                 content {json("[{'id':1,'name':'Cliff Burton'}]")}
             }
     }

     @Test
     fun `getByCountry should return artists`() {
         artistRepository.save(Artist().apply {name="James Hetfield"})

         mockMvc.get("/api/artist/country/1")
             .andExpect {
                 status {isOk()}
                 content {json("[{'id':1,'name':'James Hetfield'}]")}
             }
     }
}

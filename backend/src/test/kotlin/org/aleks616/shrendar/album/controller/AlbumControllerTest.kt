package org.aleks616.shrendar.album.controller

import org.aleks616.shrendar.album.model.Album
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlbumControllerTest {

    @Autowired
    private lateinit var mockMvc:MockMvc

    @Autowired
    private lateinit var albumRepository:org.aleks616.shrendar.album.repository.AlbumRepository

    @Autowired
    private lateinit var bandRepository:org.aleks616.shrendar.band.repository.BandRepository

    @Autowired
    private lateinit var genreRepository:org.aleks616.shrendar.genre.repository.GenreRepository

    @Autowired
    private lateinit var rateLimiter:org.aleks616.shrendar.security.RateLimiter

    @BeforeEach
    fun setup() {
        albumRepository.deleteAll()
        bandRepository.deleteAll()
        genreRepository.deleteAll()

        val storageField=org.aleks616.shrendar.security.RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<*,*>).clear()
    }


    @Test
    fun `getAlbum should return all albums`() {
        albumRepository.save(Album().apply {title="Album 1"})

        mockMvc.get("/api/album/")
            .andExpect {
                status {isOk()}
                content {json("[{'title':'Album 1'}]")}
            }
    }


    @Test
    fun `getAlbumById should return album`() {
        val album=albumRepository.save(Album().apply {title="Master of Puppets"})

        mockMvc.get("/api/album/id/${album.id}")
            .andExpect {
                status {isOk()}
                content {json("{'title':'Master of Puppets'}")}
            }
    }

    @Test
    fun `getAlbumByIdWiki should return wiki data`() {
        val band=bandRepository.save(org.aleks616.shrendar.band.model.Band().apply {name="Metallica"})
        val album=albumRepository.save(Album().apply {
            title="Master of Puppets"
            this.band=band
            releaseDate=LocalDate.of(1986,3,3)
        })

        mockMvc.get("/api/album/wiki/${album.id}")
            .andExpect {
                status {isOk()}
                content {json("{'albumName':'Master of Puppets'}")}
            }
    }

    @Test
    fun `getAlbumAnniversariesByDate should return albums for valid date`() {
        albumRepository.save(Album().apply {
            title="Anniversary"
            releaseDate=LocalDate.of(2020,5,20)
        })

        mockMvc.get("/api/album/inDate") {
            param("month","5")
            param("day","20")
        }.andExpect {
            status {isOk()}
            content {json("[{'title':'Anniversary'}]")}
        }
    }

    @Test
    fun `getAlbumAnniversariesByDate should throw error for invalid date`() {
        assertThrows<jakarta.servlet.ServletException> {
            mockMvc.get("/api/album/inDate") {
                param("month","13")
                param("day","1")
            }
        }
    }

    @Test
    fun `getAlbumsByBandId should return albums for existing band`() {
        val band=bandRepository.save(org.aleks616.shrendar.band.model.Band().apply {name="Metallica"})
        albumRepository.save(Album().apply {
            title="Band Album"
            this.band=band
        })

        mockMvc.get("/api/album/band/${band.id}")
            .andExpect {
                status {isOk()}
                content {json("[{'title':'Band Album'}]")}
            }
    }

    @Test
    fun `getAlbumsByBandId should throw error for non-existent band`() {
        assertThrows<jakarta.servlet.ServletException> {
            mockMvc.get("/api/album/band/999")
        }
    }

    @Test
    fun `getAlbumsByBandNameLike should return albums`() {
        val band=bandRepository.save(org.aleks616.shrendar.band.model.Band().apply {name="Metallica"})
        albumRepository.save(Album().apply {
            title="Some Album"
            this.band=band
        })

        mockMvc.get("/api/album/band/like/Metallica")
            .andExpect {
                status {isOk()}
                content {json("[{'title':'Some Album'}]")}
            }
    }

    @Test
    fun `getAlbumsByYear should return albums for valid year`() {
        albumRepository.save(Album().apply {
            title="Year Album"
            releaseDate=LocalDate.of(2020,1,1)
        })

        mockMvc.get("/api/album/year/2020").andExpect {status {isOk()}}
    }

    @Test
    fun `getAlbumsByYear should throw error for invalid year`() {
        val futureYear=LocalDate.now().year+1

        assertThrows<jakarta.servlet.ServletException> {
            mockMvc.get("/api/album/year/1901")
        }

        assertThrows<jakarta.servlet.ServletException> {
            mockMvc.get("/api/album/year/$futureYear")
        }
    }

    @Test
    fun `getAlbumsByNameLike should return albums`() {
        albumRepository.save(Album().apply {title="Master of Puppets"})

        mockMvc.get("/api/album/like/Master")
            .andExpect {
                status {isOk()}
                content {json("[{'title':'Master of Puppets'}]")}
            }
    }

    @Test
    fun `getAlbumsByNameExact should return albums`() {
        albumRepository.save(Album().apply {title="Master of Puppets"})

        mockMvc.get("/api/album/exact/Master of Puppets")
            .andExpect {
                status {isOk()}
                content {json("[{'title':'Master of Puppets'}]")}
            }
    }
}

package org.aleks616.shrendar.album.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumAddDto
import org.aleks616.shrendar.album.model.AlbumType
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.security.JwtUtil
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
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

    @Autowired
    private lateinit var userRepository:UserRepository

    @Autowired
    private lateinit var rankRepository:RankRepository

    @Autowired
    private lateinit var contributionRepository:ContributionRepository

    private val objectMapper=ObjectMapper().findAndRegisterModules()
    private lateinit var userToken:String

    @BeforeEach
    fun setup() {
        contributionRepository.deleteAll()
        albumRepository.deleteAll()
        bandRepository.deleteAll()
        genreRepository.deleteAll()
        userRepository.deleteAll()
        rankRepository.deleteAll()

        rankRepository.saveAndFlush(Rank().apply {id=1; name="Newbie"; minXp=0; allowedContributions=10})
        userRepository.saveAndFlush(User().apply {
            login="user"
            username="User"
            email="user@example.com"
            passwordHash="hash"
            rank=rankRepository.findById(1).get()
            verified=true
        })
        userToken=JwtUtil.createToken("user")

        val adminUser=userRepository.findAll().first()
        contributionRepository.saveAndFlush(Contribution().apply {
            changeId=0
            user=adminUser
            action=Action.create
            changedTable="seed"
            changedColumn="seed"
            newValue="seed"
            changedAt=java.time.LocalDateTime.now()
            confirmed=true
        })

        val storageField=org.aleks616.shrendar.security.RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<*,*>).clear()
    }

    @Test
    fun `addAlbum should work for authorized user`() {
        val band=bandRepository.saveAndFlush(org.aleks616.shrendar.band.model.Band().apply {name="Metallica"})
        genreRepository.saveAndFlush(
            org.aleks616.shrendar.genre.model.Genre().apply {id=1; name="Heavy Metal"; properties="1"})

        val albumAddDto=AlbumAddDto(
            bandId=band.id,
            title="Master of Puppets",
            releaseDate=LocalDate.of(1986,3,3),
            type=AlbumType.studio,
            description="Classic",
            mainSubgenre=1,
            importance=1,
            artworkUrl="https://artwork.com"
        )

        mockMvc.post("/api/album/add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(albumAddDto)
        }.andExpect {
            status {isOk()}
            content {string("Album addition request received")}
        }

        val album=albumRepository.findAll().find {it.title=="Master of Puppets"}
        assertNotNull(album)
        assertEquals(1.toByte(),album?.importance)
    }

    @Test
    fun `addAlbum should return unauthorized for missing token`() {
        mockMvc.post("/api/album/add") {
            contentType=MediaType.APPLICATION_JSON
            content="{}"
        }.andExpect {
            status {isForbidden()}
        }
    }

    @Test
    fun `addAlbum should return too many requests when rate limit reached`() {
        val band=bandRepository.saveAndFlush(org.aleks616.shrendar.band.model.Band().apply {name="Metallica"})

        repeat(10) {i->
            genreRepository.saveAndFlush(
                org.aleks616.shrendar.genre.model.Genre().apply {id=i+1; name="Genre $i"; properties="1"})
        }

        repeat(3) {i->
            val albumAddDto=AlbumAddDto(bandId=band.id,title="Quick Album $i",mainSubgenre=i+1)
            mockMvc.post("/api/album/add") {
                header("Authorization","Bearer $userToken")
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(albumAddDto)
            }
        }

        val albumAddDto=AlbumAddDto(bandId=band.id,title="Quick Album 3",mainSubgenre=4)
        mockMvc.post("/api/album/add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(albumAddDto)
        }.andExpect {
            status {isTooManyRequests()}
        }
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

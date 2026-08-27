package org.aleks616.shrendar.album.controller

import jakarta.servlet.http.HttpServletRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumAddDto
import org.aleks616.shrendar.album.model.AlbumType
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.exception.InvalidAlbumImportanceException
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.service.GenreService
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.mockito.Mockito.*
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
    private lateinit var rateLimiter:RateLimiter

    @Autowired
    private lateinit var userRepository:UserRepository

    @Autowired
    private lateinit var rankRepository:RankRepository

    @Autowired
    private lateinit var contributionRepository:ContributionRepository

    @Autowired
    private lateinit var bandsGenreRepository:org.aleks616.shrendar.band.repository.BandsGenreRepository

    @Autowired
    private lateinit var genreService:GenreService

    @Autowired
    private lateinit var bandService:BandService

    private val objectMapper=ObjectMapper().findAndRegisterModules()
    private lateinit var userToken:String

    @BeforeEach
    fun setup() {
        contributionRepository.deleteAll()
        albumRepository.deleteAll()
        bandsGenreRepository.deleteAll()
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
            action=Action.CREATE
            changedTable="seed"
            changedColumn="seed"
            newValue="seed"
            changedAt=java.time.LocalDateTime.now()
            confirmed=true
        })

        val storageField=RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<*,*>).clear()
    }

    @Nested
    inner class AlbumControllerUnitTest {
        private val albumService=mock(AlbumService::class.java)
        private val rateLimiter=mock(RateLimiter::class.java)
        private val genreService=mock(GenreService::class.java)
        private val request=mock(HttpServletRequest::class.java)
        private val controller=AlbumController(albumService,rateLimiter,genreService)
        private val dto=AlbumAddDto(bandId=1,title="Album",type=AlbumType.STUDIO,importance=3)

        @BeforeEach
        fun setupUnitTest() {
            SecurityContextHolder.getContext().authentication=
                UsernamePasswordAuthenticationToken("user",null,emptyList())
            `when`(request.remoteAddr).thenReturn("127.0.0.1")
            `when`(rateLimiter.allowRequest(anyString(),eq(3),eq(60))).thenReturn(true)
            `when`(albumService.doesBandExist(1)).thenReturn(true)
            `when`(albumService.isReleaseDateValid(dto)).thenReturn(true)
        }

        @Test
        fun `getAll should return albums`() {
            controller.getAll()
            verify(albumService).getAll()
        }

        @Test
        fun `getAlbumById should return album`() {
            controller.getAlbumById(1)
            verify(albumService).getById(1)
        }

        @Test
        fun `getAlbumByIdWiki should return wiki data`() {
            controller.getAlbumByIdWiki(1)
            verify(albumService).getByIdWiki(1)
        }

        @Test
        fun `getAlbumAnniversariesByDate should return albums`() {
            controller.getAlbumAnniversariesByDate(5,20)
            verify(albumService).getAlbumAnniversariesByDate(5,20)
        }

        @Test
        fun `getAlbumsByBandId should return albums for existing band`() {
            `when`(albumService.doesBandExist(1)).thenReturn(true)
            controller.getAlbumsByBandId(1)
            verify(albumService).getAlbumsByBandId(1)
        }

        @Test
        fun `getAlbumsByBandNameLike should return albums`() {
            controller.getAlbumsByBandNameLike("Metallica")
            verify(albumService).getAlbumsByBandName("Metallica")
        }

        @Test
        fun `getAlbumsByYear should return albums`() {
            controller.getAlbumsByYear(2020)
            verify(albumService).getAlbumsByYear(2020)
        }

        @Test
        fun `getAlbumsByNameLike should return albums`() {
            controller.getAlbumsByNameLike("Master")
            verify(albumService).getAlbumsByName("Master")
        }

        @Test
        fun `getAlbumsByNameExact should return albums`() {
            controller.getAlbumsByNameExact("Master of Puppets")
            verify(albumService).getAlbumsByNameExact("Master of Puppets")
        }

        @Test
        fun `addAlbum should return success`() {
            `when`(albumService.doesAlbumWithNameExistForBand(dto)).thenReturn(false)

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(albumService).addAlbumRequest(dto,"user")
        }

        @Test
        fun `editAlbum should return success`() {
            val editDto=dto.copy(id=1)
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            `when`(albumService.doesAlbumWithNameExistForAlbumId(editDto)).thenReturn(false)
            `when`(albumService.isReleaseDateValid(editDto)).thenReturn(true)

            val result=controller.editAlbum(editDto,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(albumService).editAlbumRequest(editDto,"user")
        }

        @Test
        fun `deleteAlbum should return success`() {
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)

            val result=controller.deleteAlbum(1,request)

            assertEquals(HttpStatus.OK,result.statusCode)
            verify(albumService).deleteAlbumRequest(1,"user")
        }

        @Test
        fun `addAlbum should return bad request when authentication is missing`() {
            SecurityContextHolder.clearContext()

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `addAlbum should return too many requests when IP rate limit is reached`() {
            `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            verify(rateLimiter,never()).allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `addAlbum should return too many requests when login rate limit is reached`() {
            `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            assertEquals("Too many requests from this user",result.body)
        }

        @Test
        fun `addAlbum should reject null bandId`() {
            val result=controller.addAlbum(dto.copy(bandId=null),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(albumService)
        }

        @Test
        fun `addAlbum should reject null type`() {
            val result=controller.addAlbum(dto.copy(type=null),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            verifyNoInteractions(albumService)
        }

        @Test
        fun `addAlbum should reject duplicate title`() {
            `when`(albumService.doesAlbumWithNameExistForBand(dto)).thenReturn(true)

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `addAlbum should return validation error`() {
            val invalid=dto.copy(bandId=99)
            `when`(albumService.doesBandExist(99)).thenReturn(false)

            val result=controller.addAlbum(invalid,request)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        }

        @Test
        fun `addAlbum should handle contribution limit exception`() {
            `when`(albumService.doesAlbumWithNameExistForBand(dto)).thenReturn(false)
            doAnswer {throw ContributionLimitExceededException("limit reached")}
                .`when`(albumService).addAlbumRequest(dto,"user")

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
            assertEquals("ContributionLimitExceededException limit reached",result.body)
        }

        @Test
        fun `addAlbum should handle invalid importance exception`() {
            `when`(albumService.doesAlbumWithNameExistForBand(dto)).thenReturn(false)
            doAnswer {throw InvalidAlbumImportanceException("invalid importance")}
                .`when`(albumService).addAlbumRequest(dto,"user")

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
            assertEquals("invalid importance",result.body)
        }

        @Test
        fun `addAlbum should handle unexpected exception`() {
            `when`(albumService.doesAlbumWithNameExistForBand(dto)).thenReturn(false)
            doThrow(IllegalStateException("broken"))
                .`when`(albumService).addAlbumRequest(dto,"user")

            val result=controller.addAlbum(dto,request)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
            assertEquals("An unexpected error occurred: broken",result.body)
        }

        @Test
        fun `editAlbum should return bad request when authentication is missing`() {
            SecurityContextHolder.clearContext()

            val result=controller.editAlbum(dto.copy(id=1),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editAlbum should return too many requests when login rate limit is reached`() {
            `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.editAlbum(dto.copy(id=1),request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        }

        @Test
        fun `editAlbum should return too many requests when IP rate limit is reached`() {
            `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.editAlbum(dto.copy(id=1),request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            verify(rateLimiter,never()).allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `editAlbum should reject missing id`() {
            val result=controller.editAlbum(dto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editAlbum should reject missing title`() {
            val result=controller.editAlbum(dto.copy(id=1,title=null),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editAlbum should reject missing type`() {
            val result=controller.editAlbum(dto.copy(id=1,type=null),request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editAlbum should reject nonexistent album`() {
            `when`(albumService.doesAlbumExist(99)).thenReturn(false)

            val result=controller.editAlbum(dto.copy(id=99),request)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        }

        @Test
        fun `editAlbum should reject duplicate title`() {
            val editDto=dto.copy(id=1)
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            `when`(albumService.doesAlbumWithNameExistForAlbumId(editDto)).thenReturn(true)

            val result=controller.editAlbum(editDto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editAlbum should return validation error`() {
            val invalid=dto.copy(id=1,bandId=99)
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            `when`(albumService.doesAlbumWithNameExistForAlbumId(invalid)).thenReturn(false)
            `when`(albumService.doesBandExist(99)).thenReturn(false)

            val result=controller.editAlbum(invalid,request)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        }

        @Test
        fun `editAlbum should handle contribution limit exception`() {
            val editDto=dto.copy(id=1)
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            `when`(albumService.doesAlbumWithNameExistForAlbumId(editDto)).thenReturn(false)
            `when`(albumService.isReleaseDateValid(editDto)).thenReturn(true)
            doAnswer {throw ContributionLimitExceededException("limit reached")}
                .`when`(albumService).editAlbumRequest(editDto,"user")

            val result=controller.editAlbum(editDto,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        }

        @Test
        fun `editAlbum should handle invalid importance exception`() {
            val editDto=dto.copy(id=1)
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            `when`(albumService.doesAlbumWithNameExistForAlbumId(editDto)).thenReturn(false)
            `when`(albumService.isReleaseDateValid(editDto)).thenReturn(true)
            doAnswer {throw InvalidAlbumImportanceException("invalid importance")}
                .`when`(albumService).editAlbumRequest(editDto,"user")

            val result=controller.editAlbum(editDto,request)

            assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        }

        @Test
        fun `editAlbum should handle unexpected exception`() {
            val editDto=dto.copy(id=1)
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            `when`(albumService.doesAlbumWithNameExistForAlbumId(editDto)).thenReturn(false)
            `when`(albumService.isReleaseDateValid(editDto)).thenReturn(true)
            doThrow(IllegalStateException("broken"))
                .`when`(albumService).editAlbumRequest(editDto,"user")

            val result=controller.editAlbum(editDto,request)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        }

        @Test
        fun `deleteAlbum should return too many requests when login rate limit is reached`() {
            `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.deleteAlbum(1,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        }

        @Test
        fun `deleteAlbum should return too many requests when IP rate limit is reached`() {
            `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_BASIC,60)).thenReturn(false)

            val result=controller.deleteAlbum(1,request)

            assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
            verify(rateLimiter,never()).allowRequest("login:acct:user",Utils.LIMIT_BASIC,60)
        }

        @Test
        fun `deleteAlbum should reject nonexistent album`() {
            `when`(albumService.doesAlbumExist(99)).thenReturn(false)

            val result=controller.deleteAlbum(99,request)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,result.statusCode)
        }

        @Test
        fun `deleteAlbum should handle contribution limit exception`() {
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            doAnswer {throw ContributionLimitExceededException("limit reached")}
                .`when`(albumService).deleteAlbumRequest(1,"user")

            val result=controller.deleteAlbum(1,request)

            assertEquals(HttpStatus.FORBIDDEN,result.statusCode)
        }

        @Test
        fun `deleteAlbum should handle unexpected exception`() {
            `when`(albumService.doesAlbumExist(1)).thenReturn(true)
            doThrow(IllegalStateException("broken"))
                .`when`(albumService).deleteAlbumRequest(1,"user")

            val result=controller.deleteAlbum(1,request)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        }

        @Test
        fun `albumValidate should accept valid album`() {
            assertNull(controller.albumValidate(dto))
        }

        @Test
        fun `albumValidate should reject missing band`() {
            val album=dto.copy(bandId=99)
            `when`(albumService.doesBandExist(99)).thenReturn(false)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,controller.albumValidate(album)?.statusCode)
        }

        @Test
        fun `albumValidate should reject invalid studio importance`() {
            val album=dto.copy(importance=6)

            assertEquals(HttpStatus.BAD_REQUEST,controller.albumValidate(album)?.statusCode)
        }

        @Test
        fun `albumValidate should reject invalid EP importance`() {
            val album=dto.copy(type=AlbumType.EP,importance=4)

            assertEquals(HttpStatus.BAD_REQUEST,controller.albumValidate(album)?.statusCode)
        }

        @Test
        fun `albumValidate should reject importance for other album types`() {
            val album=dto.copy(type=AlbumType.COMPILATION,importance=1)

            assertEquals(HttpStatus.BAD_REQUEST,controller.albumValidate(album)?.statusCode)
        }

        @Test
        fun `albumValidate should reject invalid release date`() {
            val album=dto.copy(releaseDate=LocalDate.of(1900,1,1))
            `when`(albumService.isReleaseDateValid(album)).thenReturn(false)

            assertEquals(HttpStatus.BAD_REQUEST,controller.albumValidate(album)?.statusCode)
        }

        @Test
        fun `albumValidate should reject missing genre`() {
            val album=dto.copy(mainSubgenre=99)
            `when`(albumService.isReleaseDateValid(album)).thenReturn(true)
            `when`(genreService.doesGenreExist(99)).thenReturn(false)

            assertEquals(HttpStatus.UNPROCESSABLE_CONTENT,controller.albumValidate(album)?.statusCode)
        }

        @Test
        fun `albumValidate should reject malformed artwork URL`() {
            val album=dto.copy(artworkUrl="not a url")
            `when`(albumService.isReleaseDateValid(album)).thenReturn(true)

            assertEquals(HttpStatus.BAD_REQUEST,controller.albumValidate(album)?.statusCode)
        }
    }

    @Test
    fun `addAlbum should work for authorized user`() {
        val band=bandRepository.saveAndFlush(Band().apply {name="Metallica"; formedYear=1981})
        val genre=genreRepository.saveAndFlush(Genre().apply {id=1; name="Thrash Metal"; properties="1111111"})

        val albumAddDto=AlbumAddDto(
            bandId=band.id,
            title="Ride the Lightning",
            releaseDate=LocalDate.of(1984,7,27),
            type=AlbumType.COMPILATION,
            mainSubgenre=genre.id,
            importance=0,
            artworkUrl="https://example.com/artwork.jpg"
        )

        mockMvc.post("/api/album/add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(albumAddDto)
        }.andExpect {
            status {isOk()}
            content {string("Album addition request received")}
        }

        val album=albumRepository.findAll().find {it.title=="Ride the Lightning"}
        assertNotNull(album)
        assertEquals(band.id,album?.band?.id)
        assertEquals(AlbumType.COMPILATION,album?.type)
        assertNull(album?.importance)
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
        val band=bandRepository.saveAndFlush(Band().apply {name="Metallica"})

        repeat(10) {i->
            genreRepository.saveAndFlush(
                Genre().apply {id=i+1; name="Genre $i"; properties="1253420"})
        }

        repeat(Utils.LIMIT_BASIC) {i->
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
        val band=bandRepository.save(Band().apply {name="Metallica"})
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
    fun `getAlbumAnniversariesByDate should throw exception for invalid date`() {
        assertThrows<jakarta.servlet.ServletException> {
            mockMvc.get("/api/album/inDate") {
                param("month","13")
                param("day","1")
            }
        }
    }

    @Test
    fun `getAlbumsByBandId should return albums for existing band`() {
        val band=bandRepository.save(Band().apply {name="Metallica"})
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
    fun `getAlbumsByBandId should throw exception for non-existent band`() {
        assertThrows<jakarta.servlet.ServletException> {
            mockMvc.get("/api/album/band/999")
        }
    }

    @Test
    fun `getAlbumsByBandNameLike should return albums`() {
        val band=bandRepository.save(Band().apply {name="Metallica"})
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
    fun `getAlbumsByYear should throw exception for invalid year`() {
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

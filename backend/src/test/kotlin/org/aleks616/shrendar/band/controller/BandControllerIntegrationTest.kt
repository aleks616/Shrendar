package org.aleks616.shrendar.band.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.ArtistBandAddDto
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandAddDto
import org.aleks616.shrendar.band.model.Status
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.common.model.Country
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BandControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc:MockMvc

    @Autowired
    private lateinit var bandRepository:BandRepository

    @Autowired
    private lateinit var bandsMemberRepository:BandsMemberRepository

    @Autowired
    private lateinit var artistRepository:ArtistRepository

    @Autowired
    private lateinit var contributionRepository:ContributionRepository

    @Autowired
    private lateinit var userRepository:UserRepository

    @Autowired
    private lateinit var rankRepository:RankRepository

    @Autowired
    private lateinit var countryRepository:CountryRepository

    @Autowired
    private lateinit var rateLimiter:RateLimiter

    private val objectMapper=ObjectMapper().findAndRegisterModules()
    private lateinit var userToken:String

    @BeforeEach
    fun setup() {
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

    @Test
    fun `addBand should work for authorized user`() {
        val countryId=countryRepository.saveAndFlush(Country().apply { name="USA" }).id!!
        val bandAddDto=BandAddDto(
            name="Metallica",
            formedYear=1981,
            status=Status.active,
            country=countryId,
            description="Thrash legends",
            imageUrl="https://image.com"
        )

        mockMvc.post("/api/band/add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(bandAddDto)
        }.andExpect {
            status {isOk()}
            content {string("Band addition request received")}
        }

        val band=bandRepository.findAll().find {it.name=="Metallica"}
        assertNotNull(band)
    }

    @Test
    fun `addBand should return unauthorized for missing token`() {
        mockMvc.post("/api/band/add") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(BandAddDto(name="Unknown",imageUrl=null))
        }.andExpect {
            status {isForbidden()}
        }
    }

    @Test
    fun `addBandMember should work with null nickname`() {
        val band=bandRepository.saveAndFlush(Band().apply { name="Metallica" })
        val artist=artistRepository.saveAndFlush(Artist().apply { name="James Hetfield" })

        val memberAddDto=ArtistBandAddDto(
            bandId=band.id,
            artistId=artist.id,
            role="Vocals",
            joinedYear=1981,
            nickname=null
        )

        mockMvc.post("/api/band/member-add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(memberAddDto)
        }.andExpect {
            status {isOk()}
            content {string("Band member addition request received")}
        }

        val member=bandsMemberRepository.findAll().find { it.artist?.id == artist.id && it.band?.id == band.id }
        assertNotNull(member)
        assertNull(member?.nickname)
    }

    @Test
    fun `addBandMember should work with nickname`() {
        val band=bandRepository.saveAndFlush(Band().apply { name="Metallica" })
        val artist=artistRepository.saveAndFlush(Artist().apply { name="James Hetfield" })

        val memberAddDto=ArtistBandAddDto(
            bandId=band.id,
            artistId=artist.id,
            role="Vocals",
            joinedYear=1981,
            nickname="Papa Het"
        )

        mockMvc.post("/api/band/member-add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(memberAddDto)
        }.andExpect {
            status {isOk()}
        }

        val member=bandsMemberRepository.findAll().find { it.artist?.id == artist.id && it.band?.id == band.id }
        assertNotNull(member)
        assertEquals("Papa Het",member?.nickname)
    }

    @Test
    fun `addBandMember should return too many requests when rate limit reached`() {
        val band=bandRepository.saveAndFlush(Band().apply { name="Metallica" })
        val artist=artistRepository.saveAndFlush(Artist().apply { name="James Hetfield" })
        val memberAddDto=ArtistBandAddDto(bandId=band.id,artistId=artist.id,role="Vocals",joinedYear=1981)

        repeat(20) {
            mockMvc.post("/api/band/member-add") {
                header("Authorization","Bearer $userToken")
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(memberAddDto)
            }
        }

        mockMvc.post("/api/band/member-add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(memberAddDto)
        }.andExpect {
            status {isTooManyRequests()}
        }
    }
}

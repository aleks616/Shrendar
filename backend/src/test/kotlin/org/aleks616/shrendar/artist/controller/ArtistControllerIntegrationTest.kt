package org.aleks616.shrendar.artist.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.common.Utils
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDate


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArtistControllerIntegrationTest{

    @Autowired
    private lateinit var mockMvc:MockMvc

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
            action=Action.create
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
    fun `addArtist should work for authorized user`() {
        val countryId=countryRepository.saveAndFlush(Country().apply { name="USA" }).id!!
        val artistAddDto=ArtistAddDto(
            name="James Hetfield",
            birthDate=LocalDate.of(1963,8,3),
            gender='M',
            country=countryId,
            description="Metallica frontman"
        )

        mockMvc.post("/api/artist/add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(artistAddDto)
        }.andExpect {
            status {isOk()}
            content {string("Artist addition request received")}
        }

        val artist=artistRepository.findAll().find {it.name=="James Hetfield"}
        assertNotNull(artist)

        val changeId=contributionRepository.findAll().find {it.changedTable=="artist"}?.changeId
        assertNotNull(changeId)
    }

    @Test
    fun `addArtist should return unauthorized for missing token`() {
        val artistAddDto=ArtistAddDto(name="Unknown")

        mockMvc.post("/api/artist/add") {
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(artistAddDto)
        }.andExpect {
            status {isForbidden()}
        }
    }

    @Test
    fun `addArtist should return too many requests when rate limit reached`() {
        val artistAddDto=ArtistAddDto(name="Fast Artist")

        repeat(Utils.LIMIT) {
            mockMvc.post("/api/artist/add") {
                header("Authorization","Bearer $userToken")
                contentType=MediaType.APPLICATION_JSON
                content=objectMapper.writeValueAsString(artistAddDto)
            }
        }

        mockMvc.post("/api/artist/add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(artistAddDto)
        }.andExpect {
            status {isTooManyRequests()}
        }
    }

    @Test
    fun `addArtist should fail when user reaches weekly contribution limit`() {
        val rank=rankRepository.findById(1).get()
        rank.allowedContributions=0
        rankRepository.saveAndFlush(rank)

        val artistAddDto=ArtistAddDto(name="Limited Artist")

        mockMvc.post("/api/artist/add") {
            header("Authorization","Bearer $userToken")
            contentType=MediaType.APPLICATION_JSON
            content=objectMapper.writeValueAsString(artistAddDto)
        }.andExpect {
            status {isForbidden()}
            content {string("ContributionLimitExceededException You have reached your weekly limit. Limit for rank 1 is 0")}
        }
    }
}

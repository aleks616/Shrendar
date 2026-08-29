package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.band.model.ArtistBandsStatusDto
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.common.model.Country
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.ContributionDto
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UserLog
import org.aleks616.shrendar.user.model.UsersArtists
import org.aleks616.shrendar.user.model.UsersBands
import org.aleks616.shrendar.user.model.UsersGenres
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.repository.UserLogRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional

class UserServiceTest {

    private val userRepository:UserRepository=mock(UserRepository::class.java)
    private val userBandRepository:UserBandRepository=mock(UserBandRepository::class.java)
    private val userArtistRepository:UserArtistRepository=mock(UserArtistRepository::class.java)
    private val userGenreRepository:UserGenreRepository=mock(UserGenreRepository::class.java)
    private val contributionService:ContributionService=mock(ContributionService::class.java)
    private val countryRepository:CountryRepository=mock(CountryRepository::class.java)
    private val userLogRepository:UserLogRepository=mock(UserLogRepository::class.java)
    private val bandsMemberService:BandsMemberService=mock(BandsMemberService::class.java)
    private lateinit var userService:UserService

    @BeforeEach
    fun setup() {
        userService=UserService(
            userRepository,
            userBandRepository,
            userArtistRepository,
            userGenreRepository,
            contributionService,
            countryRepository,
            userLogRepository,
            bandsMemberService
        )
    }

    @Test
    fun `getUserProfile should aggregate favorite data and contribution list`() {
        val targetUser=User().apply {
            id=7
            login="james"
            username="James"
            rank=Rank().apply { id=2; name="Member" }
            bio="Metallica vocalist"
        }
        val targetBand=Band().apply { id=4; name="Metallica"; country=1 }
        val targetArtist=Artist().apply { id=9; name="James Hetfield" }
        val targetGenre=org.aleks616.shrendar.genre.model.Genre().apply { id=5; name="Thrash Metal" }
        val bandRow=UsersBands().apply { user=targetUser; band=targetBand }
        val artistRow=UsersArtists().apply { user=targetUser; artist=targetArtist }
        val genreRow=UsersGenres().apply { user=targetUser; genre=targetGenre }

        `when`(userRepository.findByLogin("james")).thenReturn(targetUser)
        `when`(userBandRepository.findByUser(targetUser)).thenReturn(mutableListOf(bandRow))
        `when`(countryRepository.findById(1)).thenReturn(Optional.of(Country().apply { id=1; name="USA" }))
        `when`(userArtistRepository.findByUser(targetUser)).thenReturn(mutableListOf(artistRow))
        `when`(bandsMemberService.getArtistBandsList(9L)).thenReturn(listOf(
            ArtistBandsStatusDto(artistId=9,artistName="James Hetfield",bandId=4,bandName="Metallica",current=true)
        ))
        `when`(userGenreRepository.findByUser(targetUser)).thenReturn(mutableListOf(genreRow))
        `when`(contributionService.getContributionsByRequestingUser(7)).thenReturn(emptyList())
        `when`(userLogRepository.getUserLogById(7)).thenReturn(UserLog().apply {
            accountCreatedTime=Instant.now().minus(400, ChronoUnit.DAYS)
            lastLoginTime=Instant.now().minus(45, ChronoUnit.DAYS)
        })

        val profile=userService.getUserProfile("james")

        assertEquals("james", profile.login)
        assertEquals("Metallica vocalist", profile.bio)
        assertEquals(1, profile.favoriteBands!!.size)
        assertEquals("USA", profile.favoriteBands!!.first().country)
        assertEquals(1, profile.favoriteArtists!!.size)
        assertEquals("James Hetfield", profile.favoriteArtists!!.first().name)
        assertEquals(1, profile.favoriteGenres!!.size)
        assertEquals("Thrash Metal", profile.favoriteGenres!!.first().name)
        assertEquals(emptyList<ContributionDto>(), profile.contributions)
    }

    @Test
    fun `timeSinceAccountCreated should format multi-year account age`() {
        `when`(userLogRepository.getUserLogById(7)).thenReturn(
            UserLog().apply { accountCreatedTime=Instant.now().minus(400, ChronoUnit.DAYS) }
        )

        assertEquals("1 year 1 months", userService.timeSinceAccountCreated(7))
    }


    @Test
    fun `timeSinceAccountCreated should format exactly 1 year`() {
        `when`(userLogRepository.getUserLogById(2)).thenReturn(
            UserLog().apply { accountCreatedTime=Instant.now().minus(365, ChronoUnit.DAYS) }
        )

        assertEquals("1 year 0 months", userService.timeSinceAccountCreated(2))
    }

    @Test
    fun `timeSinceAccountCreated should format months and days when less than a year`() {
        `when`(userLogRepository.getUserLogById(4)).thenReturn(
            UserLog().apply { accountCreatedTime=Instant.now().minus(95, ChronoUnit.DAYS) }
        )

        assertEquals("3 months 5 days", userService.timeSinceAccountCreated(4))
    }

    @Test
    fun `timeSinceAccountCreated should format only months when less than a year without days`() {
        `when`(userLogRepository.getUserLogById(5)).thenReturn(
            UserLog().apply { accountCreatedTime=Instant.now().minus(90, ChronoUnit.DAYS) }
        )

        assertEquals("3 months 0 days", userService.timeSinceAccountCreated(5))
    }

    @Test
    fun `timeSinceAccountCreated should format only days when less than a month`() {
        `when`(userLogRepository.getUserLogById(6)).thenReturn(
            UserLog().apply { accountCreatedTime=Instant.now().minus(15, ChronoUnit.DAYS) }
        )

        assertEquals("15 days", userService.timeSinceAccountCreated(6))
    }

    @Test
    fun `timeSinceAccountCreated should format single day`() {
        `when`(userLogRepository.getUserLogById(8)).thenReturn(
            UserLog().apply { accountCreatedTime=Instant.now().minus(1, ChronoUnit.DAYS) }
        )

        assertEquals("1 days", userService.timeSinceAccountCreated(8))
    }

    @Test
    fun `timeSinceAccountCreated should format zero days for today`() {
        `when`(userLogRepository.getUserLogById(9)).thenReturn(
            UserLog().apply { accountCreatedTime=Instant.now().minus(0, ChronoUnit.DAYS) }
        )

        assertEquals("0 days", userService.timeSinceAccountCreated(9))
    }

    @Test
    fun `timeSinceLogin should format login age in years`() {
        `when`(userLogRepository.getUserLogById(7)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(730, ChronoUnit.DAYS) }
        )

        assertEquals("2 years ago", userService.timeSinceLogin(7))
    }

    @Test
    fun `timeSinceLogin should format login age in months`() {
        `when`(userLogRepository.getUserLogById(10)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(45, ChronoUnit.DAYS) }
        )

        assertEquals("15 months ago", userService.timeSinceLogin(10))
    }

    @Test
    fun `timeSinceLogin should format login age in days`() {
        `when`(userLogRepository.getUserLogById(11)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(15, ChronoUnit.DAYS) }
        )

        assertEquals("15 days ago", userService.timeSinceLogin(11))
    }

    @Test
    fun `timeSinceLogin should format login today`() {
        `when`(userLogRepository.getUserLogById(12)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(0, ChronoUnit.DAYS) }
        )

        assertEquals("0 today ago", userService.timeSinceLogin(12))
    }

    @Test
    fun `timeSinceLogin should format login 365 days ago in months`() {
        `when`(userLogRepository.getUserLogById(13)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(365, ChronoUnit.DAYS) }
        )

        assertEquals("5 months ago", userService.timeSinceLogin(13))
    }

    @Test
    fun `timeSinceLogin should format login 4 years ago`() {
        `when`(userLogRepository.getUserLogById(14)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(1460, ChronoUnit.DAYS) }
        )

        assertEquals("4 years ago", userService.timeSinceLogin(14))
    }

    @Test
    fun `timeSinceLogin should format login 30 days ago in days`() {
        `when`(userLogRepository.getUserLogById(15)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(30, ChronoUnit.DAYS) }
        )

        assertEquals("30 days ago", userService.timeSinceLogin(15))
    }

    @Test
    fun `timeSinceLogin should format login 1 day ago`() {
        `when`(userLogRepository.getUserLogById(16)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(1, ChronoUnit.DAYS) }
        )

        assertEquals("1 days ago", userService.timeSinceLogin(16))
    }

    @Test
    fun `timeSinceLogin should format login 100 days ago in months`() {
        `when`(userLogRepository.getUserLogById(17)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(100, ChronoUnit.DAYS) }
        )

        assertEquals("10 months ago", userService.timeSinceLogin(17))
    }

    @Test
    fun `timeSinceLogin should format login 31 days ago in months`() {
        `when`(userLogRepository.getUserLogById(18)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(31, ChronoUnit.DAYS) }
        )

        assertEquals("1 months ago", userService.timeSinceLogin(18))
    }

    @Test
    fun `timeSinceLogin should format login 366 days ago in years`() {
        `when`(userLogRepository.getUserLogById(19)).thenReturn(
            UserLog().apply { lastLoginTime=Instant.now().minus(366, ChronoUnit.DAYS) }
        )

        assertEquals("1 years ago", userService.timeSinceLogin(19))
    }
}

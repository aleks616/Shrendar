package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.model.ChineseZodiacSign
import org.aleks616.shrendar.artist.model.ZodiacSign
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersArtists
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.LocalDate

class ArtistServiceTest {
    private lateinit var artistRepository:ArtistRepository
    private lateinit var countryRepository:CountryRepository
    private lateinit var userService:UserService
    private lateinit var contributionRepository:ContributionRepository
    private lateinit var rankService:RankService
    private lateinit var userArtistRepository:UserArtistRepository
    private lateinit var service:ArtistService
    private lateinit var artist:Artist
    private lateinit var requester:User

    @BeforeEach
    fun setup() {
        artistRepository=mock(ArtistRepository::class.java)
        countryRepository=mock(CountryRepository::class.java)
        userService=mock(UserService::class.java)
        contributionRepository=mock(ContributionRepository::class.java)
        rankService=mock(RankService::class.java)
        userArtistRepository=mock(UserArtistRepository::class.java)
        service=ArtistService(
            artistRepository,countryRepository,userService,contributionRepository,rankService,userArtistRepository
        )
        artist=Artist().apply {
            id=1
            name="James Hetfield"
            birthDate=LocalDate.of(1963,8,3)
            gender='M'
            country=1
            description="Metallica frontman"
            artistImageUrl="https://example.com/james.jpg"
        }
        requester=User().apply {
            id=7
            login="tester"
            rank=Rank().apply {id=1}
        }
    }

    @Test
    fun `getAll returns repository artists`() {
        `when`(artistRepository.findAll()).thenReturn(listOf(artist))
        assertEquals(listOf(artist),service.getAll())
    }

    @Test
    fun `getById returns repository artist`() {
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        assertSame(artist,service.getById(1))
    }

    @Test
    fun `getById throws when artist does not exist`() {
        `when`(artistRepository.existsArtistById(1)).thenReturn(false)
        assertThrows<IllegalArgumentException> {service.getById(1)}
    }

    @Test
    fun `getByIdWiki maps living artist`() {
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1)).thenReturn(artist)
        `when`(countryRepository.getCountryNameById(1)).thenReturn("USA")
        val result=service.getByIdWiki(1)
        assertEquals(artist.name,result.name)
        assertEquals("Male",result.gender)
        assertEquals("USA",result.country)
        assertEquals(ZodiacSign.LEO,result.zodiacSign)
        assertEquals(ChineseZodiacSign.RABBIT,result.chineseZodiacSign)
        assertNull(result.deathDate)
        assertNull(result.daysTillDeathAnniversary)
    }

    @Test
    fun `getByIdWiki calculates deceased artist age`() {
        artist.deathDate=LocalDate.of(2020,9,27)
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1)).thenReturn(artist)
        `when`(countryRepository.getCountryNameById(1)).thenReturn("USA")
        val result=service.getByIdWiki(1)
        assertEquals(57,result.age)
        assertNotNull(result.daysTillDeathAnniversary)
    }

    @Test
    fun `getByNameLike delegates to repository`() {
        `when`(artistRepository.findArtistByNameContains("James")).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByNameLike("James"))
    }

    @Test
    fun `getByFirstName delegates to repository`() {
        `when`(artistRepository.findArtistByNameStartsWith("James")).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByFirstName("James"))
    }

    @Test
    fun `getByLastName delegates to repository`() {
        `when`(artistRepository.findArtistByNameEndsWithIgnoreCase("Hetfield")).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByLastName("Hetfield"))
    }

    @Test
    fun `getByBirthday delegates to repository`() {
        `when`(artistRepository.findArtistByBirthDate(8,3)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByBirthday(8,3))
    }

    @Test
    fun `getByDeathDate delegates to repository`() {
        `when`(artistRepository.findArtistByDeathDate(9,27)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByDeathDate(9,27))
    }

    @Test
    fun `getByBirthdayBetween delegates to repository`() {
        `when`(artistRepository.findArtistByBirthdayBetween(1,1,12,31)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByBirthdayBetween(1,1,12,31))
    }

    @Test
    fun `getByBirthYear delegates to repository`() {
        `when`(artistRepository.findArtistsByBirthYear(1963)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByBirthYear(1963))
    }

    @Test
    fun `getByBirthYearBetween delegates to repository`() {
        `when`(artistRepository.findArtistsByBirthYearBetween(1960,1970)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByBirthYearBetween(1960,1970))
    }

    @Test
    fun `getByCountry delegates to repository`() {
        `when`(artistRepository.findArtistByCountry(1)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),service.getByCountry(1))
    }

    @Test
    fun `getRecentDeathsAnniversaries delegates to repository`() {
        `when`(artistRepository.findArtistByDeathDateBetween(anyInt(),anyInt(),anyInt(),anyInt())).thenReturn(
            mutableListOf(artist)
        )
        assertEquals(listOf(artist),service.getRecentDeathsAnniversaries())
    }

    @Test
    fun `getRecentBirthdays delegates to repository`() {
        `when`(artistRepository.findArtistByBirthdayBetween(anyInt(),anyInt(),anyInt(),anyInt())).thenReturn(
            mutableListOf(artist)
        )
        assertEquals(listOf(artist),service.getRecentBirthdays())
    }

    @Test
    fun `getZodiacSign returns every zodiac sign`() {
        val expectedByDate=mapOf(
            (12 to 22) to ZodiacSign.CAPRICORN,
            (1 to 1) to ZodiacSign.CAPRICORN,
            (1 to 20) to ZodiacSign.AQUARIUS,
            (2 to 1) to ZodiacSign.AQUARIUS,
            (2 to 18) to ZodiacSign.PISCES,
            (3 to 1) to ZodiacSign.PISCES,
            (3 to 20) to ZodiacSign.ARIES,
            (4 to 1) to ZodiacSign.ARIES,
            (4 to 20) to ZodiacSign.TAURUS,
            (5 to 1) to ZodiacSign.TAURUS,
            (5 to 21) to ZodiacSign.GEMINI,
            (6 to 1) to ZodiacSign.GEMINI,
            (6 to 21) to ZodiacSign.CANCER,
            (7 to 1) to ZodiacSign.CANCER,
            (7 to 23) to ZodiacSign.LEO,
            (8 to 1) to ZodiacSign.LEO,
            (8 to 23) to ZodiacSign.VIRGO,
            (9 to 1) to ZodiacSign.VIRGO,
            (9 to 23) to ZodiacSign.LIBRA,
            (10 to 1) to ZodiacSign.LIBRA,
            (10 to 23) to ZodiacSign.SCORPIO,
            (11 to 1) to ZodiacSign.SCORPIO,
            (11 to 22) to ZodiacSign.SAGITTARIUS,
            (12 to 1) to ZodiacSign.SAGITTARIUS
        )

        expectedByDate.forEach {(date,expected)->
            assertEquals(expected,service.getZodiacSign(date.first,date.second))
        }
    }

    @Test
    fun `getZodiacSign throws for invalid date`() {
        assertThrows<IllegalArgumentException> {service.getZodiacSign(13,1)}
    }

    @Test
    fun `getChineseZodiacSign returns every zodiac sign`() {
        val expectedByYear=mapOf(
            1984 to ChineseZodiacSign.RAT,
            1985 to ChineseZodiacSign.OX,
            1986 to ChineseZodiacSign.TIGER,
            1987 to ChineseZodiacSign.RABBIT,
            1988 to ChineseZodiacSign.DRAGON,
            1989 to ChineseZodiacSign.SNAKE,
            1990 to ChineseZodiacSign.HORSE,
            1991 to ChineseZodiacSign.GOAT,
            1992 to ChineseZodiacSign.MONKEY,
            1993 to ChineseZodiacSign.ROOSTER,
            1994 to ChineseZodiacSign.DOG,
            1995 to ChineseZodiacSign.PIG
        )

        expectedByYear.forEach {(year,expected)->
            assertEquals(expected,service.getChineseZodiacSign(year))
        }
    }

    @Test
    fun `addArtistRequest throws contribution limit exception`() {
        `when`(userService.getUserByLogin("tester")).thenReturn(requester)
        `when`(rankService.checkRank(requester)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {
            service.addArtistRequest(ArtistAddDto(name="Artist"),"tester")
        }
        verifyNoInteractions(artistRepository,contributionRepository)
    }

    @Test
    fun `addArtistRequest saves artist and contributions`() {
        val dto=ArtistAddDto(
            name="New Artist",birthDate=LocalDate.of(1980,1,1),gender='M',country=1,
            description="Description",artistImageUrl="https://example.com/artist.jpg"
        )
        stubAddDependencies()
        service.addArtistRequest(dto,"tester")
        val saved=ArgumentCaptor.forClass(Artist::class.java)
        verify(artistRepository).save(saved.capture())
        assertEquals(dto.name,saved.value.name)
        verify(contributionRepository,times(7)).save(any(Contribution::class.java))
    }

    @Test
    fun `addArtistRequest marks trusted contributions confirmed`() {
        requester.rank=Rank().apply {id=10}
        val dto=ArtistAddDto(name="New Artist")
        stubAddDependencies()
        service.addArtistRequest(dto,"tester")
        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(saved.capture())
        assertTrue(saved.value.confirmed==true&&saved.value.confirmedBy==requester.id)
    }

    @Test
    fun `editArtistRequest throws when there are no changes`() {
        stubEditDependencies()
        assertThrows<IllegalStateException> {service.editArtistRequest(ArtistAddDto(id=1),"tester")}
        verify(artistRepository,never()).save(any(Artist::class.java))
    }

    @Test
    fun `editArtistRequest updates changed values and logs changes`() {
        stubEditDependencies()
        service.editArtistRequest(ArtistAddDto(id=1,name="New Name",gender='X'),"tester")
        assertEquals("New Name",artist.name)
        assertEquals('X',artist.gender)
        verify(artistRepository).save(artist)
        verify(contributionRepository,times(2)).save(any(Contribution::class.java))
    }

    @Test
    fun `editArtistRequest marks trusted contributions confirmed`() {
        requester.rank=Rank().apply {id=10}
        stubEditDependencies()

        service.editArtistRequest(ArtistAddDto(id=1,name="Trusted Name"),"tester")

        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository).save(saved.capture())
        assertTrue(saved.value.confirmed==true&&saved.value.confirmedBy==requester.id)
    }

    @Test
    fun `doesArtistExist delegates to repository`() {
        `when`(artistRepository.existsById(1L)).thenReturn(true)
        `when`(artistRepository.existsById(2L)).thenReturn(false)

        assertTrue(service.doesArtistExist(1L))
        assertFalse(service.doesArtistExist(2L))
    }

    @Test
    fun `deleteArtistRequest does not delete untrusted user`() {
        `when`(userService.getUserByLogin("tester")).thenReturn(requester)
        `when`(rankService.checkRank(requester)).thenReturn(null)
        service.deleteArtistRequest(1,"tester",log=false)
        verify(artistRepository,never()).deleteById(1L)
        verifyNoInteractions(contributionRepository)
    }

    @Test
    fun `deleteArtistRequest logs and deletes trusted user`() {
        requester.rank=Rank().apply {id=10}
        `when`(userService.getUserByLogin("tester")).thenReturn(requester)
        `when`(rankService.checkRank(requester)).thenReturn(null)
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        service.deleteArtistRequest(1,"tester")
        verify(artistRepository).deleteById(1L)
        verify(contributionRepository,times(8)).save(any(Contribution::class.java))
    }

    @Test
    fun `toggleFavoriteArtist removes existing favorite`() {
        val favorite=UsersArtists().apply {id=4}
        `when`(userService.getUserByLogin("tester")).thenReturn(requester)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        var lookupCount=0
        `when`(userArtistRepository.findByArtistAndUser(artist,requester))
            .thenAnswer {if(lookupCount++==0) favorite else null}
        doReturn(UsersArtists()).`when`(userArtistRepository).saveAndFlush(any(UsersArtists::class.java))

        service.toggleFavoriteArtist(1L,"tester")
        service.toggleFavoriteArtist(1L,"tester")

        verify(userArtistRepository).deleteById(4)
        verify(userArtistRepository).saveAndFlush(any(UsersArtists::class.java))
    }

    private fun stubAddDependencies() {
        `when`(userService.getUserByLogin("tester")).thenReturn(requester)
    }

    private fun stubEditDependencies() {
        `when`(userService.getUserByLogin("tester")).thenReturn(requester)
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1)).thenReturn(artist)
    }
}

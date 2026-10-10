package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.model.ArtistGenreDto
import org.aleks616.shrendar.common.model.NameValue
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersArtists
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserAccountService
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
    private lateinit var userAccountService:UserAccountService
    private lateinit var contributionRepository:ContributionRepository
    private lateinit var rankService:RankService
    private lateinit var userArtistRepository:UserArtistRepository
    private lateinit var bandsMemberRepository:BandsMemberRepository
    private lateinit var artistService:ArtistService
    private lateinit var artist:Artist
    private lateinit var artist1:Artist
    private lateinit var artist2:Artist
    private lateinit var requestingUser:User

    @BeforeEach
    fun setup() {
        artistRepository=mock(ArtistRepository::class.java)
        countryRepository=mock(CountryRepository::class.java)
        userAccountService=mock(UserAccountService::class.java)
        contributionRepository=mock(ContributionRepository::class.java)
        rankService=mock(RankService::class.java)
        userArtistRepository=mock(UserArtistRepository::class.java)
        bandsMemberRepository=mock(BandsMemberRepository::class.java)
        artistService=ArtistService(
            artistRepository,
            countryRepository,
            userAccountService,
            contributionRepository,
            rankService,
            userArtistRepository,
            bandsMemberRepository
        )
        artist=Artist().apply {
            id=1
            name="James Hetfield"
            birthDate=LocalDate.of(1963,8,3)
            deathDate=LocalDate.of(2025,9,27)
            gender='M'
            country=1
            description="Metallica frontman"
        }
        artist1=Artist().apply {
            id=2
            name="Some Woman"
            birthDate=LocalDate.of(1969,6,9)
            gender='F'
            country=1
            description="idk"
            artistImageUrl="https://example.com/someone.jpg"
        }
        artist2=Artist().apply {
            id=3
            name="Satan"
            birthDate=LocalDate.of(1950,9,11)
            gender=null
            country=1
            description="satan"
            artistImageUrl="https://example.com/someone.jpg"
        }
        requestingUser=User().apply {
            id=7
            login="tester"
            rank=Rank().apply {id=1}
        }
    }

    @Test
    fun `getAll should return repository artists`() {
        `when`(artistRepository.findAll()).thenReturn(listOf(artist))
        assertEquals(listOf(artist),artistService.getAll())
    }

    @Test
    fun `getById should return repository artist`() {
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        assertSame(artist,artistService.getById(1))
    }

    @Test
    fun `getById should throw IllegalArgumentException when artist does not exist`() {
        `when`(artistRepository.existsArtistById(1)).thenReturn(false)
        assertThrows<IllegalArgumentException> {artistService.getById(1)}
    }

    @Test
    fun `getByNameLike should delegate to repository`() {
        `when`(artistRepository.findArtistByNameContains("James")).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),artistService.getByNameLike("James"))
    }

    @Test
    fun `getByFirstName should delegate to repository`() {
        `when`(artistRepository.findArtistByNameStartsWith("James")).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),artistService.getByFirstName("James"))
    }

    @Test
    fun `getByLastName should delegate to repository`() {
        `when`(artistRepository.findArtistByNameEndsWithIgnoreCase("Hetfield")).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),artistService.getByLastName("Hetfield"))
    }

    @Test
    fun `getByBirthday should delegate to repository`() {
        `when`(artistRepository.findArtistByBirthDate(8,3)).thenReturn(mutableListOf(artist))
        val result=artistService.getByBirthday(8,3)
        assertEquals(1,result[0].id)
    }

    @Test
    fun `getByDeathDate should delegate to repository`() {
        `when`(artistRepository.findArtistByDeathDate(9,27)).thenReturn(mutableListOf(artist))
        val result=artistService.getByDeathDate(9,27)
        assertEquals(1,result[0].id)
    }

    @Test
    fun `getByBirthdayBetween should delegate to repository`() {
        `when`(artistRepository.findArtistByBirthdayBetween(1,1,12,31)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),artistService.getByBirthdayBetween(1,1,12,31))
    }

    @Test
    fun `getByBirthYear should delegate to repository`() {
        `when`(artistRepository.findArtistsByBirthYear(1963)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),artistService.getByBirthYear(1963))
    }

    @Test
    fun `getByBirthYearBetween should delegate to repository`() {
        `when`(artistRepository.findArtistsByBirthYearBetween(1960,1970)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),artistService.getByBirthYearBetween(1960,1970))
    }

    @Test
    fun `getByCountry should delegate to repository`() {
        `when`(artistRepository.findArtistByCountry(1)).thenReturn(mutableListOf(artist))
        assertEquals(listOf(artist),artistService.getByCountry(1))
    }

    @Test
    fun `getRecentDeathsAnniversaries should delegate to repository`() {
        `when`(artistRepository.findArtistByDeathDateBetween(anyInt(),anyInt(),anyInt(),anyInt())).thenReturn(
            mutableListOf(artist)
        )
        assertEquals(listOf(artist),artistService.getRecentDeathsAnniversaries())
    }

    @Test
    fun `getRecentBirthdays should delegate to repository`() {
        `when`(artistRepository.findArtistByBirthdayBetween(anyInt(),anyInt(),anyInt(),anyInt())).thenReturn(
            mutableListOf(artist)
        )
        assertEquals(listOf(artist),artistService.getRecentBirthdays())
    }

    @Test
    fun `addArtistRequest should throw contribution limit exception`() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {
            artistService.addArtistRequest(ArtistAddDto(name="Artist"),"tester")
        }
        verifyNoInteractions(artistRepository,contributionRepository)
    }

    @Test
    fun `addArtistRequest should save artist and contributions`() {
        val dto=ArtistAddDto(
            name="New Artist",birthDate=LocalDate.of(1980,1,1),gender='M',country=1,
            description="Description",artistImageUrl="https://example.com/artist.jpg"
        )
        stubAddDependencies()
        `when`(contributionRepository.findTopChangeId()).thenReturn(1)
        artistService.addArtistRequest(dto,"tester")
        val saved=ArgumentCaptor.forClass(Artist::class.java)
        verify(artistRepository).save(saved.capture())
        assertEquals(dto.name,saved.value.name)
        verify(contributionRepository,times(7)).save(any(Contribution::class.java))
    }

    @Test
    fun `addArtistRequest should mark trusted contributions confirmed`() {
        requestingUser.rank=Rank().apply {id=10}
        val dto=ArtistAddDto(name="New Artist")
        stubAddDependencies()
        artistService.addArtistRequest(dto,"tester")
        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(saved.capture())
        assertTrue(saved.value.confirmed==true&&saved.value.confirmedBy==requestingUser.id)
    }

    @Test
    fun `editArtistRequest should throw IllegalStateException when there are no changes`() {
        stubEditDependencies()
        assertThrows<IllegalStateException> {artistService.editArtistRequest(ArtistAddDto(id=1),"tester")}
        verify(artistRepository,never()).save(any(Artist::class.java))
    }

    @Test
    fun `editArtistRequest should throw contribution limit exception`() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {
            artistService.editArtistRequest(ArtistAddDto(name="Artist"),"tester")
        }
        verifyNoInteractions(artistRepository,contributionRepository)
    }

    @Test
    fun `editArtistRequest should update changed values and log changes`() {
        stubEditDependencies()
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
        artistService.editArtistRequest(ArtistAddDto(id=1,name="New Name",gender='X',country=1,description=null,artistImageUrl="https://example.org/img.jpg"),"tester")
        assertEquals("New Name",artist.name)
        assertEquals('X',artist.gender)
        verify(artistRepository).save(artist)
        verify(contributionRepository,times(3)).save(any(Contribution::class.java))
    }

    @Test
    fun `editArtistRequest should mark trusted contributions confirmed`() {
        requestingUser.rank=Rank().apply {id=10}
        stubEditDependencies()
        `when`(contributionRepository.findTopChangeId()).thenReturn(1)
        artistService.editArtistRequest(ArtistAddDto(id=1,name="Trusted Name"),"tester")

        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository).save(saved.capture())
        assertTrue(saved.value.confirmed==true&&saved.value.confirmedBy==requestingUser.id)
    }

    @Test
    fun `doesArtistExist should delegate to repository`() {
        `when`(artistRepository.existsById(1L)).thenReturn(true)
        `when`(artistRepository.existsById(2L)).thenReturn(false)

        assertTrue(artistService.doesArtistExist(1L))
        assertFalse(artistService.doesArtistExist(2L))
    }

    @Test
    fun `deleteArtistRequest should log and delete for trusted user`() {
        requestingUser.rank=Rank().apply {id=10}
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
        artistService.deleteArtistRequest(1,"tester")
        verify(artistRepository).deleteById(1L)
        verify(contributionRepository,times(8)).save(any(Contribution::class.java))
    }

    @Test
    fun `deleteArtistRequest should log and delete for trusted user v2`() {
        requestingUser.rank=Rank().apply {id=10}
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        `when`(contributionRepository.findTopChangeId()).thenReturn(3)
        artistService.deleteArtistRequest(1,"tester")
        verify(artistRepository).deleteById(1L)
        verify(contributionRepository,times(8)).save(any(Contribution::class.java))
    }

    @Test
    fun `deleteArtistRequest should not delete untrusted user`() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        artistService.deleteArtistRequest(1,"tester",log=false)
        verify(artistRepository,never()).deleteById(1L)
        verifyNoInteractions(contributionRepository)
    }

    @Test
    fun `deleteArtistRequest should throw contribution limit exception`() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {
            artistService.deleteArtistRequest(1,"tester",log=false)
        }
        verifyNoInteractions(artistRepository,contributionRepository)
    }

    @Test
    fun `toggleFavoriteArtist should remove existing favorite`() {
        val favorite=UsersArtists().apply {id=4}
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        var lookupCount=0
        `when`(userArtistRepository.findByArtistAndUser(artist,requestingUser))
            .thenAnswer {if(lookupCount++==0) favorite else null}
        doReturn(UsersArtists()).`when`(userArtistRepository).saveAndFlush(any(UsersArtists::class.java))

        artistService.toggleFavoriteArtist(1L,"tester")
        artistService.toggleFavoriteArtist(1L,"tester")

        verify(userArtistRepository).deleteById(4)
        verify(userArtistRepository).saveAndFlush(any(UsersArtists::class.java))
    }

    @Test
    fun `toggleFavoriteArtist should throw error for user that doesn't exist`() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(null)
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)

        assertThrows<IllegalStateException>{artistService.toggleFavoriteArtist(1L,"tester")}
    }

    @Test
    fun `toggleFavoriteArtistByBand adds every artist not already favorited`() {
        val existingFavorite=UsersArtists().apply {id=4}
        val members=mutableListOf(
            BandsMembers().apply {artist=this@ArtistServiceTest.artist},
            BandsMembers().apply {artist=this@ArtistServiceTest.artist1}
        )
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(bandsMemberRepository.findByBandId(5)).thenReturn(members)
        `when`(userArtistRepository.findByArtistAndUser(artist,requestingUser)).thenReturn(existingFavorite)
        `when`(userArtistRepository.findByArtistAndUser(artist1,requestingUser)).thenReturn(null)
        val service=spy(artistService)
        doNothing().`when`(service).toggleFavoriteArtist(artist1.id!!,"tester")

        service.toggleFavoriteArtistByBand(5,"tester")

        verify(service).toggleFavoriteArtist(artist1.id!!,"tester")
        verify(service,never()).toggleFavoriteArtist(artist.id!!,"tester")
    }

    @Test
    fun `toggleFavoriteArtistByBand throws when the user does not exist`() {
        `when`(userAccountService.getUserByLogin("missing")).thenReturn(null)

        assertThrows<IllegalStateException> {artistService.toggleFavoriteArtistByBand(5,"missing")}

        verifyNoInteractions(bandsMemberRepository,userArtistRepository)
    }

    @Test
    fun `getArtistGenres should return artist name and all matching genres`() {
        val expected=ArtistGenreDto(
            artistId=1,
            artistName="James Hetfield",
            genres=listOf(NameValue("Thrash Metal", 7))
        )
        `when`(artistRepository.findArtistById(1L)).thenReturn(artist)
        `when`(artistRepository.findArtistGenres(1L)).thenReturn(expected.genres)

        assertEquals(expected, artistService.getArtistGenres(1L))
    }

    private fun stubAddDependencies() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
    }

    private fun stubEditDependencies() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(artistRepository.existsArtistById(1)).thenReturn(true)
        `when`(artistRepository.findArtistById(1)).thenReturn(artist)
    }
}

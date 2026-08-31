package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.common.model.Country
import org.aleks616.shrendar.common.model.CountryDto
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersBands
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserAccountService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate
import java.util.*

class BandServiceTest {
    private val bandRepository=mock(BandRepository::class.java)
    private val countryRepository=mock(CountryRepository::class.java)
    private val genreService=mock(GenreService::class.java)
    private val bandsGenreRepository=mock(BandsGenreRepository::class.java)
    private val genreRepository=mock(GenreRepository::class.java)
    private val userAccountService=mock(UserAccountService::class.java)
    private val contributionRepository=mock(ContributionRepository::class.java)
    private val bandsMemberRepository=mock(BandsMemberRepository::class.java)
    private val rankService=mock(RankService::class.java)
    private val userBandRepository=mock(UserBandRepository::class.java)

    private lateinit var service:BandService
    private lateinit var band:Band
    private lateinit var requestingUser:User

    @BeforeEach
    fun setup() {
        service=BandService(
            bandRepository,
            bandsGenreRepository,
            bandsMemberRepository,
            contributionRepository,
            countryRepository,
            genreService,
            userAccountService,
            genreRepository,
            rankService,
            userBandRepository,
        )

        band=Band().apply {
            id=1
            name="Metallica"
            formedYear=1981
            status=Status.ACTIVE
            country=1
            description=null
            imageUrl="https://example.com/metallica.jpg"
            averageGenre="1111111"
        }
        requestingUser=User().apply {
            id=7
            login="user"
            rank=Rank().apply {id=1}
        }
    }

    @Test
    fun `getBandData should map band fields`() {
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))

        val result=service.getBandData(listOf(band))

        assertEquals(1,result.size)
        assertEquals(band.id,result.single().id)
        assertEquals(band.name,result.single().name)
        assertEquals(band.formedYear,result.single().formedYear)
        assertEquals(band.country,result.single().country?.id)
        assertEquals("USA",result.single().country?.name)
        assertEquals(band.description,result.single().description)
    }

    @Test
    fun `getBandsCountry should return null when no country exists`() {
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(null)
        assertNull(service.getBandsCountry(1))
    }

    @Test
    fun `getBandData should return empty list for empty input`() {
        assertTrue(service.getBandData(emptyList()).isEmpty())
    }

    @Test
    fun `getAll should return empty list when repository is empty`() {
        `when`(bandRepository.findAll()).thenReturn(emptyList())
        assertTrue(service.getAll().isEmpty())
    }

    @Test
    fun `getBandsByName should return empty list for unmatched name`() {
        `when`(bandRepository.findByNameContainingIgnoreCase("x")).thenReturn(emptyList())
        assertTrue(service.getBandsByName("x").isEmpty())
    }

    @Test
    fun `getBandsByNameExact should return empty list for unmatched name`() {
        `when`(bandRepository.findByNameIgnoreCase("x")).thenReturn(emptyList())
        assertTrue(service.getBandsByNameExact("x").isEmpty())
    }

    @Test
    fun `getBandsByCountryId should return empty list for unmatched country`() {
        `when`(bandRepository.findByCountry(1)).thenReturn(emptyList())
        assertTrue(service.getBandsByCountryId(1).isEmpty())
    }

    @Test
    fun `getBandsByFoundedBetween should return empty list when repository is empty`() {
        `when`(bandRepository.findByFormedYearBetween(2000,2020)).thenReturn(emptyList())
        assertTrue(service.getBandsByFoundedBetween(2000,2020).isEmpty())
    }

    @Test
    fun `getBandsByStatus should return empty list when repository is empty`() {
        `when`(bandRepository.findByStatus(Status.DISBANDED)).thenReturn(emptyList())
        assertTrue(service.getBandsByStatus(Status.DISBANDED).isEmpty())
    }

    @Test
    fun `getAll should map repository bands`() {
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1, "USA"))
        `when`(bandRepository.findAll()).thenReturn(listOf(band))
        assertEquals(1, service.getAll().size)
    }

    @Test
    fun `getBandById should return requested band`() {
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        assertEquals(band, service.getBandById(1))
    }

    @Test
    fun `getBandDataById should return mapped band details`() {
        `when`(bandRepository.findById(1)).thenReturn(Optional.of(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))

        val result=service.getBandDataById(1)

        assertEquals("Metallica",result.name)
        assertEquals(1,result.country?.id)
    }

    @Test
    fun `getBandDataById should throw NoSuchElementException when band is missing`() {
        `when`(bandRepository.findById(1)).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> {service.getBandDataById(1)}
    }

    @Test
    fun `getBandByIdWiki should throw NoSuchElementException when band is missing`() {
        `when`(bandRepository.findById(1)).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> {service.getBandByIdWiki(1)}
    }

    @Test
    fun `getBandByIdWiki should map wiki data`() {
        `when`(bandRepository.findById(1)).thenReturn(Optional.of(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))
        `when`(genreService.getBandAlbumGenresList(1)).thenReturn(listOf(GenreDto(id=10,name="Rock",value=8)))

        val result=service.getBandByIdWiki(1)

        assertEquals("Metallica",result.name)
        assertEquals(1981,result.formedYear)
        assertEquals(Status.ACTIVE,result.status)
        assertEquals("USA",result.country?.name)
        assertEquals("https://example.com/metallica.jpg",result.imageUrl)
        assertEquals(1,result.computedGenres?.size)
    }

    @Test
    fun `getCountryByName should return null for unknown country`() {
        assertNull(service.getCountryByName("missing"))
    }

    @Test
    fun `getCountryByName should return first matching country`() {
        `when`(countryRepository.getCountryByName("USA")).thenReturn(
            mutableListOf(
                Country().apply {id=1; name="USA"},
                Country().apply {id=2; name="Duplicate"}
            )
        )
        assertEquals(CountryDto(1,"USA"),service.getCountryByName("USA"))
    }

    @Test
    fun `getBandsByName should return matching bands`() {
        `when`(bandRepository.findByNameContainingIgnoreCase("meta")).thenReturn(listOf(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))
        assertEquals(listOf("Metallica"),service.getBandsByName("meta").map {it.name})
    }

    @Test
    fun `getBandsByNameExact should return exact matches`() {
        `when`(bandRepository.findByNameIgnoreCase("Metallica")).thenReturn(listOf(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))
        assertEquals(listOf("Metallica"),service.getBandsByNameExact("Metallica").map {it.name})
    }

    @Test
    fun `getBandsByCountry should return bands from named country`() {
        `when`(countryRepository.getCountryByName("USA")).thenReturn(mutableListOf(Country().apply {id=1; name="USA"}))
        `when`(bandRepository.findByCountry(1)).thenReturn(listOf(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))
        assertEquals(listOf("Metallica"),service.getBandsByCountry("USA").map {it.name})
    }

    @Test
    fun `getBandsByCountryId should return bands from country id`() {
        `when`(bandRepository.findByCountry(1)).thenReturn(listOf(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))
        assertEquals(listOf("Metallica"),service.getBandsByCountryId(1).map {it.name})
    }

    @Test
    fun `getBandsByCountry should throw NullPointerException when country name is unknown`() {
        `when`(countryRepository.getCountryByName("missing")).thenReturn(mutableListOf())
        assertThrows<NullPointerException> {service.getBandsByCountry("missing")}
    }

    @Test
    fun `getBandsByFoundedBetween should default missing years`() {
        `when`(bandRepository.findByFormedYearBetween(1900,LocalDate.now().year)).thenReturn(listOf(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))

        val result=service.getBandsByFoundedBetween(null,null)

        assertEquals(1,result.size)
        assertEquals("Metallica",result.first().name)
    }

    @Test
    fun `getBandsByFoundedBetween should use default start year`() {
        `when`(bandRepository.findByFormedYearBetween(1900,2020)).thenReturn(listOf(band))
        assertEquals(1,service.getBandsByFoundedBetween(null,2020).size)
    }

    @Test
    fun `getBandsByFoundedBetween should use current year as default end year`() {
        `when`(bandRepository.findByFormedYearBetween(1980,LocalDate.now().year)).thenReturn(listOf(band))
        assertEquals(1,service.getBandsByFoundedBetween(1980,null).size)
    }

    @Test
    fun `getBandsByStatus should return bands with requested status`() {
        `when`(bandRepository.findByStatus(Status.ACTIVE)).thenReturn(listOf(band))
        `when`(bandRepository.findCountryByBandId(1)).thenReturn(CountryDto(1,"USA"))

        assertEquals(listOf("Metallica"),service.getBandsByStatus(Status.ACTIVE).map {it.name})
    }

    @Test
    fun `calculateBandsGenre should update average genre`() {
        val genreId=10
        val genre=Genre().apply {id=genreId; properties="1234567"}
        val genreDto=GenreDto(id=genreId,name="Rock",value=8)

        `when`(genreService.getBandAlbumGenresList(1)).thenReturn(listOf(genreDto))
        `when`(genreRepository.findGenreById(genreId)).thenReturn(genre)
        `when`(bandRepository.findBandById(1)).thenReturn(band)

        service.calculateBandsGenre(1)

        verify(bandsGenreRepository).deleteByBandsId(1)
        verify(bandsGenreRepository).save(any(BandsGenres::class.java))
        verify(bandRepository).save(band)
        assertNotNull(band.averageGenre)
    }

    @Test
    fun `calculateBandsGenre should ignore incomplete genre rows`() {
        val valid=GenreDto(id=10,name="Rock",value=8)
        val missingName=GenreDto(id=11,name=null,value=8)
        val missingValue=GenreDto(id=12,name="Metal",value=null)
        `when`(genreService.getBandAlbumGenresList(1)).thenReturn(listOf(valid,missingName,missingValue))
        `when`(genreRepository.findGenreById(10)).thenReturn(Genre().apply {id=10; properties="1234567"})
        `when`(bandRepository.findBandById(1)).thenReturn(band)

        service.calculateBandsGenre(1)
        verify(genreRepository).findGenreById(10)
        verify(genreRepository,never()).findGenreById(11)
        verify(genreRepository,never()).findGenreById(12)
    }

    @Test
    fun `getBandsGenre should return stored average genre`() {
        val band=Band().apply {id=2; name="Slayer"; formedYear=1981; country=2; averageGenre="1111111"}
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        assertEquals("1111111",service.getBandsGenre(1))
    }

    @Test
    fun `getSimilarBands should return other bands`() {
        val other=Band().apply {id=2; name="Slayer"; formedYear=1981; country=2; averageGenre="1111111"}
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        `when`(bandRepository.findBandsWithAvgGenre()).thenReturn(listOf(band,other))
        `when`(countryRepository.getCountryNameById(2)).thenReturn("USA")
        val similar=service.getSimilarBands(1,5)
        assertEquals(1,similar.size)
        assertEquals(2,similar.first().id)
        assertEquals("Slayer",similar.first().name)
    }

    @Test
    fun `getSimilarBands should exclude requested band`() {
        val other=Band().apply {id=2; name="Slayer"; formedYear=1981; country=2; averageGenre="1111111"}
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        `when`(bandRepository.findBandsWithAvgGenre()).thenReturn(listOf(band,other))
        `when`(countryRepository.getCountryNameById(2)).thenReturn("USA")
        assertTrue(service.getSimilarBands(1,0).isEmpty())
        assertTrue(service.getSimilarBands(1,5).none {it.id==1})
    }

    @Test
    fun `doesBandExist should return true for existing band`() {
        `when`(bandRepository.existsById(1)).thenReturn(true)

        assertTrue(service.doesBandExist(1))
        verify(bandRepository).existsById(1)
    }

    @Test
    fun `doesBandExist should return false when band does not exist`() {
        `when`(bandRepository.existsById(99)).thenReturn(false)
        assertFalse(service.doesBandExist(99))
    }

    @Test
    fun `addBand should throw ContributionLimitExceededException when user reaches contribution limit`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(ContributionLimitExceededException("limit"))

        assertThrows<ContributionLimitExceededException> {
            service.addBand(BandAddDto(name="Metallica",status=Status.ACTIVE,imageUrl=null),"user")
        }
        verifyNoInteractions(contributionRepository,bandRepository)
    }

    @Test
    fun `addBand should create trusted band contribution`() {
        val dto=BandAddDto(
            name="Metallica",
            formedYear=1981,
            status=Status.ACTIVE,
            country=1,
            description="Thrash legends",
            imageUrl="https://example.com/metallica.jpg"
        )
        requestingUser.rank=Rank().apply {id=11}
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandRepository.findTopIdByName("Metallica")).thenReturn(5)
        `when`(contributionRepository.findTopChangeId()).thenReturn(7)

        service.addBand(dto,"user")

        verify(bandRepository).save(any(Band::class.java))
        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(captor.capture())
        assertTrue(captor.allValues.all {it.confirmed==true&&it.confirmedBy==7})
    }

    @Test
    fun `addBand should create untrusted contributions`() {
        val dto=BandAddDto(name="Metallica",formedYear=null,status=Status.ACTIVE,country=null,description=null,imageUrl=null)
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandRepository.findTopIdByName("Metallica")).thenReturn(5)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)

        service.addBand(dto,"user")
        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(captor.capture())
        assertTrue(captor.allValues.all {it.confirmed==false&&it.confirmedBy==null})
    }

    @Test
    fun `editBandRequest should update changed fields`() {
        val band=Band().apply {
            id=1
            name="Metallica"
            formedYear=1981
            status=Status.ACTIVE
            country=1
            description="Old"
            imageUrl="https://example.com/old.jpg"
        }
        requestingUser.rank=Rank().apply {id=12}
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
        val dto=BandAddDto(
            id=1,
            name="Metallica",
            formedYear=1981,
            status=Status.ACTIVE,
            country=2,
            description="New",
            imageUrl="https://example.com/new.jpg"
        )

        service.editBandRequest(dto,"user")
        verify(contributionRepository,atLeastOnce()).save(any(Contribution::class.java))
        val captor=org.mockito.ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(captor.capture())
        assertTrue(captor.allValues.all {it.confirmed==true&&it.confirmedBy==7})
    }

    @Test
    fun `editBandRequest should throw IllegalStateException for unchanged band`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        assertThrows<IllegalStateException> {
            service.editBandRequest(BandAddDto(id=1,name="Metallica",imageUrl=null),"user")
        }
    }

    @Test
    fun `editBandRequest should throw ContributionLimitExceededException when user reaches contribution limit`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {
            service.editBandRequest(BandAddDto(id=1,name="New",imageUrl=null),"user")
        }
    }

    @Test
    fun `editBandRequest should update every supported field`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        `when`(contributionRepository.findTopChangeId()).thenReturn(4)
        val dto=BandAddDto(1,"New",1982,Status.DISBANDED,1990,2,"New desc","new.jpg")
        service.editBandRequest(dto,"user")
        assertEquals("New",band.name)
        assertEquals(1982,band.formedYear)
        assertEquals(1990,band.disbandedYear)
        assertEquals(Status.DISBANDED,band.status)
        assertEquals(2,band.country)
        assertEquals("New desc",band.description)
        assertEquals("new.jpg",band.imageUrl)
        verify(contributionRepository,atLeast(7)).save(any(Contribution::class.java))
    }

    @Test
    fun `deleteBandRequest should record contributions for untrusted user`() {
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandRepository.findBandById(1)).thenReturn(band)

        service.deleteBandRequest(1,"user",log=true)
        verify(contributionRepository,atLeastOnce()).save(any(Contribution::class.java))
        verify(bandRepository,never()).deleteById(1)
    }

    @Test
    fun `deleteBandRequest should remove band for trusted user`() {
        `when`(contributionRepository.findTopChangeId()).thenReturn(2)
        requestingUser.rank=Rank().apply {id=11}
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        service.deleteBandRequest(1,"user",log=true)
        verify(bandRepository).deleteById(1)
    }

    @Test
    fun `deleteBandRequest should throw ContributionLimitExceededException when user reaches contribution limit`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(ContributionLimitExceededException("limit"))
        assertThrows<ContributionLimitExceededException> {service.deleteBandRequest(1,"user")}
    }

    @Test
    fun `deleteBandRequest should skip contributions when logging is disabled`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        service.deleteBandRequest(1,"user",log=false)
        verify(contributionRepository,never()).save(any(Contribution::class.java))
        verify(bandRepository,never()).deleteById(1)
    }

    @Test
    fun `doesSameMemberExist should return true if matching member exists`() {
        val member=ArtistBandAddDto(artistId=10L,bandId=1,role="Vocals",joinedYear=1981)
        val bandMember=BandsMembers().apply {
            id=22
            artist=Artist().apply {id=10L}
            band=Band().apply {id=1}
            role="Vocals"
            joinedYear=1981
        }

        `when`(bandsMemberRepository.findArtistInBand(10L,1)).thenReturn(mutableListOf(bandMember))
        assertTrue(service.doesSameMemberExist(member))
    }

    @Test
    fun `doesSameMemberExist should return false if similar member exists but band id differs`() {
        val members=listOf(
            ArtistBandAddDto(artistId=11234L,bandId=1,role="Vocals",joinedYear=1981),
            ArtistBandAddDto(artistId=10L,bandId=2,role="Vocals",joinedYear=1981),
            ArtistBandAddDto(id=5,artistId=10L,bandId=1,role="Vocals",joinedYear=1981),
            ArtistBandAddDto(artistId=10L,bandId=1,role="Kazoo",joinedYear=1981),
            ArtistBandAddDto(id=22,artistId=10L,bandId=1,role="Guitar",joinedYear=2000,leftYear=2001)
        )

        val existingBandMember=BandsMembers().apply {
            id=22
            artist=Artist().apply {id=10L}
            band=Band().apply {id=1}
            role="Guitar"
            joinedYear=1982
        }

        `when`(bandsMemberRepository.findArtistInBand(10L,1)).thenReturn(mutableListOf(existingBandMember))
        members.forEach { member->
            assertFalse(service.doesSameMemberExist(member))
        }
    }

    @Test
    fun `doesBandMemberExist should return true for existing member`() {
        `when`(bandsMemberRepository.existsById(22L)).thenReturn(true)
        assertTrue(service.doesBandMemberExist(22))
    }

    @Test
    fun `getBandMemberById should return requested member`() {
        val member=BandsMembers()
        `when`(bandsMemberRepository.findById(22.toInt())).thenReturn(Optional.of(member))
        assertSame(member,service.getBandMemberById(22))
    }

    @Test
    fun `toggleFavoriteBand should remove existing favorite`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        val favorite=UsersBands().apply {id=4}
        `when`(userBandRepository.findByBandAndUser(band,requestingUser)).thenReturn(favorite)
        service.toggleFavoriteBand(1,"user")
        verify(userBandRepository).deleteById(4)
    }

    @Test
    fun `toggleFavoriteBand should create missing favorite`() {
        `when`(userAccountService.getUserByLogin("user")).thenReturn(requestingUser)
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        doReturn(UsersBands().apply {id=null}).`when`(userBandRepository).findByBandAndUser(band,requestingUser)
        doReturn(UsersBands()).`when`(userBandRepository).saveAndFlush(any(UsersBands::class.java))
        service.toggleFavoriteBand(1,"user")
        verify(userBandRepository).saveAndFlush(any(UsersBands::class.java))
    }

    @Test
    fun `toggleFavoriteBand should throw IllegalStateException for unknown user`() {
        `when`(userAccountService.getUserByLogin("missing")).thenReturn(null)
        assertThrows<IllegalStateException> {service.toggleFavoriteBand(1,"missing")}
    }
}

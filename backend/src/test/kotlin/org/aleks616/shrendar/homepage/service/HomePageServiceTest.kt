package org.aleks616.shrendar.homepage.service

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.album.model.BandDto
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAnniversaryDto
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.ArtistBandsDto
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandGenreDto
import org.aleks616.shrendar.band.model.BandsMembers
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersArtists
import org.aleks616.shrendar.user.model.UsersBands
import org.aleks616.shrendar.user.model.UsersGenres
import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.service.UserAccountService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.LocalDateTime

class HomePageServiceTest {
    private val albumService=mock(AlbumService::class.java)
    private val artistService=mock(ArtistService::class.java)
    private val bandsMemberRepository=mock(BandsMemberRepository::class.java)
    private val userBandRepository=mock(UserBandRepository::class.java)
    private val userAccountService=mock(UserAccountService::class.java)
    private val countryRepository=mock(CountryRepository::class.java)
    private val userArtistRepository=mock(UserArtistRepository::class.java)
    private val albumRepository=mock(AlbumRepository::class.java)
    private val userGenreRepository=mock(UserGenreRepository::class.java)
    private val genreRepository=mock(GenreRepository::class.java)
    private val bandService=mock(BandService::class.java)
    private val artistRepository=mock(ArtistRepository::class.java)
    private val contributionRepository=mock(ContributionRepository::class.java)
    private lateinit var service:HomePageService
    private lateinit var user:User

    @BeforeEach
    fun setup() {
        service=HomePageService(
            albumService,artistService,bandsMemberRepository,userBandRepository,userAccountService,
            countryRepository,userArtistRepository,albumRepository,userGenreRepository,genreRepository,
            bandService,artistRepository,contributionRepository
        )
        user=User().apply { id=1; login="tester" }
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(user)
    }

    @Test
    fun `favorite birthday and death source rejects unknown users and empty favorites`() {
        `when`(userAccountService.getUserByLogin("missing")).thenReturn(null)
        assertThrows<IllegalArgumentException> {service.getUpcomingFavoriteBirthdaysAndDeaths("missing")}

        `when`(userArtistRepository.findByUser(user)).thenReturn(mutableListOf())
        assertEquals(emptyList<Any>(),service.getUpcomingFavoriteBirthdaysAndDeaths("tester"))
    }

    @Test
    fun `favorite birthday and death source maps living and deceased artists while ignoring artists without birthdays`() {
        val living=artist(1,LocalDate.now().plusDays(2))
        val deceased=artist(2,LocalDate.now().plusDays(3),LocalDate.now().plusDays(4),2)
        val noBirthday=artist(3,null)
        `when`(userArtistRepository.findByUser(user)).thenReturn(mutableListOf(favorite(living),favorite(deceased),favorite(noBirthday)))
        `when`(countryRepository.getCountryNameById(2)).thenReturn("Poland")

        val result=service.getUpcomingFavoriteBirthdaysAndDeaths("tester")

        assertEquals(listOf(1L,2L),result.map {it.id})
        assertNull(result[0].country)
        assertEquals("Poland",result[1].country)
        assertNotNull(result[1].daysTillDeathAnniversary)
        assertEquals(0,result[1].ageAtDeath)
    }

    @Test
    fun `favorite birthday and death source returns empty after filtering favorites without birthdays`() {
        `when`(userArtistRepository.findByUser(user)).thenReturn(
            mutableListOf(favorite(artist(1,null)),favorite(artist(2,null,LocalDate.now())))
        )

        assertEquals(emptyList<Any>(),service.getUpcomingFavoriteBirthdaysAndDeaths("tester"))
    }

    @Test
    fun `favorite upcoming artists exclude todays birthday and death and include near and later anniversaries`() {
        val birthdayArtists=(1..4).map { artist(it.toLong(),LocalDate.now().plusDays(it.toLong())) }+
            listOf(
                artist(5,LocalDate.now().plusDays(10)),
                artist(6,LocalDate.now()),
                artist(7,LocalDate.now().plusDays(60))
            )
        val deathArtists=(10..13).map { artist(it.toLong(),LocalDate.now().minusYears(30),LocalDate.now().plusDays((it-9).toLong())) }+
            listOf(
                artist(14,LocalDate.now().minusYears(30),LocalDate.now().plusDays(10)),
                artist(15,LocalDate.now().minusYears(30),LocalDate.now()),
                artist(16,LocalDate.now().minusYears(30),LocalDate.now().plusDays(60))
            )
        `when`(userArtistRepository.findByUser(user)).thenReturn((birthdayArtists+deathArtists).map(::favorite).toMutableList())

        val birthdays=service.getUpcomingFavoriteArtistsBirthdays("tester")
        val deaths=service.getUpcomingFavoriteArtistsDeathAnniversaries("tester")

        assertFalse(birthdays.any {it.id==6L})
        assertEquals(setOf(1L,2L,3L,4L,5L),birthdays.mapNotNull {it.id}.toSet())
        assertFalse(deaths.any {it.id==15L})
        assertEquals(setOf(10L,11L,12L,13L,14L),deaths.mapNotNull {it.id}.toSet())
    }

    @Test
    fun `favorite death anniversaries return empty when no favorite has died`() {
        `when`(userArtistRepository.findByUser(user)).thenReturn(mutableListOf(favorite(artist(1,LocalDate.now()))))
        assertEquals(emptyList<Any>(),service.getUpcomingFavoriteArtistsDeathAnniversaries("tester"))
    }

    @Test
    fun `random upcoming artists map qualifying birthday and death anniversaries`() {
        val birthday=artist(1,LocalDate.now().plusDays(2),country=3)
        val deceased=artist(2,LocalDate.now().minusYears(30),LocalDate.now().plusDays(3),3)
        `when`(artistRepository.findUpcomingBirthdays()).thenReturn(listOf(birthday,artist(4,LocalDate.now().plusDays(10),country=3)))
        `when`(artistRepository.findUpcomingDeathAnniversaries()).thenReturn(
            listOf(deceased,artist(5,LocalDate.now().minusYears(20),LocalDate.now().plusDays(10),3),artist(3,LocalDate.now(),null))
        )
        `when`(countryRepository.getCountryNameById(3)).thenReturn("USA")

        val birthdays=service.getUpcomingRandomArtistsBirthdays()
        val deaths=service.getUpcomingRandomArtistsDeathAnniversaries()

        assertEquals(setOf(1L,4L),birthdays.mapNotNull {it.id}.toSet())
        assertEquals(setOf(2L,5L),deaths.mapNotNull {it.id}.toSet())
        assertEquals(deceased.birthDate,deaths.first {it.id==2L}.anniversaryDate)
        assertEquals("USA",birthdays.first {it.id==1L}.country)
    }

    @Test
    fun `favorite album anniversaries reject unknown users and return empty without favorite bands`() {
        `when`(userAccountService.getUserByLogin("missing")).thenReturn(null)
        assertThrows<IllegalArgumentException> {service.getUpcomingFavoriteAlbumAnniversaries("missing")}
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf())
        assertEquals(emptyList<Any>(),service.getUpcomingFavoriteAlbumAnniversaries("tester"))
    }

    @Test
    fun `album anniversary endpoints select upcoming albums and preserve album data`() {
        val band=band(7)
        val near=album(1,band,LocalDate.now().plusDays(2))
        val later=album(2,band,LocalDate.now().plusDays(10))
        val today=album(3,band,LocalDate.now())
        val outsideRange=album(4,band,LocalDate.now().plusDays(30))
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(band)))
        `when`(albumRepository.findByBandId(7)).thenReturn(listOf(near,later,today,outsideRange))
        `when`(albumRepository.findAlbumsByUpcomingAnniversaries()).thenReturn(mutableListOf(near,later))

        val favorite=service.getUpcomingFavoriteAlbumAnniversaries("tester")
        val random=service.getUpcomingRandomAlbumAnniversaries()

        assertEquals(setOf(1L,2L),favorite.mapNotNull {it.id}.toSet())
        assertEquals(setOf(1L,2L),random.mapNotNull {it.id}.toSet())
        assertEquals("Band 7",favorite.first {it.id==1L}.bandName)
    }

    @Test
    fun `anonymous today anniversaries map current date data and leave authenticated sections absent`() {
        val today=LocalDate.now()
        val album=album(1,band(1),today)
        val birthday=ArtistAnniversaryDto(id=2,name="Birthday")
        val death=ArtistAnniversaryDto(id=3,name="Death")
        `when`(albumRepository.findByReleaseDateMonthAndDay(today.monthValue,today.dayOfMonth)).thenReturn(listOf(album))
        `when`(artistService.getByBirthday(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf(birthday))
        `when`(artistService.getByDeathDate(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf(death))

        val result=service.getTodayAnniversariesNoAuth()

        assertEquals(1L,result.favoriteAlbums!!.single().id)
        assertEquals(listOf(birthday),result.favoriteArtistsBirthdays)
        assertEquals(listOf(death),result.favoriteArtistsDeathAnniversaries)
        assertNull(result.recommendedArtistBirthdays)
    }

    @Test
    fun `other band members reject unknown users and flatten every favorite band`() {
        `when`(userAccountService.getUserByLogin("missing")).thenReturn(null)
        assertThrows<IllegalArgumentException> {service.getOtherBandMembers("missing")}
        val first=band(1)
        val second=band(2)
        val firstMember=member(artist(1,LocalDate.now()),first)
        val secondMember=member(artist(2,LocalDate.now()),second)
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(first),favoriteBand(second)))
        `when`(bandsMemberRepository.findByBandId(1)).thenReturn(mutableListOf(firstMember))
        `when`(bandsMemberRepository.findByBandId(2)).thenReturn(mutableListOf(secondMember))

        assertEquals(listOf(firstMember,secondMember),service.getOtherBandMembers("tester"))
    }

    @Test
    fun `other band member anniversaries include todays artists and birthday entries are distinct`() {
        val band=band(1)
        val birthday=artist(1,LocalDate.now(),country=1)
        val death=artist(2,LocalDate.now().minusYears(20).plusDays(1),LocalDate.now(),1)
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(band)))
        val differentMonth=artist(3,LocalDate.now().plusMonths(1),LocalDate.now().plusMonths(1),1)
        val differentDay=artist(4,LocalDate.of(LocalDate.now().year,LocalDate.now().month,if(LocalDate.now().dayOfMonth==1) 2 else 1),
            LocalDate.of(LocalDate.now().year,LocalDate.now().month,if(LocalDate.now().dayOfMonth==1) 2 else 1),1)
        val noDates=artist(5,null)
        `when`(bandsMemberRepository.findByBandId(1)).thenReturn(
            mutableListOf(member(birthday,band),member(birthday,band),member(death,band),member(differentMonth,band),member(differentDay,band),member(noDates,band))
        )
        `when`(countryRepository.getCountryNameById(1)).thenReturn("UK")

        assertEquals(listOf(1L),service.getOtherBandMembersBirthdaysToday("tester").mapNotNull {it.id})
        assertEquals(listOf(2L),service.getOtherBandMembersDeathAnniversariesToday("tester").mapNotNull {it.id})
    }

    @Test
    fun `recommended albums return genre matches and query favorite band genres`() {
        val band=band(1,"rock")
        val genre=Genre().apply { id=2; properties="rock" }
        val matching=album(1,band,LocalDate.now(),genre)
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(band)))
        `when`(userGenreRepository.findByUser(user)).thenReturn(mutableListOf(UsersGenres().apply {this.user=this@HomePageServiceTest.user; this.genre=genre}))
        `when`(albumRepository.findByBandId(1)).thenReturn(listOf(matching))
        `when`(albumRepository.findByGenre(genre)).thenReturn(mutableListOf(matching))
        `when`(genreRepository.findAll()).thenReturn(listOf(genre))

        assertEquals(listOf(matching),service.getRecommendedAlbums(user))
        verify(albumRepository).findByBandId(1)
    }

    @Test
    fun `recommended albums ignore incomplete album records and evaluate matching and nonmatching band genres`() {
        val rockBand=band(1,"rock")
        val metalBand=band(2,"metal")
        val rock=Genre().apply {id=1; properties="rock"}
        val noProperties=Genre().apply {id=2; properties=null}
        val valid=album(1,rockBand,LocalDate.now(),rock)
        val noGenre=album(3,rockBand,LocalDate.now())
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(rockBand),favoriteBand(metalBand)))
        `when`(userGenreRepository.findByUser(user)).thenReturn(mutableListOf(
            UsersGenres().apply {genre=rock},UsersGenres().apply {genre=noProperties}
        ))
        `when`(albumRepository.findByBandId(1)).thenReturn(emptyList())
        `when`(albumRepository.findByBandId(2)).thenReturn(emptyList())
        `when`(albumRepository.findByGenre(rock)).thenReturn(mutableListOf(valid,noGenre))
        `when`(albumRepository.findByGenre(noProperties)).thenReturn(mutableListOf())
        `when`(genreRepository.findAll()).thenReturn(listOf(rock))

        assertEquals(listOf(valid),service.getRecommendedAlbums(user))
    }

    @Test
    fun `recommended artists deduplicate similar bands before loading their members`() {
        val favorite=band(1)
        val recommended=BandGenreDto(id=2)
        val artist=artist(5,LocalDate.now())
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(favorite)))
        `when`(bandService.getSimilarBands(1,10)).thenReturn(listOf(recommended,recommended))
        `when`(bandsMemberRepository.findByBandId(2)).thenReturn(mutableListOf(member(artist,band(2))))

        assertEquals(listOf(artist),service.getRecommendedArtists(user))
        verify(bandsMemberRepository,times(1)).findByBandId(2)
    }

    @Test
    fun `authenticated today anniversaries use fallback artists and related albums when recommendations are sparse`() {
        val today=LocalDate.now()
        val favoriteBand=band(1)
        val similar=BandGenreDto(id=2)
        val fallbackBirthday=ArtistAnniversaryDto(id=10,name="Fallback birthday")
        val fallbackDeath=ArtistAnniversaryDto(id=11,name="Fallback death")
        val related=album(2,band(2),today)
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(favoriteBand)))
        `when`(userArtistRepository.findByUser(user)).thenReturn(mutableListOf())
        `when`(albumRepository.findByBandId(1)).thenReturn(emptyList())
        `when`(userGenreRepository.findByUser(user)).thenReturn(mutableListOf())
        `when`(genreRepository.findAll()).thenReturn(emptyList())
        `when`(bandService.getSimilarBands(1,10)).thenReturn(emptyList())
        `when`(bandService.getSimilarBands(1,20)).thenReturn(listOf(similar))
        `when`(albumService.getAlbumAnniversariesByDate(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf(related.anniversary()))
        `when`(artistService.getByBirthday(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf(fallbackBirthday))
        `when`(artistService.getByDeathDate(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf(fallbackDeath))

        val result=service.getTodayAnniversaries("tester")

        assertEquals(listOf(fallbackBirthday),result.recommendedArtistBirthdays)
        assertEquals(listOf(fallbackDeath),result.recommendedArtistDeathAnniversaries)
        assertEquals(listOf(2L),result.recommendedAlbumsAnniversaries!!.mapNotNull {it.id})
    }

    @Test
    fun `authenticated today anniversaries retain four or more recommended albums without querying related bands`() {
        val today=LocalDate.now()
        val favoriteBand=band(1,"rock")
        val genre=Genre().apply {id=1; properties="rock"}
        val recommendations=(1L..4L).map {album(it,favoriteBand,today,genre)}
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(favoriteBand)))
        `when`(userArtistRepository.findByUser(user)).thenReturn(mutableListOf())
        `when`(userGenreRepository.findByUser(user)).thenReturn(mutableListOf(UsersGenres().apply {this.genre=genre}))
        `when`(genreRepository.findAll()).thenReturn(listOf(genre))
        `when`(albumRepository.findByBandId(1)).thenReturn(emptyList())
        `when`(albumRepository.findByGenre(genre)).thenReturn(recommendations.toMutableList())
        `when`(bandsMemberRepository.findByBandId(1)).thenReturn(mutableListOf())
        `when`(bandService.getSimilarBands(1,10)).thenReturn(emptyList())
        `when`(albumService.getAlbumAnniversariesByDate(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf())
        `when`(artistService.getByBirthday(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf())
        `when`(artistService.getByDeathDate(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf())

        val result=service.getTodayAnniversaries("tester")

        assertEquals(setOf(1L,2L,3L,4L),result.recommendedAlbumsAnniversaries!!.mapNotNull {it.id}.toSet())
        verify(bandService,never()).getSimilarBands(1,20)
    }

    @Test
    fun `authenticated today anniversaries reject unknown users`() {
        `when`(userAccountService.getUserByLogin("missing")).thenReturn(null)
        assertThrows<IllegalArgumentException> {service.getTodayAnniversaries("missing")}
    }

    @Test
    fun `authenticated today anniversaries map recommended album and artist DTOs after date filtering`() {
        val today=LocalDate.now()
        val favoriteBand=band(1,"rock")
        val genre=Genre().apply {id=1; properties="rock"}
        val currentAlbum=album(1,favoriteBand,today,genre)
        val sameMonthDifferentDay=album(2,favoriteBand,LocalDate.of(today.year,today.month,if(today.dayOfMonth==1) 2 else 1),genre)
        val differentMonthSameDay=album(3,favoriteBand,today.plusMonths(1),genre)
        val differentMonthDifferentDay=album(4,favoriteBand,today.plusMonths(1).plusDays(1),genre)
        val recommendedBand=band(2)
        val currentBirthday=artist(10,today,country=1)
        val sameMonthBirthday=artist(11,sameMonthDifferentDay.releaseDate,country=1)
        val differentMonthBirthday=artist(12,differentMonthSameDay.releaseDate,country=1)
        val currentDeath=artist(13,today.minusYears(30).plusDays(1),today,1)
        val sameMonthDeath=artist(14,today.minusYears(31).plusDays(1),sameMonthDifferentDay.releaseDate,1)
        val differentMonthDeath=artist(15,today.minusYears(32).plusDays(1),differentMonthSameDay.releaseDate,1)
        val living=artist(16,today.minusYears(20).plusDays(1),null,1)
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(favoriteBand)))
        `when`(userArtistRepository.findByUser(user)).thenReturn(mutableListOf())
        `when`(albumRepository.findByBandId(1)).thenReturn(listOf(currentAlbum,sameMonthDifferentDay,differentMonthSameDay,differentMonthDifferentDay))
        `when`(userGenreRepository.findByUser(user)).thenReturn(mutableListOf(UsersGenres().apply {this.genre=genre}))
        `when`(albumRepository.findByGenre(genre)).thenReturn(mutableListOf(currentAlbum,sameMonthDifferentDay,differentMonthSameDay,differentMonthDifferentDay))
        `when`(genreRepository.findAll()).thenReturn(listOf(genre))
        `when`(bandService.getSimilarBands(1,10)).thenReturn(listOf(BandGenreDto(id=2)))
        `when`(bandService.getSimilarBands(1,20)).thenReturn(emptyList())
        `when`(bandsMemberRepository.findByBandId(1)).thenReturn(mutableListOf())
        `when`(bandsMemberRepository.findByBandId(2)).thenReturn(mutableListOf(
            member(currentBirthday,recommendedBand),member(sameMonthBirthday,recommendedBand),member(differentMonthBirthday,recommendedBand),
            member(currentDeath,recommendedBand),member(sameMonthDeath,recommendedBand),member(differentMonthDeath,recommendedBand),member(living,recommendedBand)
        ))
        `when`(artistService.getByBirthday(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf())
        `when`(artistService.getByDeathDate(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf())
        `when`(albumService.getAlbumAnniversariesByDate(today.monthValue,today.dayOfMonth)).thenReturn(mutableListOf())
        `when`(countryRepository.getCountryNameById(1)).thenReturn("UK")

        val result=service.getTodayAnniversaries("tester")

        assertEquals(listOf(1L),result.recommendedAlbumsAnniversaries!!.mapNotNull {it.id})
        assertEquals(listOf(10L),result.recommendedArtistBirthdays!!.mapNotNull {it.id})
        assertEquals(listOf(13L),result.recommendedArtistDeathAnniversaries!!.mapNotNull {it.id})
        assertEquals("UK",result.recommendedArtistBirthdays!!.single().country)
    }

    @Test
    fun `common bands reject unknown users and remove favorites and duplicates`() {
        `when`(userAccountService.getUserByLogin("missing")).thenReturn(null)
        assertThrows<IllegalArgumentException> {service.getCommonBands("missing")}
        val favorite=band(1)
        val artist=artist(2,LocalDate.now())
        val own=ArtistBandsDto(bandId=1)
        val shared=ArtistBandsDto(bandId=3)
        `when`(userBandRepository.findByUser(user)).thenReturn(mutableListOf(favoriteBand(favorite)))
        `when`(userArtistRepository.findByUser(user)).thenReturn(mutableListOf(favorite(artist)))
        `when`(bandsMemberRepository.findBandsByArtistId(2)).thenReturn(listOf(own,shared,shared))

        assertEquals(listOf(shared),service.getCommonBands("tester"))
    }

    @Test
    fun `recent additions groups contribution rows by change id`() {
        val contributor=User().apply {id=9}
        val first=contribution(1,5,contributor,"album","title","Old","New")
        val second=contribution(2,5,contributor,"album","genre","Rock","Metal")
        val third=contribution(3,6,contributor,"band","name","Old band","New band")
        `when`(contributionRepository.getRecentAdditions(5)).thenReturn(listOf(first,second,third))

        val result=service.getRecentlyAdded()

        assertEquals(2,result.size)
        val grouped=result.filterIsInstance<org.aleks616.shrendar.homepage.model.RecentlyAddedDto>()
        assertEquals(2,grouped.first {it.changeId==5L}.data!!.size)
        assertEquals("New band",grouped.first {it.changeId==6L}.data!!.single().newValue)
    }

    private fun artist(id:Long,birthDate:LocalDate?,deathDate:LocalDate?=null,country:Int?=null)=Artist().apply {
        this.id=id; name="Artist $id"; this.birthDate=birthDate; this.deathDate=deathDate; this.country=country
    }
    private fun band(id:Int,genre:String?=null)=Band().apply {this.id=id; name="Band $id"; averageGenre=genre}
    private fun album(id:Long,band:Band,date:LocalDate,genre:Genre?=null)=Album().apply {
        this.id=id; this.band=band; title="Album $id"; releaseDate=date; this.genre=genre
    }
    private fun Album.anniversary()=AlbumByDateDto(
        id=id,band=BandDto(band!!.id,band!!.name),title=title,releaseDate=releaseDate
    )
    private fun favorite(artist:Artist)=UsersArtists().apply {this.artist=artist; user=this@HomePageServiceTest.user}
    private fun favoriteBand(band:Band)=UsersBands().apply {this.band=band; user=this@HomePageServiceTest.user}
    private fun member(artist:Artist,band:Band)=BandsMembers().apply {this.artist=artist; this.band=band}
    private fun contribution(id:Long,changeId:Long,user:User,table:String,column:String,old:String,new:String)=Contribution().apply {
        this.id=id; this.changeId=changeId; this.user=user; changedTable=table; changedColumn=column
        oldValue=old; newValue=new; changedAt=LocalDateTime.of(2026,1,1,1,1); changedRecordId=1
    }
}

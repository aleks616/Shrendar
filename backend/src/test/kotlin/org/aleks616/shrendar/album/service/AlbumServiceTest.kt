package org.aleks616.shrendar.album.service

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumAddDto
import org.aleks616.shrendar.album.model.AlbumType
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.exception.InvalidAlbumImportanceException
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.RankService
import org.aleks616.shrendar.user.service.UserAccountService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.LocalDate

class AlbumServiceTest {
    private lateinit var albumRepository:AlbumRepository
    private lateinit var bandService:BandService
    private lateinit var contributionRepository:ContributionRepository
    private lateinit var genreRepository:GenreRepository
    private lateinit var userAccountService:UserAccountService
    private lateinit var rankService:RankService
    private lateinit var albumService:AlbumService
    private lateinit var album:Album

    private lateinit var album1:Album
    private lateinit var band:Band
    private lateinit var genre:Genre
    private lateinit var requestingUser:User

    @BeforeEach
    fun setup() {
        albumRepository=mock(AlbumRepository::class.java)
        bandService=mock(BandService::class.java)
        contributionRepository=mock(ContributionRepository::class.java)
        genreRepository=mock(GenreRepository::class.java)
        userAccountService=mock(UserAccountService::class.java)
        rankService=mock(RankService::class.java)
        albumService=AlbumService(albumRepository,bandService,contributionRepository,genreRepository,userAccountService,rankService)

        band=Band().apply {id=2; name="Metallica"; formedYear=1981}
        genre=Genre().apply {id=3; name="Metal"}
        album=Album().apply {
            id=1
            title="Ride the Lightning"
            releaseDate=LocalDate.of(1984,7,27)
            type=AlbumType.STUDIO
            importance=5
            artworkUrl="https://example.com/artwork.jpg"
            description="Description"
        }
        album.band=band
        album.genre=genre
        requestingUser=User().apply {
            id=7
            login="tester"
            rank=Rank().apply {id=1}
        }

        album1=Album().apply {
            id=1
            title="Ride the Lightning"
            releaseDate=LocalDate.of(1984,7,27)
            type=AlbumType.STUDIO
            importance=5
            artworkUrl="https://example.com/artwork.jpg"
            description="Description"
        }
        album1.genre=genre
    }

    @Test
    fun `doesBandExist should delegate to band service`() {
        `when`(bandService.doesBandExist(2)).thenReturn(true)

        assertTrue(albumService.doesBandExist(2))
        verify(bandService).doesBandExist(2)
    }

    @Test
    fun `doesAlbumExist should delegate to album repository`() {
        `when`(albumRepository.existsById(1L)).thenReturn(true)

        assertTrue(albumService.doesAlbumExist(1L))
        verify(albumRepository).existsById(1L)
    }

    @Test
    fun `getAll should map albums to album data`() {
        `when`(albumRepository.findAll()).thenReturn(listOf(album))

        val result=albumService.getAll()

        assertEquals(1,result.size)
        assertEquals(album.id,result.single().id)
        assertEquals(2,result.single().band?.id)
        assertEquals("Metallica",result.single().band?.name)
        assertEquals(album.title,result.single().title)
        assertEquals(album.releaseDate,result.single().releaseDate)
        assertEquals(album.type,result.single().type)
        assertEquals(album.importance,result.single().importance)
        assertSame(genre,result.single().genre)
        assertEquals(album.artworkUrl,result.single().artworkUrl)
    }

    @Test
    fun `getById should return repository album`() {
        `when`(albumRepository.findAlbumById(1L)).thenReturn(album)

        assertSame(album,albumService.getById(1L))
    }

    @Test
    fun `getByIdWiki should map album wiki data`() {
        `when`(albumRepository.findAlbumById(1L)).thenReturn(album)

        val result=albumService.getByIdWiki(1L)

        assertEquals(album.id,result.id)
        assertEquals(album.title,result.albumName)
        assertEquals(2,result.band?.id)
        assertEquals("Metallica",result.band?.name)
        assertEquals(album.releaseDate,result.releaseDate)
        assertEquals(album.type,result.type)
        assertSame(album.genre,result.genre)
        assertEquals(album.description,result.description)
        assertEquals(album.artworkUrl,result.artworkUrl)
        assertEquals(album.importance,result.importance)
        assertTrue(result.albumAge!!>0)
        assertTrue(result.daysTillAnniversary!! in 0..366)
    }

    @Test
    fun `getByIdWiki should not fail when band id is not found`(){
        `when`(albumRepository.findAlbumById(2L)).thenReturn(album1)

        val result=albumService.getByIdWiki(2L)

        assertEquals(album1.id,result.id)
        assertEquals(album1.title,result.albumName)
        assertEquals(null,result.band?.id)
        assertEquals(null,result.band?.name)
        assertEquals(album1.releaseDate,result.releaseDate)
        assertEquals(album1.type,result.type)
        assertSame(album1.genre,result.genre)
        assertEquals(album1.description,result.description)
        assertEquals(album1.artworkUrl,result.artworkUrl)
        assertEquals(album1.importance,result.importance)
        assertTrue(result.albumAge!!>0)
        assertTrue(result.daysTillAnniversary!! in 0..366)
    }

    @Test
    fun `getAlbumsByBandId should return repository albums`() {
        `when`(albumRepository.findByBandId(2)).thenReturn(listOf(album))

        assertEquals(listOf(album),albumService.getAlbumsByBandId(2))
    }

    @Test
    fun `getAlbumsByBandName should return repository albums`() {
        `when`(albumRepository.findByBandNameContainingIgnoreCase("metal")).thenReturn(mutableListOf(album))

        assertEquals(listOf(album),albumService.getAlbumsByBandName("metal"))
    }

    @Test
    fun `getAlbumsByYear should return repository albums`() {
        `when`(albumRepository.findByYear(1984)).thenReturn(listOf(album))

        assertEquals(listOf(album),albumService.getAlbumsByYear(1984))
    }

    @Test
    fun `getAlbumsByName should return repository albums`() {
        `when`(albumRepository.findByTitleContainingIgnoreCase("Ride")).thenReturn(listOf(album))

        assertEquals(listOf(album),albumService.getAlbumsByName("Ride"))
    }

    @Test
    fun `getAlbumsByNameExact should return repository albums`() {
        `when`(albumRepository.findByTitleIgnoreCase("Ride the Lightning")).thenReturn(listOf(album))

        assertEquals(listOf(album),albumService.getAlbumsByNameExact("Ride the Lightning"))
    }

    @Test
    fun `getAlbumAnniversariesByDate should return matching mapped albums`() {
        val other=Album().apply {id=2; title="Other"; releaseDate=LocalDate.of(1984,7,28)}
        `when`(albumRepository.findAll()).thenReturn(listOf(album,other))

        val result=albumService.getAlbumAnniversariesByDate(7,27)

        assertEquals(1,result.size)
        assertEquals(album.id,result.single().id)
        assertEquals(album.title,result.single().title)
        assertEquals(2,result.single().band?.id)
        assertEquals("Metallica",result.single().band?.name)
        assertEquals(LocalDate.now().year-1984,result.single().yearsSince)
    }

    @Test
    fun `doesAlbumWithNameExistForBand should return true and false`() {
        val other=album.copyForTest(title="Other")
        `when`(albumRepository.findByBandId(2)).thenReturn(listOf(album,other))

        assertTrue(albumService.doesAlbumWithNameExistForBand(AlbumAddDto(bandId=2,title=album.title)))
        assertFalse(albumService.doesAlbumWithNameExistForBand(AlbumAddDto(bandId=2,title="Missing")))
    }

    @Test
    fun `doesAlbumWithNameExistForAlbumId should return true and false`() {
        val other=album.copyForTest(title="Other")
        `when`(albumRepository.findById(1L)).thenReturn(album)
        `when`(albumRepository.findByBandId(2)).thenReturn(listOf(album,other))

        assertTrue(albumService.doesAlbumWithNameExistForAlbumId(AlbumAddDto(id=1,title=album.title)))
        assertFalse(albumService.doesAlbumWithNameExistForAlbumId(AlbumAddDto(id=1,title="Missing")))
    }

    @Test
    fun `isReleaseDateValid should reject too far future dates`() {
        assertFalse(albumService.isReleaseDateValid(AlbumAddDto(bandId=2,releaseDate=LocalDate.now().plusYears(2))))
        verifyNoInteractions(bandService)
    }

    @Test
    fun `isReleaseDateValid should reject dates before band formation`() {
        `when`(bandService.getBandById(2)).thenReturn(band)

        assertFalse(albumService.isReleaseDateValid(AlbumAddDto(bandId=2,releaseDate=LocalDate.of(1980,1,1))))
    }

    @Test
    fun `isReleaseDateValid should accept valid and missing release dates`() {
        `when`(bandService.getBandById(2)).thenReturn(band)

        assertTrue(albumService.isReleaseDateValid(AlbumAddDto(bandId=2,releaseDate=LocalDate.of(1981,1,1))))
        assertThrows<NullPointerException> {
            albumService.isReleaseDateValid(AlbumAddDto(bandId=2,releaseDate=null))
        }
    }

    @Test
    fun `addAlbumRequest should throw contribution limit exception`() {
        val limit=ContributionLimitExceededException("limit")
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(limit)

        assertThrows<ContributionLimitExceededException> {
            albumService.addAlbumRequest(
                AlbumAddDto(
                    bandId=2,
                    title="Album",
                    mainSubgenre=3
                ),"tester"
            )
        }
        verifyNoInteractions(albumRepository,contributionRepository,genreRepository)
    }

    @Test
    fun `addAlbumRequest should save untrusted album and changes`() {
        val dto=AlbumAddDto(
            bandId=2,title="Album",releaseDate=LocalDate.of(1984,1,1),type=AlbumType.COMPILATION,
            description="Description",mainSubgenre=3,importance=5,artworkUrl="url"
        )
        stubAddDependencies(dto)

        albumService.addAlbumRequest(dto,"tester")

        val saved=ArgumentCaptor.forClass(Album::class.java)
        verify(albumRepository).save(saved.capture())
        assertEquals(dto.title,saved.value.title)
        assertNull(saved.value.importance)
        verify(contributionRepository,times(8)).save(any(Contribution::class.java))
        verify(bandService).calculateBandsGenre(2)
    }

    @Test
    fun `addAlbumRequest should mark trusted user changes confirmed`() {
        requestingUser.rank=Rank().apply {id=10}
        val dto=AlbumAddDto(bandId=2,title="Album",type=AlbumType.STUDIO,mainSubgenre=3,importance=4)
        stubAddDependencies(dto)

        albumService.addAlbumRequest(dto,"tester")

        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository,atLeastOnce()).save(saved.capture())
        assertTrue(saved.allValues.all {it.confirmed==true&&it.confirmedBy==requestingUser.id})
    }

    @Test
    fun `editAlbumRequest should throw invalid importance exceptions`() {
        stubEditDependencies()
        album.type=AlbumType.OTHER

        assertThrows<InvalidAlbumImportanceException> {
            albumService.editAlbumRequest(AlbumAddDto(id=1,importance=4),"tester")
        }
        assertThrows<InvalidAlbumImportanceException> {
            albumService.editAlbumRequest(AlbumAddDto(id=1,importance=1),"tester")
        }
    }

    @Test
    fun `editAlbumRequest should throw when there are no changes`() {
        stubEditDependencies()

        assertThrows<IllegalStateException> {albumService.editAlbumRequest(AlbumAddDto(id=1),"tester")}
        verify(albumRepository,never()).save(any(Album::class.java))
    }

    @Test
    fun `editAlbumRequest should update changed values and recalculate genre`() {
        stubEditDependencies()
        val dto=AlbumAddDto(id=1,bandId=4,title="New",mainSubgenre=5,importance=4)
        `when`(bandService.getBandById(4)).thenReturn(Band().apply {id=4})
        `when`(genreRepository.findGenreById(5)).thenReturn(Genre().apply {id=5})

        albumService.editAlbumRequest(dto,"tester")

        assertEquals("New",album.title)
        assertEquals(4,album.band?.id)
        assertEquals(4,album.importance)
        verify(albumRepository).save(album)
        verify(bandService).calculateBandsGenre(4)
        verify(contributionRepository,times(4)).save(any(Contribution::class.java))
    }

    @Test
    fun `editAlbumRequest should mark changes confirmed for rank above 9`() {
        requestingUser.rank=Rank().apply {id=10}
        stubEditDependencies()
        val dto=AlbumAddDto(id=1,title="New")

        albumService.editAlbumRequest(dto,"tester")

        val saved=ArgumentCaptor.forClass(Contribution::class.java)
        verify(contributionRepository).save(saved.capture())
        assertTrue(saved.value.confirmed==true&&saved.value.confirmedBy==requestingUser.id)
    }

    @Test
    fun `deleteAlbumRequest should throw contribution limit exception`() {
        val limit=ContributionLimitExceededException("limit")
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(limit)

        assertThrows<ContributionLimitExceededException> {albumService.deleteAlbumRequest(1,"tester")}
        verifyNoInteractions(albumRepository,contributionRepository,bandService)
    }

    @Test
    fun `deleteAlbumRequest should not delete untrusted users`() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)

        albumService.deleteAlbumRequest(1,"tester",log=false)

        verify(albumRepository,never()).deleteById(1L)
        verifyNoInteractions(bandService)
    }

    @Test
    fun `deleteAlbumRequest should log and delete for trusted users`() {
        requestingUser.rank=Rank().apply {id=10}
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(albumRepository.findAlbumById(1L)).thenReturn(album)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)

        albumService.deleteAlbumRequest(1,"tester")

        verify(albumRepository).deleteById(1L)
        verify(bandService).calculateBandsGenre(2)
        verify(contributionRepository,times(9)).save(any(Contribution::class.java))
    }

    private fun stubAddDependencies(dto:AlbumAddDto) {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(bandService.getBandById(dto.bandId!!)).thenReturn(band)
        `when`(genreRepository.findGenreById(dto.mainSubgenre!!)).thenReturn(genre)
        `when`(albumRepository.findIdByData(dto.bandId,dto.title!!)).thenReturn(9)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
    }

    private fun stubEditDependencies() {
        `when`(userAccountService.getUserByLogin("tester")).thenReturn(requestingUser)
        `when`(rankService.checkRank(requestingUser)).thenReturn(null)
        `when`(albumRepository.findAlbumById(1L)).thenReturn(album)
        `when`(contributionRepository.findTopChangeId()).thenReturn(null)
    }

    private fun Album.copyForTest(title:String)=Album().apply {
        id=this@copyForTest.id
        band=this@copyForTest.band
        this.title=title
        releaseDate=this@copyForTest.releaseDate
        type=this@copyForTest.type
        importance=this@copyForTest.importance
        genre=this@copyForTest.genre
    }
}

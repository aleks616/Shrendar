package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.common.model.CountryDto
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.service.GenreService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

class BandWikiServiceTest {
    private val bandRepository=mock(BandRepository::class.java)
    private val genreService=mock(GenreService::class.java)
    private val bandService=mock(BandService::class.java)
    private val bandsMemberService=mock(BandsMemberService::class.java)
    private val albumRepository=mock(AlbumRepository::class.java)
    private lateinit var service:BandWikiService

    @BeforeEach
    fun setup() {
        service=BandWikiService(
            bandRepository,
            genreService,
            bandService,
            bandsMemberService,
            albumRepository,
            userBandRepository,
        )
    }

    @Test
    fun `getBandByIdWiki should reject a missing band`() {
        `when`(bandService.doesBandExist(1)).thenReturn(false)

        val exception=assertThrows<IllegalArgumentException> {service.getBandByIdWiki(1)}

        assertEquals("band_not_exist",exception.message)
    }

    @Test
    fun `getBandByIdWiki should map wiki data`() {
        val band=Band().apply {
            id=1
            name="Metallica"
            formedYear=1981
            status=Status.ACTIVE
            imageUrl="https://example.com/metallica.jpg"
        }
        `when`(bandService.doesBandExist(1)).thenReturn(true)
        `when`(bandRepository.findBandById(1)).thenReturn(band)
        `when`(bandService.getBandsCountry(1)).thenReturn(CountryDto(1,"USA"))
        `when`(genreService.getBandAlbumGenresList(1))
            .thenReturn(listOf(GenreDto(id=10,name="Rock",value=8)))
        `when`(bandsMemberService.getAllBandMembers(1)).thenReturn(emptyList())
        `when`(albumRepository.findByBandId(1)).thenReturn(emptyList())
        `when`(bandService.getSimilarBands(1,8)).thenReturn(emptyList())

        val result=service.getBandByIdWiki(1)

        assertEquals("Metallica",result.name)
        assertEquals(1981,result.formedYear)
        assertEquals("Active",result.status)
        assertEquals("USA",result.country)
        assertEquals("https://example.com/metallica.jpg",result.imageUrl)
        assertEquals("Rock",result.computedGenres?.single()?.name)
        assertTrue(result.bandMembers.isNullOrEmpty())
        assertTrue(result.albums.isNullOrEmpty())
    }

    @Test
    fun `getAllBandMembersWiki should map service members`() {
        val members=listOf(
            BandsMembersDto(
                id=10,
                artistId=2,
                artistName="James Hetfield",
                bandId=3,
                nickname=null,
                yearRole=mutableListOf("Vocals (1981-)")
            )
        )
        `when`(bandsMemberService.getAllBandMembers(3)).thenReturn(members)

        val result=service.getAllBandMembersWiki(3)

        assertEquals(1,result.size)
        assertEquals(10,result.single().id)
        assertEquals("James Hetfield",result.single().artistName)
        assertEquals(listOf("Vocals (1981-)"),result.single().yearRole)
    }
}

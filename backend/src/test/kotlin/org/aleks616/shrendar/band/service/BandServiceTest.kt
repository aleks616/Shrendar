package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandsGenres
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.service.GenreService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class BandServiceTest {

    private val bandRepository=mock(BandRepository::class.java)
    private val countryRepository=mock(CountryRepository::class.java)
    private val genreService=mock(GenreService::class.java)
    private val bandsGenreRepository=mock(BandsGenreRepository::class.java)
    private val genreRepository=mock(GenreRepository::class.java)
    private val bandService=BandService(
        bandRepository,
        countryRepository,
        genreService,
        bandsGenreRepository,
        genreRepository
    )

    @Test
    fun `calculateBandsGenre should calculate and save genre`() {
        val bandId=1
        val genreId=10
        val band=Band().apply {id=bandId; name="Metallica"}
        val genre=Genre().apply {id=genreId; properties="1234567"}
        val genreDto=GenreDto(id=genreId,value=100)

        `when`(genreService.getBandAlbumGenresList(bandId)).thenReturn(listOf(genreDto))
        `when`(genreRepository.findGenreById(genreId)).thenReturn(mutableListOf(genre))
        `when`(bandRepository.findBandById(bandId)).thenReturn(band)

        bandService.calculateBandsGenre(bandId)

        verify(bandsGenreRepository).deleteByBandsId(bandId)
        verify(bandsGenreRepository).save(any(BandsGenres::class.java))
        verify(bandRepository).save(band)
    }
}

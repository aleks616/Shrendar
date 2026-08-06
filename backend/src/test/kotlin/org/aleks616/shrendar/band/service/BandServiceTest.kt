package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandsGenres
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.user.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class BandServiceTest {

    private val bandRepository=mock(BandRepository::class.java)
    private val countryRepository=mock(CountryRepository::class.java)
    private val genreService=mock(GenreService::class.java)
    private val bandsGenreRepository=mock(BandsGenreRepository::class.java)
    private val genreRepository=mock(GenreRepository::class.java)
    private val contributionService=mock(ContributionService::class.java)
    private val userService=mock(UserService::class.java)
    private val contributionRepository=mock(ContributionRepository::class.java)
    private val bandsMemberRepository=mock(BandsMemberRepository::class.java)
    private val artistService=mock(ArtistService::class.java)
    private val bandService=BandService(
        artistService,
        bandRepository,
        bandsGenreRepository,
        bandsMemberRepository,
        contributionRepository,
        contributionService,
        countryRepository,
        genreService,
        userService,
        genreRepository,
    )

    @Test
    fun `calculateBandsGenre should calculate and save genre`() {
        val bandId=1
        val genreId=10
        val band=Band().apply {id=bandId; name="Metallica"}
        val genre=Genre().apply {id=genreId; properties="1234567"}
        val genreDto=GenreDto(id=genreId,name="rock",value=8)

        `when`(genreService.getBandAlbumGenresList(bandId)).thenReturn(listOf(genreDto))
        `when`(genreRepository.findGenreById(genreId)).thenReturn(genre)
        `when`(bandRepository.findBandById(bandId)).thenReturn(band)

        bandService.calculateBandsGenre(bandId)

        verify(bandsGenreRepository).deleteByBandsId(bandId)
        verify(bandsGenreRepository).save(any(BandsGenres::class.java))
        verify(bandRepository).save(band)
    }
}

package org.aleks616.shrendar.controllers

import org.aleks616.shrendar.repositories.*
import org.aleks616.shrendar.services.*
import org.aleks616.shrendar.user.RankService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AllControllers(
    private val albumService:AlbumService,
    private val artistService:ArtistService,
    private val bandService:BandService,
    private val bandsGenreRepository:BandsGenreRepository,
    private val bandsMemberRepository:BandsMemberRepository,
    private val contributionService:ContributionService,
    private val eventService:EventService,
    private val genreService:GenreService,
    private val rankService:RankService,
    private val userArtistRepository:UserArtistRepository,
    private val userBandRepository:UserBandRepository,
    private val userGenreRepository:UserGenreRepository
) {

    @GetMapping("/albums")
    fun getAlbum()=albumService.getAll()

    @GetMapping("/artist")
    fun getArtist()=artistService.getAll()

    @GetMapping("/artistBirthdays")
    fun getArtistWithBirthdayInDate(@RequestParam month:Int,@RequestParam day:Int)=
        artistService.getTodayBirthdays(month,day)

    @GetMapping("/artistDeaths")
    fun getArtistWithDeathInDate(@RequestParam month:Int,@RequestParam day:Int)=
        artistService.getTodayDeathAnniversaries(month,day)

    @GetMapping("/recentDeaths")
    fun getRecentArtistDeathAnniversaries()=artistService.getRecentDeaths()

    @GetMapping("/albumsInDate")
    fun getAlbumAnniversariesByDate(@RequestParam month:Int,@RequestParam day:Int)=
        albumService.getAlbumAnniversariesByDate(month,day)

    @GetMapping("/eventsInDate")
    fun getEventsByDate(@RequestParam month:Int,@RequestParam day:Int)=eventService.getEventsByDate(month,day)

    @GetMapping("/bands")
    fun getBand()=bandService.getAll()

    @GetMapping("/bandsGenres")
    fun getBandsGenre()=bandsGenreRepository.findAll()

    @GetMapping("/bandsMember")
    fun getBandsMember()=bandsMemberRepository.findAll()

    @GetMapping("/contributions")
    fun getContribution()=contributionService.getAll()

    @GetMapping("/genres")
    fun getGenre()=genreService.getAll()

    @GetMapping("/ranks")
    fun getRank()=rankService.getAll()

    @GetMapping("/userArtist")
    fun getUserArtist()=userArtistRepository.findAll()

    @GetMapping("/userBand")
    fun getUserBand()=userBandRepository.findAll()

    @GetMapping("/userGenre")
    fun getUserGenre()=userGenreRepository.findAll()

}
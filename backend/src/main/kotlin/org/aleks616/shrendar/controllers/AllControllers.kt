package org.aleks616.shrendar.controllers

import org.aleks616.shrendar.repositories.*
import org.aleks616.shrendar.services.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AllControllers(
    private val albumRepository:AlbumService,
    private val artistRepository:ArtistService,
    private val bandRepository:BandService,
    private val bandsGenreRepository:BandsGenreRepository,
    private val bandsMemberRepository:BandsMemberRepository,
    private val contributionRepository:ContributionService,
    private val genreRepository:GenreService,
    private val rankRepository:RankService,
    private val userArtistRepository:UserArtistRepository,
    private val userBandRepository:UserBandRepository,
    private val userGenreRepository:UserGenreRepository,
    private val userRepository:UserService,
){
    data class RegisterRequest(
        val login:String,
        val displayName:String,
        val email:String,
        val password:String
    )
    @PostMapping("/register")
    fun registerData(@RequestBody request:RegisterRequest){
        userRepository.createUser(request)
    }

    @GetMapping("/loginCheck")
    fun doesLoginExist(@RequestParam login:String):Boolean=userRepository.doesLoginExist(login)

    @GetMapping("/emailCheck")
    fun doesEmailExist(@RequestParam email:String):Boolean=userRepository.doesAccountWithEmailExist(email)


    @GetMapping("/albums")
    fun getAlbum()=albumRepository.getAll()

    @GetMapping("/artist")
    fun getArtist()=artistRepository.getAll()

    @GetMapping("/bands")
    fun getBand()=bandRepository.getAll()

    @GetMapping("/bandsGenres")
    fun getBandsGenre()=bandsGenreRepository.findAll()

    @GetMapping("/bandsMember")
    fun getBandsMember()=bandsMemberRepository.findAll()

    @GetMapping("/contributions")
    fun getContribution()=contributionRepository.getAll()

    @GetMapping("/genres")
    fun getGenre()=genreRepository.getAll()

    @GetMapping("/ranks")
    fun getRank()=rankRepository.getAll()

    @GetMapping("/userArtist")
    fun getUserArtist()=userArtistRepository.findAll()

    @GetMapping("/userBand")
    fun getUserBand()=userBandRepository.findAll()

    @GetMapping("/userGenre")
    fun getUserGenre()=userGenreRepository.findAll()

    @GetMapping("/users")
    fun getUsers()=userRepository.getUsersDto()


}
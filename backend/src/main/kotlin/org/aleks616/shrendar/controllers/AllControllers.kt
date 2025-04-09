package org.aleks616.shrendar.controllers

import org.aleks616.shrendar.repositories.*
import org.aleks616.shrendar.services.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AllControllers(
    private val albumService:AlbumService,
    private val artistService:ArtistService,
    private val bandService:BandService,
    private val bandsGenreRepository:BandsGenreRepository,
    private val bandsMemberRepository:BandsMemberRepository,
    private val contributionService:ContributionService,
    private val genreService:GenreService,
    private val rankService:RankService,
    private val userArtistRepository:UserArtistRepository,
    private val userBandRepository:UserBandRepository,
    private val userGenreRepository:UserGenreRepository,
    private val userService:UserService,
){
    data class RegisterRequest(
        val login:String,
        val displayName:String,
        val email:String,
        val password:String
    )
    data class LoginRequest(
        val login:String?,
        val email:String?,
        val password:String
    )

    @PostMapping("/register")
    fun registerData(@RequestBody request:RegisterRequest){
        userService.createUser(request)
    }

    @GetMapping("/loginCheck")
    fun doesLoginExist(@RequestParam login:String):Boolean=userService.doesLoginExist(login)

    @GetMapping("/emailCheck")
    fun doesEmailExist(@RequestParam email:String):Boolean=userService.doesAccountWithEmailExist(email)

    @PostMapping("/passwordCheck")
    fun isPasswordCorrect(@RequestBody request:LoginRequest):Boolean=userService.isPasswordCorrect(request)

    @GetMapping("/albums")
    fun getAlbum()=albumService.getAll()

    @GetMapping("/artist")
    fun getArtist()=artistService.getAll()

    @GetMapping("/artistBirthdays")
    fun getArtistWithBirthdayInDate(@RequestParam month:Int, @RequestParam day:Int)=artistService.getTodayBirthdays(month,day)

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

    @GetMapping("/users")
    fun getUsers()=userService.getUsersDto()


}
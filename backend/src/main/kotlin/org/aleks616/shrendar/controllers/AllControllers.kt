package org.aleks616.shrendar.controllers

import org.aleks616.shrendar.repositories.*
import org.aleks616.shrendar.services.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.security.TokenBlacklistService
import org.aleks616.shrendar.security.JwtUtil

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
    private val userGenreRepository:UserGenreRepository,
    private val userService:UserService,
    private val rateLimiter:RateLimiter,
    private val tokenBlacklistService:TokenBlacklistService
) {
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
    fun registerData(@RequestBody request:RegisterRequest,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("reg:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many registration attempts from this IP")
        else if(!rateLimiter.allowRequest("reg:email:${request.email}",5,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many registration attempts for this email")
        else if(userService.initiateRegistration(request))
            ResponseEntity.ok("Verification code sent to email if not already registered")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cannot initiate registration: email/login may exist or rate-limited")
    }

    @PostMapping("/register/confirm")
    fun confirmRegistration(
        @RequestBody request:RegisterRequest,
        @RequestParam code:String,
        servletRequest:HttpServletRequest
    ):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("regconfirm:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many confirmation attempts from this IP")
        else if(userService.createUser(request,code))
            ResponseEntity.ok("Account created and verified")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid code or registration could not be completed")
    }

    @GetMapping("/loginCheck")
    fun doesLoginExist(@RequestParam login:String):Boolean=userService.doesLoginExist(login)

    @GetMapping("/emailCheck")
    fun doesEmailExist(@RequestParam email:String):Boolean=userService.doesAccountWithEmailExist(email)

    @PostMapping("/login")
    fun login(@RequestBody request:LoginRequest,servletRequest:HttpServletRequest):ResponseEntity<Any> {
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("login:ip:$ip",10,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mapOf("error" to "Too many login attempts"))
        val accountKey=request.email?:request.login?:"unknown"
        if(!rateLimiter.allowRequest("login:acct:$accountKey",5,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mapOf("error" to "Too many login attempts"))

        val subject=userService.authenticate(request)?:return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to "Invalid credentials"))
        val token=JwtUtil.createToken(subject)
        return ResponseEntity.ok(mapOf("token" to token))
    }

    @PostMapping("/logout")
    fun logout(servletRequest:HttpServletRequest):ResponseEntity<String> {
        val header=servletRequest.getHeader("Authorization")
        if(header!=null&&header.startsWith("Bearer ")) {
            val token=header.substringAfter("Bearer ").trim()
            val expiry=JwtUtil.getExpiration(token)
            if(expiry!=null) {
                tokenBlacklistService.blacklistToken(token,expiry)
            }
        }
        return ResponseEntity.ok("Logged out")
    }

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

    @GetMapping("/users")
    fun getUsers()=userService.getUsersDto()


}
package org.aleks616.shrendar.artist.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.*
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.ArtistBandsStatusDto
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.service.CountryService
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.userban.service.UserBanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/artist")
class ArtistController(
    private val artistService:ArtistService,
    private val rateLimiter:RateLimiter,
    private val countryService:CountryService,
    private val bandsMemberService:BandsMemberService,
    private val bandService:BandService,
    private val userBanService:UserBanService,
) {
    @GetMapping("/")
    fun getAll():List<Artist>{
        return artistService.getAll()
    }

    @GetMapping("/id/{id}")
    fun getById(@PathVariable id:Long):Artist{
        return artistService.getById(id)
    }

    //WIKI ARTIST PAGE 1/2
    @GetMapping("/wiki/{id}")
    fun getByIdWiki(@PathVariable id:Long):ArtistWikiDto{
        return artistService.getByIdWiki(id)
    }

    @GetMapping("/name")
    fun getByNameLike(@RequestParam name:String):List<Artist>{
        if(name.length<3) throw IllegalArgumentException("name_at_least_3")
        return artistService.getByNameLike(name)
    }

    @GetMapping("/first-name")
    fun getByFirstName(@RequestParam name:String):List<Artist>{
        if(name.length<3) throw IllegalArgumentException("name_at_least_2")
        return artistService.getByFirstName(name)
    }

    @GetMapping("/last-name")
    fun getByLastName(@RequestParam name:String):List<Artist>{
        if(name.length<3) throw IllegalArgumentException("name_at_least_2")
        return artistService.getByLastName(name)
    }

    @GetMapping("/birthdate")
    fun getByBirthdate(@RequestParam month:Int,@RequestParam day:Int):List<ArtistAnniversaryDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("invalid_month_day")
        return artistService.getByBirthday(month,day)
    }

    @GetMapping("/birthdaybetween")
    fun getByBirthdayBetween(@RequestParam startMonth:Int,@RequestParam startDay:Int,@RequestParam endMonth:Int,@RequestParam endDay:Int):List<Artist>{
        if(!Utils.doesDateExist(startMonth,startDay)||!Utils.doesDateExist(endMonth,endDay)) throw IllegalArgumentException("invalid_month_day")
        return artistService.getByBirthdayBetween(startMonth,startDay,endMonth,endDay)
    }

    @GetMapping("/birthyear/{year}")
    fun getByBirthYear(@PathVariable year:Int):List<Artist>{
        return artistService.getByBirthYear(year)
    }

    @GetMapping("/birthyear/")
    fun getByBirthYearBetween(@RequestParam startYear:Int,@RequestParam endYear:Int):List<Artist>{
        return artistService.getByBirthYearBetween(startYear,endYear)
    }

    @GetMapping("/recentBirthdays")
    fun getRecentArtistBirthdays():List<Artist>{
        return artistService.getRecentBirthdays()
    }

    @GetMapping("/deathDate")
    fun getByDeathDate(@RequestParam month:Int,@RequestParam day:Int):List<ArtistAnniversaryDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("invalid_month_day")
        return artistService.getByDeathDate(month,day)
    }

    @GetMapping("/recentDeaths")
    fun getRecentArtistDeathAnniversaries():List<Artist>{
        return artistService.getRecentDeathsAnniversaries()
    }

    @GetMapping("/country/{country}")
    fun getByCountry(@PathVariable country:Int):List<Artist>{
        return artistService.getByCountry(country)
    }

    @GetMapping("/{id}/bands-data")
    fun getArtistBands(@PathVariable id:Long):List<ArtistBandsStatusDto>{
        return bandsMemberService.getArtistBandsList(id)
    }

    @GetMapping("/genres/{id}")
    fun getArtistGenres(@PathVariable id:Long):ArtistGenreDto{
        return artistService.getArtistGenres(id)
    }

    @PostMapping("/add")
    fun addArtist(@RequestBody artist:ArtistAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned")
        if(artist.name.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_artist_add_data")
        if(artistValidate(artist)!=null)
            return artistValidate(artist)!!

        try{
            artistService.addArtistRequest(artist,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("artist_addition_received")
    }

    @PutMapping("/edit")
    fun editArtist(@RequestBody artist:ArtistAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned")
        if(artist.id==null||artist.name.isNullOrEmpty()||artist.gender==null)
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_artist_edit_data")
        if(!artistService.doesArtistExist(artist.id))
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_not_exist")
        
        try{
            artistService.editArtistRequest(artist,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("artist_edition_received")
    }

    @DeleteMapping("/delete")
    fun deleteArtist(@RequestParam id:Long,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned")
        if(!artistService.doesArtistExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_not_exist")

        try{
            artistService.deleteArtistRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("artist_deletion_received")
    }

    @PostMapping("/favorite")
    fun favoriteArtist(@RequestBody artistId:Long, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")
        if(!artistService.doesArtistExist(artistId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_not_exist")

        try{
            artistService.toggleFavoriteArtist(artistId,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }
        return ResponseEntity.ok("artist_toggled")
    }

    @PostMapping("/favoriteAll")
    fun favoriteBandsArtists(@RequestBody bandId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")
        if(!bandService.doesBandExist(bandId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("band_not_exist")

        try{
            artistService.toggleFavoriteArtistByBand(bandId,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }
        return ResponseEntity.ok("artist_toggled")
    }


    fun artistValidate(artist:ArtistAddDto):ResponseEntity<String>?{
        if(artist.birthDate!=null&&artist.deathDate!=null&&artist.birthDate.plusYears(10)>artist.deathDate)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid_artist_birthdate")
        if(artist.birthDate!=null&&artist.birthDate.plusYears(10)>LocalDate.now())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_too_young")
        if(artist.deathDate!=null&&artist.deathDate>LocalDate.now())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid_death_date")
        if(artist.gender!=null&&artist.gender !in listOf('M','F','X'))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid_gender")
        if(artist.country!=null&&(artist.country<1||!countryService.doesCountryExist(artist.country)))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("country_not_exist")
        if(!Utils.isValidUrl(artist.artistImageUrl))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("url_too_long")

        return null
    }

}
package org.aleks616.shrendar.artist.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.service.CountryService
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.security.RateLimiter
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
    private val countryService:CountryService
) {
    @GetMapping("/")
    fun getAll():List<Artist>{
        return artistService.getAll()
    }

    @GetMapping("/id/{id}")
    fun getById(@PathVariable id:Int):Artist{
        return artistService.getById(id)
    }

    //WIKI ARTIST PAGE 1/2
    @GetMapping("/wiki/{id}")
    fun getByIdWiki(@PathVariable id:Int):ArtistWikiDto{
        return artistService.getByIdWiki(id)
    }

    @GetMapping("/name")
    fun getByNameLike(@RequestParam name:String):List<Artist>{
        if(name.length<3) throw IllegalArgumentException("name has to be at least 3 characters")
        return artistService.getByNameLike(name)
    }

    @GetMapping("/first-name")
    fun getByFirstName(@RequestParam name:String):List<Artist>{
        if(name.length<3) throw IllegalArgumentException("name has to be at least 2 characters")
        return artistService.getByFirstName(name)
    }

    @GetMapping("/last-name")
    fun getByLastName(@RequestParam name:String):List<Artist>{
        if(name.length<3) throw IllegalArgumentException("name has to be at least 2 characters")
        return artistService.getByLastName(name)
    }

    @GetMapping("/birthday")
    fun getByBirthday(@RequestParam month:Int,@RequestParam day:Int):List<Artist>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("invalid month or day")
        return artistService.getByBirthday(month,day)
    }

    @GetMapping("/birthdaybetween")
    fun getByBirthdayBetween(@RequestParam startMonth:Int,@RequestParam startDay:Int,@RequestParam endMonth:Int,@RequestParam endDay:Int):List<Artist>{
        if(!Utils.doesDateExist(startMonth,startDay)||!Utils.doesDateExist(endMonth,endDay)) throw IllegalArgumentException("invalid month or day")
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

    @GetMapping("/death")
    fun getByDeathDate(@RequestParam month:Int,@RequestParam day:Int):List<Artist>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("invalid month or day")
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


    @PostMapping("/add")
    fun addArtist(@RequestBody artist:ArtistAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(artist.name.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least artist name is required to add an artist")

        if(artistValidate(artist)!=null)
            return artistValidate(artist)!!

        try{
            artistService.addArtistRequest(artist,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Artist addition request received")
    }

    @PutMapping("/edit")
    fun editArtist(@RequestBody artist:ArtistAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")
       if(artist.id==null||artist.name.isNullOrEmpty()||artist.gender==null)
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist id, name and gender are required")
        if(!artistService.doesArtistExist(artist.id))
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist with id ${artist.id} does not exist")
        
        try{
            artistService.editArtistRequest(artist,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Artist edit request received")
    }

    @DeleteMapping("/delete")
    fun deleteArtist(@RequestParam id:Int,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")
        if(!artistService.doesArtistExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist with id $id does not exist")

        try{
            artistService.deleteArtistRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Artist deletion request received")
    }

    fun artistValidate(artist:ArtistAddDto):ResponseEntity<String>?{
        if(artist.birthDate!=null&&artist.deathDate!=null&&artist.birthDate.plusYears(10)>artist.deathDate)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist has to be at least 10 years old, and death date cannot be before birth date")
        if(artist.birthDate!=null&&artist.birthDate.plusYears(10)>LocalDate.now())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist has to be at least 10 years old")
        if(artist.deathDate!=null&&artist.deathDate>LocalDate.now())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist death date cannot be in the future")
        if(artist.gender!=null&&artist.gender !in listOf('M','F','X'))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist gender must be one of: M, F, X or null")
        if(artist.country!=null&&(artist.country<1||!countryService.doesCountryExist(artist.country)))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("Country with id ${artist.country} does not exist")
        if(!Utils.isValidUrl(artist.artistImageUrl))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URL is not valid or too long (maximum length is 255 characters)")

        return null
    }

}
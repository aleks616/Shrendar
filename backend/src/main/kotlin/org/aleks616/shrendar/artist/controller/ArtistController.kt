package org.aleks616.shrendar.artist.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.security.RateLimiter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/artist")
class ArtistController(
    private val artistService:ArtistService,
    private val rateLimiter:RateLimiter
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
        if(!rateLimiter.allowRequest("reg:ip:$ip",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!artistService.addArtistRequest(artist,userLogin))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("User reached their weekly limit")


        return ResponseEntity.ok("Artist addition request received")
    }


    @PostMapping("/revertAddition")
    fun revertAlbumAddition(@RequestParam changeId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!artistService.revertArtistAddition(changeId,userLogin))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")

        return ResponseEntity.ok("Artist addition revert successful")
    }

}
package org.aleks616.shrendar.artist.controller

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistWikiDto
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.common.Utils
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/artist")
class ArtistController(
    private val artistService:ArtistService,
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
}
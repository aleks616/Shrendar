package org.aleks616.shrendar.band.controller

import org.aleks616.shrendar.band.model.BandDto
import org.aleks616.shrendar.band.model.Status
import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/band")
class BandController (
    private val bandService:BandService,
    private val bandsGenreRepository:BandsGenreRepository,
    private val bandsMemberRepository:BandsMemberRepository,
){

    @GetMapping("/")
    fun getBand():List<BandDto>{
       return bandService.getAll()
    }

    /*@GetMapping("/genres")
    fun getBandsGenre()=bandsGenreRepository.findAll()

    @GetMapping("/member")
    fun getBandsMember()=bandsMemberRepository.findAll()*/

    //all members


    //past members


    //current members


    //by name like
    @GetMapping("/like/{name}")
    fun getBandByNameLike(@PathVariable name:String):List<BandDto>{
        return bandService.getBandsByName(name)
    }

    //by name exact
    @GetMapping("/{name}")
    fun getBandsByNameExact(@PathVariable name:String):List<BandDto>{
        return bandService.getBandsByNameExact(name)
    }

    @GetMapping("/country/{name}")
    fun getBandsByCountry(@PathVariable name:String):List<BandDto>{
        return bandService.getBandsByCountry(name)
    }

    @GetMapping("/foundedBetween")
    fun getBandsByFoundedBetween(@RequestParam startYear:Int?,@RequestParam endYear:Int?):List<BandDto>{
        if(startYear==null && endYear==null) throw IllegalArgumentException("startYear and endYear cannot both be null")
        if(startYear!=null && endYear!=null && startYear>endYear) throw IllegalArgumentException("startYear cannot be greater than endYear")
        if(startYear!=null &&(startYear>LocalDate.now().year)) throw IllegalArgumentException("invalid startYear")
        if(endYear!=null &&(endYear>LocalDate.now().year)) throw IllegalArgumentException("invalid endYear")
        return bandService.getBandsByFoundedBetween(startYear,endYear)
    }

    @GetMapping("/status/{status}")
    fun getBandsByStatus(@PathVariable status:String):List<BandDto>{
        return bandService.getBandsByStatus(statusStringToEnum(status))
    }

    fun statusStringToEnum(statusString:String):Status {
        return when(statusString.lowercase()){
            "active"->Status.active
            "disbanded"->Status.disbanded
            "on_hold"->Status.on_hold
            "on hold"->Status.on_hold
            "unknown"->Status.unknown
            else->throw IllegalArgumentException("invalid status")
        }
    }

}

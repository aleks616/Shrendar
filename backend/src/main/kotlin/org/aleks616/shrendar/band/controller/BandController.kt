package org.aleks616.shrendar.band.controller

import org.aleks616.shrendar.band.model.ArtistBandsHistoryDto
import org.aleks616.shrendar.band.model.BandDto
import org.aleks616.shrendar.band.model.BandsMembersDto
import org.aleks616.shrendar.band.model.Status
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/band")
class BandController (
    private val bandService:BandService,
    private val bandsMemberService:BandsMemberService,
){

    @GetMapping("/")
    fun getAll():List<BandDto>{
       return bandService.getAll()
    }

    @GetMapping("/id/{id}")
    fun getBand(@PathVariable id:Int):BandDto{
        return bandService.getBandById(id)
    }

    //genres

    @GetMapping("/{bandId}/members")
    fun getAllMembersOfBand(@PathVariable bandId:Int):List<BandsMembersDto>{
        return bandsMemberService.getAllBandMembers(bandId)
    }

    @GetMapping("/{bandId}/members/current")
    fun getCurrentBandMembers(@PathVariable bandId:Int):List<BandsMembersDto>{
        return bandsMemberService.getCurrentBandMembers(bandId)
    }

    @GetMapping("/{bandId}/members/past")
    fun getPastBandMembers(@PathVariable bandId:Int):List<BandsMembersDto>{
        return bandsMemberService.getPastBandMembers(bandId)
    }

    @GetMapping("/name-like/{name}")
    fun getBandByNameLike(@PathVariable name:String):List<BandDto>{
        return bandService.getBandsByName(name)
    }

    @GetMapping("/name-exact/{name}")
    fun getBandsByNameExact(@PathVariable name:String):List<BandDto>{
        return bandService.getBandsByNameExact(name)
    }

    @GetMapping("/country")
    fun getBandsByCountryName(@RequestParam name:String):List<BandDto>{
        return bandService.getBandsByCountry(name)
    }

    @GetMapping("/country/")
    fun getBandsByCountryId(@RequestParam id:Int):List<BandDto>{
        return bandService.getBandsByCountryId(id)
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

    @GetMapping("/artist/{id}")
    fun getBandsByArtistId(@PathVariable id:Int):List<ArtistBandsHistoryDto>{
        return bandsMemberService.getBandsByArtistId(id)
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

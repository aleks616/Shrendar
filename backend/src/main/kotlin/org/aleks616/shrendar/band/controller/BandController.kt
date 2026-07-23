package org.aleks616.shrendar.band.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.model.ArtistAddDto
import org.aleks616.shrendar.band.model.ArtistBandsHistoryDto
import org.aleks616.shrendar.band.model.BandAddDto
import org.aleks616.shrendar.band.model.BandDto
import org.aleks616.shrendar.band.model.BandGenreDto
import org.aleks616.shrendar.band.model.BandWikiDto
import org.aleks616.shrendar.band.model.BandsMembersDto
import org.aleks616.shrendar.band.model.BandsMembersWikiDto
import org.aleks616.shrendar.band.model.Status
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
import org.aleks616.shrendar.security.RateLimiter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/band")
class BandController (
    private val bandService:BandService,
    private val bandsMemberService:BandsMemberService,
    private val rateLimiter:RateLimiter
){

    @GetMapping("/")
    fun getAll():List<BandDto>{
       return bandService.getAll()
    }

    @GetMapping("/id/{id}")
    fun getBand(@PathVariable id:Int):BandDto{
        return bandService.getBandDataById(id)
    }

    //WIKI BAND PAGE 1/4
    @GetMapping("/wiki/{id}")
    fun getBandByIdWiki(@PathVariable id:Int):BandWikiDto {
        return bandService.getBandByIdWiki(id)
    }

    //WIKI BAND PAGE 2/4
    @GetMapping("wiki/{bandId}/members")
    fun getAllBandMembersWiki(@PathVariable bandId:Int):List<BandsMembersWikiDto>{
        return bandsMemberService.getAllBandMembersWiki(bandId)
    }

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

    //WIKI ARTIST PAGE 2/2
    @GetMapping("/artist/{id}")
    fun getBandsByArtistId(@PathVariable id:Int):List<ArtistBandsHistoryDto>{
        return bandsMemberService.getBandsByArtistId(id)
    }

    //WIKI BAND PAGE 4/4
    @GetMapping("/similar/{bandId}")
    fun getSimilarBands(@PathVariable bandId:Int, @RequestParam quantity:Int?):List<BandGenreDto>{
        return bandService.getSimilarBands(bandId,quantity?:5)
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

    @PostMapping("/add")
    fun addBandRequest(@RequestBody band:BandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!bandService.addBandRequest(band,userLogin))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("User reached their weekly limit")


        return ResponseEntity.ok("Band addition request received")
    }

    @PostMapping("/revertAddition")
    fun revertBabdAddition(@RequestParam changeId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!bandService.revertBandAddition(changeId,userLogin))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")

        return ResponseEntity.ok("Band addition revert successful")
    }

}

package org.aleks616.shrendar.band.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.artist.service.ArtistService
import org.aleks616.shrendar.band.model.*
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.band.service.BandsMemberService
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
@RequestMapping("/api/band")
class BandController (
    private val bandService:BandService,
    private val bandsMemberService:BandsMemberService,
    private val rateLimiter:RateLimiter,
    private val countryService:CountryService,
    private val artistService:ArtistService
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
    fun getBandsByArtistId(@PathVariable id:Long):List<ArtistBandsHistoryDto>{
        return bandsMemberService.getBandsByArtistId(id)
    }

    //WIKI BAND PAGE 4/4
    @GetMapping("/similar/{bandId}")
    fun getSimilarBands(@PathVariable bandId:Int, @RequestParam quantity:Int?):List<BandGenreDto>{
        return bandService.getSimilarBands(bandId,quantity?:5)
    }

    fun statusStringToEnum(statusString:String):Status {
        return when(statusString.lowercase()){
            "active"->Status.ACTIVE
            "disbanded"->Status.DISBANDED
            "on_hold"->Status.ON_HOLD
            "on hold"->Status.ON_HOLD
            "unknown"->Status.UNKNOWN
            else->throw IllegalArgumentException("invalid status")
        }
    }

    @PostMapping("/add")
    fun addBandRequest(@RequestBody band:BandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(band.name.isNullOrEmpty()||band.status==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least band name and status are required to add a new band")
       if(bandValidate(band)!=null)
            return bandValidate(band)!!

        try{
            bandService.addBandRequest(band,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Band addition request received")
    }

    @PutMapping("/edit")
    fun editBand(@RequestBody band:BandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        if(band.id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band ID is required")
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(band.id==null||band.name.isNullOrEmpty()||band.status==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band id, name and status are required")
        if(!bandService.doesBandExist(band.id!!))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band with id ${band.id} does not exist")
        if(bandValidate(band)!=null)
            return bandValidate(band)!!

        try{
            bandService.editBandRequest(band,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Band edit request received")
    }

    @DeleteMapping("/delete")
    fun deleteBand(@RequestParam id:Int,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")
        if(!bandService.doesBandExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band with id $id does not exist")

        try{
            bandService.deleteBandRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Band deletion request received")
    }

    @PostMapping("/member-add")
    fun addBandMembersRequest(@RequestBody member:ArtistBandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",20,120))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",20,120))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(member.artistId==null||member.bandId==null||member.role==null||member.joinedYear==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least artist id, band id, role and joined year are required")
        if(memberValidate(member)!=null)
            return memberValidate(member)!!
        if(bandService.doesSameMemberExist(member))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Member with id, role and joined year or left year already exists")

        try{
            bandsMemberService.addBandMemberRequest(member,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Band member addition request received")
    }

    @PutMapping("/member-edit")
    fun editBandMembersRequest(@RequestBody member:ArtistBandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String>{
        if(member.id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band member ID is required")
        val user=SecurityContextHolder.getContext().authentication?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",20,120))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",20,120))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(member.id==null||member.artistId==null||member.bandId==null||member.role==null||member.joinedYear==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Member id, artist id, band id, role and joined year are required")
        if(!bandService.doesBandMemberExist(member.id!!))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band member with id ${member.id} doesn't exists")
        if(memberValidate(member)!=null)
            return memberValidate(member)!!

        try{
            bandsMemberService.editBandMemberRequest(member,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Band member edit request received")
    }


    @DeleteMapping("/member-delete")
    fun deleteBandMembersRequest(@RequestParam id:Long,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")
        if(!bandsMemberService.doesBandMemberExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band member with id $id does not exist")

        try{
            bandsMemberService.deleteBandMemberRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Band deletion request received")
    }

    @PostMapping("/favorite")
    fun favoriteBand(@RequestBody bandId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")
        if(!bandService.doesBandExist(bandId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist with id $bandId does not exist")

        try{
            bandService.toggleFavoriteBand(bandId,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }
        return ResponseEntity.ok("Band favorite toggled successfully")
    }

    fun bandValidate(band:BandAddDto):ResponseEntity<String>?{
        if(band.formedYear!=null&&band.formedYear!!>LocalDate.now().year)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band formed year cannot be in the future")
        if(band.disbandedYear!=null&&band.formedYear==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band formed year is required if disbanded year is provided")
        }
        if(band.formedYear!=null&&band.formedYear!!<1901)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band formed year cannot be before 1901")
        if(band.disbandedYear!=null&&band.disbandedYear!!<1901)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band disbanded year cannot be before 1901")
        if(band.formedYear!=null&&band.disbandedYear!=null&&band.formedYear!!>band.disbandedYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band disbanded year cannot be before formed year")
        if(band.disbandedYear!=null&&band.status!=Status.DISBANDED)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band status must be disbanded if disbanded year is provided")
        if(band.disbandedYear==null&&band.status==Status.DISBANDED)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Disbanded year is required if band status is disbanded")
        if(band.country!=null&&!countryService.doesCountryExist(band.country!!))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Country with id ${band.country} does not exist")
        if(band.imageUrl!=null&&(band.imageUrl!!.length>255||!Utils.isValidUrl(band.imageUrl!!)))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image url can't be more than 255 characters and has to be valid URL")

        return null
    }

    fun memberValidate(member:ArtistBandAddDto):ResponseEntity<String>?{
        if(member.joinedYear!=null&&member.joinedYear!!>LocalDate.now().year)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Joined year can't be in the future")
        if(member.leftYear!=null&&member.leftYear!!>LocalDate.now().year)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Left year can't be in the future")
        if(member.leftYear!=null&&member.joinedYear!=null&&member.joinedYear!!>member.leftYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Left year has to be the same or greater than joined year")

        if(member.artistId!=null&&!artistService.doesArtistExist(member.artistId!!)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist with id ${member.artistId} does not exist")
        }
        if(member.bandId!=null&&!bandService.doesBandExist(member.bandId!!)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Band with id ${member.bandId} does not exist")
        }
        val artistId:Long=if(member.artistId!=null) member.artistId!!
            else bandService.getBandMemberById(member.bandId!!).artist!!.id!!

        val artist=artistService.getById(artistId)
        if(member.joinedYear!=null&&artist.birthDate!=null&&artist.birthDate!!.year+10>member.joinedYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist has to be at least 10 years old when joining the band")
        if(member.joinedYear!=null&&artist.deathDate!=null&&artist.deathDate!!.year<member.joinedYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist has to be alive when joining the band")
        if(artist.deathDate!=null&&member.leftYear!=null&&artist.deathDate!!.year>member.leftYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Artist has to leave the band when dying")
        if(member.nickname!=null&&member.nickname!!.length>255)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nickname can't be longer than 255 characters")
        if(member.role!=null&&member.role!!.length>20)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role can't be longer than 20 characters. Input roles separately")

        return null
    }

}

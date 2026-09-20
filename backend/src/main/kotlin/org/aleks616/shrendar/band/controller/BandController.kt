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
import org.aleks616.shrendar.userban.service.UserBanService
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
    private val artistService:ArtistService,
    private val userBanService:UserBanService
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
        if(startYear==null && endYear==null) throw IllegalArgumentException("start_end_not_null")
        if(startYear!=null && endYear!=null && startYear>endYear) throw IllegalArgumentException("start_before_end")
        if(startYear!=null &&(startYear>LocalDate.now().year)) throw IllegalArgumentException("invalid_start_year")
        if(endYear!=null &&(endYear>LocalDate.now().year)) throw IllegalArgumentException("invalid_end_year")
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
            else->throw IllegalArgumentException("invalid_status")
        }
    }

    @PostMapping("/add")
    fun addBand(@RequestBody band:BandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned.")
        if(band.name.isNullOrEmpty()||band.status==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_band_add_data")
        if(bandValidate(band)!=null)
            return bandValidate(band)!!

        try{
            bandService.addBand(band,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("band_addition_received")
    }

    @PutMapping("/edit")
    fun editBand(@RequestBody band:BandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        if(band.id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("band_id_required")
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned.")
        if(band.name.isNullOrEmpty()||band.status==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_band_edit_data")
        if(!bandService.doesBandExist(band.id!!))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("band_not_exist")
        if(bandValidate(band)!=null)
            return bandValidate(band)!!

        try{
            bandService.editBandRequest(band,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("band_edition_received")
    }

    @DeleteMapping("/delete")
    fun deleteBand(@RequestParam id:Int,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned.")
        if(!bandService.doesBandExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("band_not_exist")

        try{
            bandService.deleteBandRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("band_deletion_received")
    }

    @PostMapping("/member-add")
    fun addBandMember(@RequestBody member:ArtistBandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned.")
        if(member.artistId==null||member.bandId==null||member.role==null||member.joinedYear==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_member_add_data")
        if(memberValidate(member)!=null)
            return memberValidate(member)!!
        if(bandService.doesSameMemberExist(member))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("member_exists")

        try{
            bandsMemberService.addBandMember(member,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("member_addition_received")
    }

    @PutMapping("/member-edit")
    fun editBandMember(@RequestBody member:ArtistBandAddDto,servletRequest:HttpServletRequest):ResponseEntity<String>{
        if(member.id==null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("member_id_required")
        val user=SecurityContextHolder.getContext().authentication?:return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned.")
        if(member.artistId==null||member.bandId==null||member.role==null||member.joinedYear==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_member_edit_data")
        if(!bandService.doesBandMemberExist(member.id!!))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("member_not_exist")
        if(memberValidate(member)!=null)
            return memberValidate(member)!!

        try{
            bandsMemberService.editBandMember(member,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("member_edition_received")
    }


    @DeleteMapping("/member-delete")
    fun deleteBandMember(@RequestParam id:Long,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("you_are_banned.")
        if(!bandsMemberService.doesBandMemberExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("member_not_exist")

        try{
            bandsMemberService.deleteBandMember(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("member_deletion_received")
    }

    @PostMapping("/favorite")
    fun favoriteBand(@RequestBody bandId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
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
            bandService.toggleFavoriteBand(bandId,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }
        return ResponseEntity.ok("band_toggled")
    }

    fun bandValidate(band:BandAddDto):ResponseEntity<String>?{
        if(band.formedYear!=null&&band.formedYear!!>LocalDate.now().year)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("formed_year_future")
        if(band.disbandedYear!=null&&band.formedYear==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_formed")
        }
        if(band.formedYear!=null&&band.formedYear!!<1901)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("formed_before_min")
        if(band.disbandedYear!=null&&band.disbandedYear!!<1901)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("disbanded_before_min")
        if(band.formedYear!=null&&band.disbandedYear!=null&&band.formedYear!!>band.disbandedYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("disbanded_before_formed")
        if(band.disbandedYear!=null&&band.status!=Status.DISBANDED)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("inconsistent_status")
        if(band.disbandedYear==null&&band.status==Status.DISBANDED)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_disbanded")
        if(band.country!=null&&!countryService.doesCountryExist(band.country!!))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("country_not_exist")
        if(band.imageUrl!=null&&(band.imageUrl!!.length>255||!Utils.isValidUrl(band.imageUrl!!)))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("url_too_long")

        return null
    }

    fun memberValidate(member:ArtistBandAddDto):ResponseEntity<String>?{
        if(member.joinedYear!=null&&member.joinedYear!!>LocalDate.now().year)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("joined_future")
        if(member.leftYear!=null&&member.leftYear!!>LocalDate.now().year)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("left_future")
        if(member.leftYear!=null&&member.joinedYear!!>member.leftYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Left year has to be the same or greater than joined year")

        if(member.artistId!=null&&!artistService.doesArtistExist(member.artistId!!)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_not_exist")
        }
        if(member.bandId!=null&&!bandService.doesBandExist(member.bandId!!)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("band_not_exist")
        }
        val artistId:Long=if(member.artistId!=null) member.artistId!!
            else bandService.getBandMemberById(member.bandId!!).artist!!.id!!

        val artist=artistService.getById(artistId)
        if(member.joinedYear!=null&&artist.birthDate!=null&&artist.birthDate!!.year+10>member.joinedYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_too_young_joining")
        if(member.joinedYear!=null&&artist.deathDate!=null&&artist.deathDate!!.year<member.joinedYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_dead_joining")
        if(artist.deathDate!=null&&member.leftYear!=null&&artist.deathDate!!.year<member.leftYear!!)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("artist_leave_when_dead")
        if(member.nickname!=null&&member.nickname!!.length>255)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("nickname_too_long")
        if(member.role!=null&&member.role!!.length>20)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("role_too_long")

        return null
    }

}

package org.aleks616.shrendar.event.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.band.service.BandService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.event.model.EventAddDto
import org.aleks616.shrendar.event.service.EventService
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.userban.service.UserBanService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/event")
class EventController(
    private val rateLimiter:RateLimiter,
    private val eventService:EventService,
    private val userBanService:UserBanService,
    private val bandService:BandService,
) {

    @PostMapping("/add")
    fun addEvent(@RequestBody event:EventAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
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
        if(validateEvent(event)!=null)
            return validateEvent(event)!!

        try{
            eventService.addEventRequest(event,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("event_addition_received")
    }

    @PutMapping("/edit")
    fun editEvent(@RequestBody event:EventAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
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
        if(event.id==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("event_id_required")
        if(!eventService.doesEventExist(event.id))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("event_not_exist")
        if(validateEvent(event)!=null)
            return validateEvent(event)!!

        try{
            eventService.editEventRequest(event,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error ${e.message}")
        }

        return ResponseEntity.ok("event_edition_received")
    }

    @DeleteMapping("/delete")
    fun deleteEvent(@RequestParam id:Int,servletRequest:HttpServletRequest):ResponseEntity<String>{
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
        if(!eventService.doesEventExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("event_not_exist")

        try{
            eventService.deleteEventRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("event_delete_received")
    }

    fun validateEvent(event:EventAddDto):ResponseEntity<String>?{
        if(event.bandId==null||event.bandId<1||event.date==null||event.name.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing_event_add_data")
        if(event.name.length>120)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("event_name_too_long")
        if(event.date.isAfter(LocalDate.now().plusYears(2)))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid_event_date")
        if(!bandService.doesBandExist(event.bandId))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("band_not_exist")
        return null
    }
}

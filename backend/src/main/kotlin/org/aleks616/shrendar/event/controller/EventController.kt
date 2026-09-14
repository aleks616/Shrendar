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
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You're banned, your site access is view-only. If you think this is a mistake, file an appeal.")
        if(validateEvent(event)!=null)
            return validateEvent(event)!!

        try{
            eventService.addEventRequest(event,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Event addition request received")
    }

    @PutMapping("/edit")
    fun editEvent(@RequestBody event:EventAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You're banned, your site access is view-only. If you think this is a mistake, file an appeal.")
        if(event.id==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event id is required")
        if(!eventService.doesEventExist(event.id))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("Event with id ${event.id} does not exist")
        if(validateEvent(event)!=null)
            return validateEvent(event)!!

        try{
            eventService.editEventRequest(event,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Event edit request received")
    }

    @DeleteMapping("/delete")
    fun deleteEvent(@RequestParam id:Int,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(userBanService.isBanned(userLogin))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You're banned, your site access is view-only. If you think this is a mistake, file an appeal.")
        if(!eventService.doesEventExist(id))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event with id $id does not exist")

        try{
            eventService.deleteEventRequest(id,userLogin)
        }
        catch (e:ContributionLimitExceededException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Event deletion request received")
    }

    fun validateEvent(event:EventAddDto):ResponseEntity<String>?{
        if(event.bandId==null||event.bandId<1||event.date==null||event.name.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least band, date and name are required to add an event")
        if(event.name.length>120)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event name must be shorter than 120 characters long")
        if(event.date.isAfter(LocalDate.now().plusYears(2)))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event date must be in the past or up to 2 years in the future")
        if(!bandService.doesBandExist(event.bandId))
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body("Band with id ${event.bandId} does not exist")
        return null
    }
}

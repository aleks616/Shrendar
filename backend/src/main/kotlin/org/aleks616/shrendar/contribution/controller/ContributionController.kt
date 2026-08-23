package org.aleks616.shrendar.contribution.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.model.Table
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.ContributionDto
import org.aleks616.shrendar.contribution.service.ContributionRevertService
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.exception.RankTooLowToRevertConfirmedContribution
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/contribution")
class ContributionController (
    private val contributionService:ContributionService,
    private val contributionRevertService:ContributionRevertService,
    private val rateLimiter:RateLimiter,
    private val userService:UserService,
){
    @GetMapping("/")
    fun getContributions()=contributionService.getAll()

    @PostMapping("/confirm")
    fun confirmAddRequest(@RequestParam changeId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!contributionService.confirmDataAddRequest(changeId,userLogin))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")

        return ResponseEntity.ok("Confirmation successful")
    }

    @PostMapping("/revert")
    fun revertAddRequest(@RequestParam changeId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        try{
           contributionRevertService.revertAddition(changeId,userLogin)
        }
        catch(e:RankTooLowToRevertConfirmedContribution){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong. ${e.message}")
        }

        return ResponseEntity.ok("Addition reverted successful")
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleLimitExceededException(e:IllegalStateException):ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong. ${e.message}")
    }

    @GetMapping("/requested-by/{id}")
    fun getContributionsByRequestingUser(@PathVariable id:Int,servletRequest:HttpServletRequest):List<ContributionDto>{
        if(!userService.doesUserExist(id)) throw IllegalStateException("user with id $id doesn't exist")
        val user=SecurityContextHolder.getContext().authentication?:
                 throw IllegalStateException("User not authenticated")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this user")

        try{
            return contributionService.getContributionsByRequestingUser(id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/confirmed-by/{id}")
    fun getContributionsByConfirmingUser(@PathVariable id:Int,servletRequest:HttpServletRequest):List<ContributionDto>{
        if(!userService.doesUserExist(id)) throw IllegalStateException("user with id $id doesn't exist")
        val user=SecurityContextHolder.getContext().authentication?:
                 throw IllegalStateException("User not authenticated")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this user")

        try{
            return contributionService.getContributionsByConfirmingUser(id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/table/{table}")
    fun getContributionsByTableName(@PathVariable table:String,servletRequest:HttpServletRequest):List<ContributionDto>{
        try{
            val table:Table=Table.valueOf(table.uppercase())
        }
        catch(_:IllegalStateException){
            throw IllegalStateException("table \"$table\" does not exist")
        }

        val user=SecurityContextHolder.getContext().authentication?:
                 throw IllegalStateException("User not authenticated")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this user")

        try{
            return contributionService.getContributionsByTableName(table)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/table-record/{table}")
    fun getContributionsByTableNameAndChangedRecordId(@PathVariable table:String, @RequestParam id:Int,servletRequest:HttpServletRequest):List<ContributionDto>{
        try{
            val table:Table=Table.valueOf(table.uppercase())
        }
        catch(_:IllegalStateException){
            throw IllegalStateException("table \"$table\" does not exist")
        }

        val user=SecurityContextHolder.getContext().authentication?:
                 throw IllegalStateException("User not authenticated")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this user")

        try{
            return contributionService.getContributionsByTableNameAndChangedRecordId(table,id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/between-dates")
    fun getContributionsByChangedAtBetween(@RequestParam start:LocalDate, @RequestParam(required=false) end:LocalDate,servletRequest:HttpServletRequest):List<ContributionDto>{
        val user=SecurityContextHolder.getContext().authentication?:
                 throw IllegalStateException("User not authenticated")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this user")

        if(start.isAfter(end)) throw IllegalStateException("start date cannot be after end date")

        try{
            return contributionService.getContributionsByChangedAtBetween(start,end)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/between-dates-by-user/{id}")
    fun getContributionsByRequestingUserAndChangedAtBetween(@RequestParam start:LocalDate,@RequestParam(required=false) end:LocalDate,@PathVariable id:Int,servletRequest:HttpServletRequest):List<ContributionDto>{
        if(!userService.doesUserExist(id)) throw IllegalStateException("user with id $id doesn't exist")
        val user=SecurityContextHolder.getContext().authentication?:
                 throw IllegalStateException("User not authenticated")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this user")

        if(start.isAfter(end)) throw IllegalStateException("start date cannot be after end date")

        try{
            return contributionService.getContributionsByRequestingUserAndChangedAtBetween(start,end,id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/by-action-and-user/{id}/{action}")
    fun getContributionsByRequestingUserAndAction(@PathVariable id:Int,@PathVariable action:Action,servletRequest:HttpServletRequest):List<ContributionDto>{
        if(!userService.doesUserExist(id)) throw IllegalStateException("user with id $id doesn't exist")
        val user=SecurityContextHolder.getContext().authentication?:
                 throw IllegalStateException("User not authenticated")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT,60)) throw IllegalStateException("Too many requests from this user")

        try{
            return contributionService.getContributionsByActionAndRequestingUser(id,action)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }
}

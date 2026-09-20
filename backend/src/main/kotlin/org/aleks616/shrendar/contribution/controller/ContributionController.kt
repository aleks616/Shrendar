package org.aleks616.shrendar.contribution.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.common.model.Table
import org.aleks616.shrendar.contribution.model.Action
import org.aleks616.shrendar.contribution.model.ContributionDto
import org.aleks616.shrendar.contribution.model.ContributionHistoryDto
import org.aleks616.shrendar.contribution.service.ContributionRevertService
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.exception.RankTooLowToConfirmContributionException
import org.aleks616.shrendar.exception.RankTooLowToRevertConfirmedContributionException
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.user.service.UserAccountService
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
    private val userAccountService:UserAccountService,
){
    @GetMapping("/")
    fun getContributions()=contributionService.getAll()

    @PostMapping("/confirm")
    fun confirmContributionRequest(@RequestParam changeId:Long,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        try{
            contributionService.confirmDataChangeRequest(changeId,userLogin)
        }
        catch(e:RankTooLowToConfirmContributionException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something_wrong. ${e.message}")
        }

        return ResponseEntity.ok("confirmation_success")
    }

    @PostMapping("/revert")
    fun revertAddRequest(@RequestParam changeId:Long,servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        try{
           contributionRevertService.revertAddition(changeId,userLogin)
        }
        catch(e:RankTooLowToRevertConfirmedContributionException){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e::class.simpleName} ${e.message}")
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("something_wrong. ${e.message}")
        }

        return ResponseEntity.ok("addition_reverted")
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleLimitExceededException(e:IllegalStateException):ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong. ${e.message}")
    }

    //region select

    @GetMapping("/requested-by/{id}")
    fun getContributionsByRequestingUser(@PathVariable id:Int):List<ContributionDto>{
        if(!userAccountService.doesUserExist(id)) throw IllegalStateException("user_not_exist")

        try{
            return contributionService.getContributionsByRequestingUser(id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/confirmed-by/{id}")
    fun getContributionsByConfirmingUser(@PathVariable id:Int):List<ContributionDto>{
        if(!userAccountService.doesUserExist(id)) throw IllegalStateException("user_not_exist")

        try{
            return contributionService.getContributionsByConfirmingUser(id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/table/{table}")
    fun getContributionsByTableName(@PathVariable table:String):List<ContributionDto>{
        try{
            Table.valueOf(table.uppercase())
        }
        catch(_:IllegalArgumentException){
            throw IllegalArgumentException("table_not_exist")
        }

        try{
            return contributionService.getContributionsByTableName(table)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/table-record/{table}")
    fun getContributionsByTableNameAndChangedRecordId(@PathVariable table:String, @RequestParam id:Int):List<ContributionDto>{
        try{
            Table.valueOf(table.uppercase())
        }
        catch(_:IllegalArgumentException){
            throw IllegalArgumentException("table_not_exist")
        }

        try{
            return contributionService.getContributionsByTableNameAndChangedRecordId(table,id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/table-record-last/{table}/{id}")
    fun getLastChangesByTableAndChangedRecordId(@PathVariable table:String, @PathVariable id:Int):ContributionHistoryDto{
        try{
            Table.valueOf(table.uppercase())
        }
        catch(_:IllegalArgumentException){
            throw IllegalArgumentException("table_not_exist")
        }

        try{
            return contributionService.getLastChangesByTableNameAndChangedRecordId(table,id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/between-dates")
    fun getContributionsByChangedAtBetween(@RequestParam start:LocalDate, @RequestParam(required=false) end:LocalDate):List<ContributionDto>{
        if(start.isAfter(end)) throw IllegalStateException("start_after_end")

        try{
            return contributionService.getContributionsByChangedAtBetween(start,end)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/between-dates-by-user/{id}")
    fun getContributionsByRequestingUserAndChangedAtBetween(@RequestParam start:LocalDate,@RequestParam(required=false) end:LocalDate,@PathVariable id:Int):List<ContributionDto>{
        if(!userAccountService.doesUserExist(id)) throw IllegalStateException("user_not_exist")

        if(start.isAfter(end)) throw IllegalStateException("start_after_end")

        try{
            return contributionService.getContributionsByRequestingUserAndChangedAtBetween(start,end,id)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    @GetMapping("/by-action-and-user/{id}/{action}")
    fun getContributionsByRequestingUserAndAction(@PathVariable id:Int,@PathVariable action:Action):List<ContributionDto>{
        try{
            return contributionService.getContributionsByActionAndRequestingUser(id,action)
        }
        catch(e:Exception){
            throw IllegalStateException("${e.message}")
        }
    }

    //endregion
}

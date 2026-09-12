package org.aleks616.shrendar.userreport.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.exception.RankTooLowException
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.user.model.ReportRequestDto
import org.aleks616.shrendar.user.model.ReportsByUserDto
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersReportDto
import org.aleks616.shrendar.user.service.UserAccountService
import org.aleks616.shrendar.userreport.service.UserReportService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/report")
class UserReportController(
    private val userAccountService:UserAccountService,
    private val rateLimiter:RateLimiter,
    private val userReportService:UserReportService,
) {

    @PostMapping("/user")
    fun reportUser(@RequestBody report:ReportRequestDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")
        if(report.reason.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Report reason not provided")

        try{
            userReportService.reportUser(report,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.status(HttpStatus.OK).body("User reported successfully")
    }

    @PostMapping("/check")
    fun canReport(@RequestBody userId:Int):Boolean{
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name
        return userReportService.canReport(userId,userLogin)
    }

    @GetMapping("/of-user/{id}")
    fun getReportsByUserId(@PathVariable id:Int):ReportsByUserDto?{
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        if(requestingUser.rank!!.id!!>9){
            return userReportService.getUserReportsByUserId(id)
        }
        else throw RankTooLowException("Rank too low")
    }

    @GetMapping("/unresolved")
    fun getUnresolvedReports():List<UsersReportDto>{
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        if(requestingUser.rank!!.id!!>9){
            return userReportService.getNotResolvedReports()
        }
        else throw RankTooLowException("Rank too low")
    }

    @PostMapping("/resolve/{id}")
    fun resolve(@PathVariable id:Long):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        if(requestingUser.rank!!.id!!>9){
            try{
                userReportService.resolveReport(id)
                return ResponseEntity.status(HttpStatus.OK).body("Resolved successfully")
            }
            catch(e:Exception){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
            }
        }
        else throw RankTooLowException("Rank too low")
    }

    @ExceptionHandler(RankTooLowException::class)
    fun handleForbiddenException(e:RankTooLowException):ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You shouldn't be here. ${e.message}")
    }

}
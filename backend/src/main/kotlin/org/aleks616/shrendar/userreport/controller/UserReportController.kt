package org.aleks616.shrendar.userreport.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.exception.RankTooLowException
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.userreport.model.ReportRequestDto
import org.aleks616.shrendar.userreport.model.ReportsByUserDto
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.userreport.model.UsersReportDto
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
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")
        if(report.reason.isNullOrEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no_report_reason")

        try{
            userReportService.reportUser(report,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.status(HttpStatus.OK).body("report_success")
    }

    @PostMapping("/check")
    fun canReport(@RequestBody userId:Int):Boolean{
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name
        return userReportService.canReport(userId,userLogin)
    }

    @GetMapping("/of-user/{id}")
    fun getReportsByUserId(@PathVariable id:Int):ReportsByUserDto?{
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        if(requestingUser.rank.id >9){
            return userReportService.getUserReportsByUserId(id)
        }
        else throw RankTooLowException("rank_too_low")
    }

    @GetMapping("/unresolved")
    fun getUnresolvedReports():List<UsersReportDto>{
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        if(requestingUser.rank.id >9){
            return userReportService.getNotResolvedReports()
        }
        else throw RankTooLowException("rank_too_low")
    }

    @PostMapping("/resolve/{id}")
    fun resolve(@PathVariable id:Long):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name
        val requestingUser:User=userAccountService.getUserByLogin(userLogin)!!
        if(requestingUser.rank.id >9){
            try{
                userReportService.resolveReport(id)
                return ResponseEntity.status(HttpStatus.OK).body("resolved_success")
            }
            catch(e:Exception){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
            }
        }
        else throw RankTooLowException("rank_too_low")
    }

    @ExceptionHandler(RankTooLowException::class)
    fun handleForbiddenException(e:RankTooLowException):ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("${e.message}")
    }

}
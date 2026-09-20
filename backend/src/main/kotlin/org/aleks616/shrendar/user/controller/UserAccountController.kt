package org.aleks616.shrendar.user.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.exception.RankTooLowException
import org.aleks616.shrendar.security.JwtUtil
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.security.TokenBlacklistService
import org.aleks616.shrendar.user.model.LoginRequestDto
import org.aleks616.shrendar.user.model.RegisterRequestDto
import org.aleks616.shrendar.user.model.ResetPasswordDto
import org.aleks616.shrendar.user.model.UsersDto
import org.aleks616.shrendar.user.service.UserAccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/user-account")
class UserAccountController(
    private val userAccountService:UserAccountService,
    private val rateLimiter:RateLimiter,
    private val tokenBlacklistService:TokenBlacklistService
) {
    @GetMapping("/loginCheck")
    fun doesLoginExist(@RequestParam login:String):ResponseEntity<Boolean> = ResponseEntity.ok(userAccountService.doesAccountExist(login))

    @GetMapping("/emailCheck")
    fun doesEmailExist(@RequestParam email:String):ResponseEntity<Boolean> = ResponseEntity.ok(userAccountService.doesAccountExist(email))
    @PostMapping("/register")
    fun register(@RequestBody request:RegisterRequestDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("reg:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        else if(!rateLimiter.allowRequest("reg:email:${request.email}",5,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_email_requests")
        else if(userAccountService.initiateRegistration(request))
            ResponseEntity.ok("verification_code_sent")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
    }

    @PostMapping("/register/confirm")
    fun confirmRegistration(
        @RequestBody request:RegisterRequestDto,
        @RequestParam code:String,
        servletRequest:HttpServletRequest
    ):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("regconfirm:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        else if(userAccountService.createUser(request,code))
            ResponseEntity.ok("account_created")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
    }

    @PostMapping("/requestPasswordReset")
    fun requestPasswordReset(@RequestParam accountKey:String):ResponseEntity<String> {
        return if(!rateLimiter.allowRequest("reset:acct:$accountKey",1,240))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")
        else if(!userAccountService.doesAccountExist(accountKey))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("account_not_found")
        else if(userAccountService.requestPasswordReset(accountKey))
            ResponseEntity.ok("Password reset code sent to email")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
    }

    @PostMapping("/resetPassword")
    fun resetPassword(@RequestBody request:ResetPasswordDto,@RequestParam code:String):ResponseEntity<String> {
        return if(!rateLimiter.allowRequest("reset:acct:${request.email}",2,240))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")
        else if(userAccountService.changePassword(request.email,request.newPassword,code))
            ResponseEntity.ok("password_changed")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
    }

    @PostMapping("/login")
    fun login(@RequestBody request:LoginRequestDto,servletRequest:HttpServletRequest):ResponseEntity<Any> {
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("login:ip:$ip",10,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        val accountKey=request.email?:request.login?:"unknown"
        if(!rateLimiter.allowRequest("login:acct:$accountKey",5,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        val subject=userAccountService.authenticate(request)?:return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("invalid_credentials")
        val token=JwtUtil.createToken(subject)
        return ResponseEntity.ok(mapOf("token" to token))
    }

    @PostMapping("/logout")
    fun logout(servletRequest:HttpServletRequest):ResponseEntity<String> {
        val header=servletRequest.getHeader("Authorization")
        if(header!=null&&header.startsWith("Bearer ")) {
            val token=header.substringAfter("Bearer ").trim()
            tokenBlacklistService.blacklistToken(token)

            SecurityContextHolder.clearContext()
            return ResponseEntity.ok("logged_out")
        }
        return ResponseEntity.badRequest().body("no_token")
    }

    @PostMapping("/updateUsername")
    fun updateUsername(@RequestParam email:String,@RequestParam newUsername:String):ResponseEntity<String> {
        return if(!userAccountService.doesAccountExist(email))
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("account_not_found")
        else if(userAccountService.doesAccountExist(newUsername))
            ResponseEntity.status(HttpStatus.CONFLICT).body("username_taken")
        else if(!userAccountService.changeUsername(email,newUsername))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("usernamed_change_limit")
        else
            ResponseEntity.ok("username_changed")
    }

    @PostMapping("/updateEmail")
    fun updateEmail(@RequestParam email:String,@RequestParam newEmail:String):ResponseEntity<String> {
        if(!userAccountService.doesAccountExist(email))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("account_not_found")
        else if(userAccountService.doesAccountExist(newEmail))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("new_email_exists")
        userAccountService.changeEmail(email,newEmail)
        return ResponseEntity.ok("email_change")
    }

    @PostMapping("/addBirthday")
    fun addBirthday(@RequestParam email:String, @RequestParam date:LocalDate): ResponseEntity<String>{
        return if(!userAccountService.doesAccountExist(email))
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("account_not_found")
        else if(ChronoUnit.YEARS.between(date,LocalDate.now())<13){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user_too_young")
        }
        else if(!userAccountService.addBirthday(email,date))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        else ResponseEntity.ok("birthday_added")
    }

    /**requires email**/
    @PostMapping("/deleteAccount")
    fun deleteAccount(@RequestBody request:LoginRequestDto):ResponseEntity<Any> {
        userAccountService.authenticate(request,false)
        userAccountService.requestDeletion(request.email!!)

        return ResponseEntity.ok("confirmed")
    }

    @PostMapping("/bio/add")
    fun addBio(@RequestBody bio:String, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something_wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_ip_requests")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("too_many_user_requests")

        try{
            userAccountService.addBio(bio,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("unexpected_error: ${e.message}")
        }

        return ResponseEntity.ok("bio_added")
    }

    @GetMapping("/users")
    fun getUsers():ResponseEntity<List<UsersDto>>{
        val userAuth=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=userAuth.name
        val user=userAccountService.getUserByLogin(userLogin)?:throw IllegalStateException("user_not_exist")
        if(user.rank.id<10) throw RankTooLowException("cant_view")

        return ResponseEntity.ok(userAccountService.getUsersDto())
    }

    @ExceptionHandler(RankTooLowException::class)
    fun handleRankTooLowException():ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("page_not_found")
    }
}
package org.aleks616.shrendar.user.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.security.JwtUtil
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.security.TokenBlacklistService
import org.aleks616.shrendar.user.model.ResetPassword
import org.aleks616.shrendar.user.model.UsersDto
import org.aleks616.shrendar.user.service.UserAccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Controller
@RequestMapping("/api/user-account")
class UserAccountController(
    private val userAccountService:UserAccountService,
    private val rateLimiter:RateLimiter,
    private val tokenBlacklistService:TokenBlacklistService
) {
    data class RegisterRequest(
        val login:String,
        val displayName:String,
        val email:String,
        val password:String
    )

    data class LoginRequest(
        val login:String?,
        val email:String?,
        val password:String
    )

    @PostMapping("/register")
    fun registerData(@RequestBody request:RegisterRequest,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("reg:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many registration attempts from this IP")
        else if(!rateLimiter.allowRequest("reg:email:${request.email}",5,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many registration attempts for this email")
        else if(userAccountService.initiateRegistration(request))
            ResponseEntity.ok("Verification code sent to email if not already registered")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cannot initiate registration: email/login may exist or rate-limited")
    }

    @PostMapping("/register/confirm")
    fun confirmRegistration(
        @RequestBody request:RegisterRequest,
        @RequestParam code:String,
        servletRequest:HttpServletRequest
    ):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("regconfirm:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many confirmation attempts from this IP")
        else if(userAccountService.createUser(request,code))
            ResponseEntity.ok("Account created and verified")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid code or registration could not be completed")
    }

    @PostMapping("/requestPasswordReset")
    fun requestPasswordReset(@RequestParam accountKey:String):ResponseEntity<String> {
        return if(!rateLimiter.allowRequest("reset:acct:$accountKey",1,240))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many reset passwords attempts")
        else if(!userAccountService.doesAccountExist(accountKey))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account not found")
        else if(userAccountService.requestPasswordReset(accountKey))
            ResponseEntity.ok("Password reset code sent to email")
        else ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Could not send password reset code, try again in 5 minutes")
    }

    @PostMapping("/resetPassword")
    fun resetPassword(@RequestBody request:ResetPassword,@RequestParam code:String):ResponseEntity<String> {
        return if(!rateLimiter.allowRequest("reset:acct:${request.email}",2,240))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests")
        else if(userAccountService.changePassword(request.email,request.newPassword,code))
            ResponseEntity.ok("Password changed successfully")
        else ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not change password, try again later")
    }

    @PostMapping("/login")
    fun login(@RequestBody request:LoginRequest,servletRequest:HttpServletRequest):ResponseEntity<Any> {
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("login:ip:$ip",10,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mapOf("error" to "Too many login attempts"))
        val accountKey=request.email?:request.login?:"unknown"
        if(!rateLimiter.allowRequest("login:acct:$accountKey",5,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mapOf("error" to "Too many login attempts"))

        val subject=userAccountService.authenticate(request)?:return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to "Invalid credentials"))
        val token=JwtUtil.createToken(subject)
        return ResponseEntity.ok(mapOf("token" to token))
    }

    @PostMapping("/logout")
    fun logout(servletRequest:HttpServletRequest):ResponseEntity<String> {
        val header=servletRequest.getHeader("Authorization")
        if(header!=null&&header.startsWith("Bearer ")) {
            val token=header.substringAfter("Bearer ").trim()
            if(token.isNotEmpty()) {
                tokenBlacklistService.blacklistToken(token)
            }
        }
        SecurityContextHolder.clearContext()
        return ResponseEntity.ok("Logged out")
    }

    @PostMapping("/updateUsername")
    fun updateUsername(@RequestParam email:String,@RequestParam newUsername:String):ResponseEntity<String> {
        return if(!userAccountService.doesAccountExist(email))
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found")
        else if(userAccountService.doesAccountExist(newUsername))
            ResponseEntity.status(HttpStatus.CONFLICT).body("New username is taken")
        else if(!userAccountService.changeUsername(email,newUsername))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username has been changed in last 90 days")
        else
            ResponseEntity.ok("Username changed")
    }

    @PostMapping("/updateEmail")
    fun updateEmail(@RequestParam email:String,@RequestParam newEmail:String):ResponseEntity<String> {
        return if(!userAccountService.doesAccountExist(email))
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found")
        else if(userAccountService.doesAccountExist(newEmail))
            ResponseEntity.status(HttpStatus.CONFLICT).body("There's already an account associated with $newEmail.")
        else if(!userAccountService.changeEmail(email,newEmail))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong. Can't change email address")
        else
            ResponseEntity.ok("Email changed")
    }

    @PostMapping("/addBirthday")
    fun addBirthday(@RequestParam email:String, @RequestParam date:LocalDate): ResponseEntity<String>{
        return if(!userAccountService.doesAccountExist(email))
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found")
        else if(ChronoUnit.YEARS.between(date,LocalDate.now())<13){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User too young")
        }
        else if(!userAccountService.addBirthday(email,date))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong. Can't add birthday")
        else ResponseEntity.ok("Birthday added")
    }

    /**requires email**/
    @PostMapping("/deleteAccount")
    fun deleteAccount(@RequestBody request:LoginRequest):ResponseEntity<Any> {
        userAccountService.authenticate(request,false)
        userAccountService.requestDeletion(request.email!!)

        return ResponseEntity.ok("Confirmed")
    }

    @PostMapping("/bio/add")
    fun addBio(@RequestBody bio:String, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_BASIC,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        try{
            userAccountService.addBio(bio,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }

        return ResponseEntity.ok("Bio added")
    }

    @GetMapping("/loginCheck")
    fun doesLoginExist(@RequestParam login:String):ResponseEntity<Boolean> = ResponseEntity.ok(userAccountService.doesAccountExist(login))

    @GetMapping("/emailCheck")
    fun doesEmailExist(@RequestParam email:String):ResponseEntity<Boolean> = ResponseEntity.ok(userAccountService.doesAccountExist(email))

    @GetMapping("/users")
    fun getUsers():ResponseEntity<List<UsersDto>> = ResponseEntity.ok(userAccountService.getUsersDto())
}
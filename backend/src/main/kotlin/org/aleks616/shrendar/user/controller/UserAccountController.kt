package org.aleks616.shrendar.user.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.security.JwtUtil
import org.aleks616.shrendar.security.RateLimiter
import org.aleks616.shrendar.security.TokenBlacklistService
import org.aleks616.shrendar.user.model.ResetPassword
import org.aleks616.shrendar.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/api/user-account")
class UserAccountController (
    private val userService:UserService,
    private val rateLimiter:RateLimiter,
    private val tokenBlacklistService:TokenBlacklistService
){
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

    @PostMapping("/register") fun registerData(@RequestBody request:RegisterRequest,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("reg:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many registration attempts from this IP")
        else if(!rateLimiter.allowRequest("reg:email:${request.email}",5,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many registration attempts for this email")
        else if(userService.initiateRegistration(request))
            ResponseEntity.ok("Verification code sent to email if not already registered")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cannot initiate registration: email/login may exist or rate-limited")
    }

    @PostMapping("/register/confirm") fun confirmRegistration(
        @RequestBody request:RegisterRequest,
        @RequestParam code:String,
        servletRequest:HttpServletRequest
    ):ResponseEntity<String> {
        val ip=servletRequest.remoteAddr?:"unknown"
        return if(!rateLimiter.allowRequest("regconfirm:ip:$ip",10,60))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many confirmation attempts from this IP")
        else if(userService.createUser(request,code))
            ResponseEntity.ok("Account created and verified")
        else
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid code or registration could not be completed")
    }

    @PostMapping("/requestPasswordReset") fun requestPasswordReset(@RequestParam accountKey:String):ResponseEntity<String> {
        return if(!rateLimiter.allowRequest("reset:acct:$accountKey",1,240))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many reset passwords attempts")
        else if(!userService.doesAccountExist(accountKey))
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account not found")
        else if(userService.requestPasswordReset(accountKey))
            ResponseEntity.ok("Password reset code sent to email")
        else ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not send password reset code, try again in 5 minutes")
    }

    @PostMapping("/resetPassword") fun resetPassword(@RequestBody request:ResetPassword,@RequestParam code:String):ResponseEntity<String> {
        return if(!rateLimiter.allowRequest("reset:acct:${request.email}",2,240))
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests")
        else if(userService.changePassword(request.email,request.newPassword,code))
            ResponseEntity.ok("Password changed successfully")
        else ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not change password, try again later")
    }

    @PostMapping("/login") fun login(@RequestBody request:LoginRequest,servletRequest:HttpServletRequest):ResponseEntity<Any> {
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("login:ip:$ip",10,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mapOf("error" to "Too many login attempts"))
        val accountKey=request.email?:request.login?:"unknown"
        if(!rateLimiter.allowRequest("login:acct:$accountKey",5,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mapOf("error" to "Too many login attempts"))

        val subject=userService.authenticate(request)?:return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to "Invalid credentials"))
        val token=JwtUtil.createToken(subject)
        return ResponseEntity.ok(mapOf("token" to token))
    }

    @PostMapping("/logout") fun logout(servletRequest:HttpServletRequest):ResponseEntity<String> {
        val header=servletRequest.getHeader("Authorization")
        if(header!=null&&header.startsWith("Bearer ")) {
            val token=header.substringAfter("Bearer ").trim()
            val expiry=JwtUtil.getExpiration(token)
            if(expiry!=null) {
                tokenBlacklistService.blacklistToken(token,expiry)
            }
        }
        return ResponseEntity.ok("Logged out")
    }


    @GetMapping("/loginCheck")
    fun doesLoginExist(@RequestParam login:String):Boolean=userService.doesAccountExist(login)

    @GetMapping("/emailCheck")
    fun doesEmailExist(@RequestParam email:String):Boolean=userService.doesAccountExist(email)

    @GetMapping("/users")
    fun getUsers()=userService.getUsersDto()
}
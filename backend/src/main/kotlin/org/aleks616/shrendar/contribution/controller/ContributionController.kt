package org.aleks616.shrendar.contribution.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.contribution.service.ContributionService
import org.aleks616.shrendar.security.RateLimiter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/api/contribution")
class ContributionController (
    private val contributionService:ContributionService,
    private val rateLimiter:RateLimiter,
){
    @GetMapping("/contributions")
    fun getContributions()=contributionService.getAll()

    @PostMapping("/confirmAddition")
    fun confirmAddRequest(@RequestParam changeId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!contributionService.confirmDataAddRequest(changeId,userLogin))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")

        return ResponseEntity.ok("Album addition confirmation successful")
    }
}

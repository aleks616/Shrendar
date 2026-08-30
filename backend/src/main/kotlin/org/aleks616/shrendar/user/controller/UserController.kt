package org.aleks616.shrendar.user.controller

import org.aleks616.shrendar.user.model.UserProfileDto
import org.aleks616.shrendar.user.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(private val userService:UserService) {

    @GetMapping("/@{login}")
    fun getUserProfile(@PathVariable login:String):UserProfileDto?{
        return try{
            userService.getUserProfile(login)
        }
        catch(_:IllegalArgumentException){
            null
        }
    }
}

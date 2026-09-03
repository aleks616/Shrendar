package org.aleks616.shrendar.homepage.controller

import org.aleks616.shrendar.homepage.model.HomePageMainDto
import org.aleks616.shrendar.homepage.service.HomePageService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/main")
class HomePageController(
    private val homePageService:HomePageService
) {

    @GetMapping("/today")
    fun getTodayAnniversaries():HomePageMainDto{
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name
        return homePageService.getTodayAnniversaries(userLogin)
    }
}
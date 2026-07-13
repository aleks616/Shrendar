package org.aleks616.shrendar.contribution.controller

import org.aleks616.shrendar.contribution.service.ContributionService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/contribution")
class ContributionController (
    private val contributionService:ContributionService,
){
    @GetMapping("/contributions")
    fun getContribution()=contributionService.getAll()
}

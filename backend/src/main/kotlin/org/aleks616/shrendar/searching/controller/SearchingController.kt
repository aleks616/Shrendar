package org.aleks616.shrendar.searching.controller

import org.aleks616.shrendar.searching.model.SearchResults
import org.aleks616.shrendar.searching.service.SearchingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/api/search")
class SearchingController(
    val searchingService:SearchingService
) {
    @GetMapping
    fun search(@RequestParam query:String):SearchResults {
        return searchingService.search(query)
    }
}

package org.aleks616.shrendar.genre.controller

import org.aleks616.shrendar.genre.service.GenreService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class GenreController(
    private val genreService:GenreService,

    ) {

    @GetMapping("/genres")
    fun getGenre()=genreService.getAll()
}
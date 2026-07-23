package org.aleks616.shrendar.genre.controller

import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.service.GenreService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/genre")
class GenreController(private val genreService:GenreService) {

    @GetMapping("/all")
    fun getGenre():List<Genre>{
        return genreService.getAll()
    }

    @GetMapping("/allAlbums/{bandId}")
    fun getBandAlbumGenresList(@PathVariable bandId:Int):List<GenreDto>{
        return genreService.getBandAlbumGenresList(bandId)
    }
}
package org.aleks616.shrendar.genre.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.security.RateLimiter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/genre")
class GenreController(
    private val genreService:GenreService,
    private val rateLimiter:RateLimiter,
) {

    @GetMapping("/all")
    fun getGenre():List<Genre>{
        return genreService.getAll()
    }

    @GetMapping("/allAlbums/{bandId}")
    fun getBandAlbumGenresList(@PathVariable bandId:Int):List<GenreDto>{
        return genreService.getBandAlbumGenresList(bandId)
    }

    @PostMapping("/favorite")
    fun favoriteGenre(@RequestBody genreId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name
        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",Utils.LIMIT_HIGH,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")
        if(!genreService.doesGenreExist(genreId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Genre with id $genreId does not exist")

        try{
            genreService.toggleFavoriteGenre(genreId,userLogin)
        }
        catch(e:Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: ${e.message}")
        }
        return ResponseEntity.ok("Genre favorite toggled successfully")
    }

}
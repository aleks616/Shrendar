package org.aleks616.shrendar.album.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.album.model.*
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.security.RateLimiter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/album")
class AlbumController (
    private val albumService:AlbumService,
    private val rateLimiter:RateLimiter
){
    @GetMapping("/")
    fun getAll():List<AlbumDataDto>{
        return albumService.getAll()
    }

    @GetMapping("/id/{id}")
    fun getAlbumById(@PathVariable id:Int):Album{
        return albumService.getById(id)
    }

    //WIKI ALBUM PAGE 1/1
    @GetMapping("wiki/{id}")
    fun getAlbumByIdWiki(@PathVariable id:Int):AlbumWikiDto{
        return albumService.getByIdWiki(id)
    }

    @GetMapping("/inDate")
    fun getAlbumAnniversariesByDate(@RequestParam month:Int,@RequestParam day:Int):List<AlbumByDateDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("Invalid date")
        return albumService.getAlbumAnniversariesByDate(month,day)
    }

    //WIKI BAND PAGE 3/4
    @GetMapping("/band/{bandId}")
    fun getAlbumsByBandId(@PathVariable bandId:Int):List<Album>{
        if(!albumService.doesBandExist(bandId)) throw IllegalArgumentException("Band doesn't exist")
        return albumService.getAlbumsByBandId(bandId)
    }

    @GetMapping("/band/like/{name}")
    fun getAlbumsByBandNameLike(@PathVariable name:String):List<Album>{
        return albumService.getAlbumsByBandName(name)
    }

    @GetMapping("/year/{year}")
    fun getAlbumsByYear(@PathVariable year:Int):List<Album>{
        if(year>LocalDate.now().year || year<1918) throw IllegalArgumentException("Invalid year")
        return albumService.getAlbumsByYear(year)
    }

    @GetMapping("/like/{name}")
    fun getAlbumsByNameLike(@PathVariable name:String):List<Album>{
        return albumService.getAlbumsByName(name)
    }

    @GetMapping("/exact/{name}")
    fun getAlbumsByNameExact(@PathVariable name:String):List<Album>{
        return albumService.getAlbumsByNameExact(name)
    }

    @PostMapping("/add")
    fun addAlbum(@RequestBody album:AlbumAddDto,servletRequest:HttpServletRequest):ResponseEntity<String> {
        val user=SecurityContextHolder.getContext().authentication?:
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!albumService.addAlbumRequest(album,userLogin))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("User reached their weekly limit")


        return ResponseEntity.ok("Album addition request received")
    }

    @PostMapping("/revertAddition")
    fun revertAlbumAddition(@RequestParam changeId:Int, servletRequest:HttpServletRequest):ResponseEntity<String>{
        val user=SecurityContextHolder.getContext().authentication?:
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")
        val userLogin=user.name

        val ip=servletRequest.remoteAddr?:"unknown"
        if(!rateLimiter.allowRequest("reg:ip:$ip",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this IP")
        if(!rateLimiter.allowRequest("login:acct:$userLogin",3,60))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests from this user")

        if(!albumService.revertAlbumAddition(changeId,userLogin))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong")

        return ResponseEntity.ok("Album addition revert successful")
    }

}
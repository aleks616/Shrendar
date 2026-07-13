package org.aleks616.shrendar.album.controller

import org.aleks616.shrendar.album.service.AlbumService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/api/album")
class AlbumController (
    private val albumService:AlbumService,
){
    @GetMapping("/albums")
    fun getAlbum()=albumService.getAll()

    @GetMapping("/albumsInDate")
    fun getAlbumAnniversariesByDate(@RequestParam month:Int,@RequestParam day:Int)=
        albumService.getAlbumAnniversariesByDate(month,day)

}
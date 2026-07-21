package org.aleks616.shrendar.album.controller

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.album.model.AlbumWikiDto
import org.aleks616.shrendar.album.service.AlbumService
import org.aleks616.shrendar.common.Utils
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/album")
class AlbumController (
    private val albumService:AlbumService,
){
    @GetMapping("/")
    fun getAlbum():List<Album>{
        return albumService.getAll()
    }

    @GetMapping("/id/{id}")
    fun getAlbumById(@PathVariable id:Int):Album{
        return albumService.getById(id)
    }

    @GetMapping("wiki/{id}")
    fun getAlbumByIdWiki(@PathVariable id:Int):AlbumWikiDto{
        return albumService.getByIdWiki(id)
    }

    @GetMapping("/inDate")
    fun getAlbumAnniversariesByDate(@RequestParam month:Int,@RequestParam day:Int):List<AlbumByDateDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("Invalid date")
        return albumService.getAlbumAnniversariesByDate(month,day)
    }

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

}
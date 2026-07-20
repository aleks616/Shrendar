package org.aleks616.shrendar.album.controller

import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.album.model.AlbumDataDto
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
    fun getAlbum():List<AlbumDataDto>{
        return albumService.getAll()
    }

    @GetMapping("/inDate")
    fun getAlbumAnniversariesByDate(@RequestParam month:Int,@RequestParam day:Int):List<AlbumByDateDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("Invalid date")
        return albumService.getAlbumAnniversariesByDate(month,day)
    }

    @GetMapping("/band/{bandId}")
    fun getAlbumsByBandId(@PathVariable bandId:Int):List<AlbumDataDto>{
        if(!albumService.doesBandExist(bandId)) throw IllegalArgumentException("Band doesn't exist")
        return albumService.getAlbumsByBandId(bandId)
    }

    @GetMapping("/band/like/{name}")
    fun getAlbumsByBandNameLike(@PathVariable name:String):List<AlbumDataDto>{
        return albumService.getAlbumsByBandName(name)
    }

    @GetMapping("/year/{year}")
    fun getAlbumsByYear(@PathVariable year:Int):List<AlbumDataDto>{
        if(year>LocalDate.now().year || year<1918) throw IllegalArgumentException("Invalid year")
        return albumService.getAlbumsByYear(year)
    }

    @GetMapping("/like/{name}")
    fun getAlbumsByNameLike(@PathVariable name:String):List<AlbumDataDto>{
        return albumService.getAlbumsByName(name)
    }

    @GetMapping("/exact/{name}")
    fun getAlbumsByNameExact(@PathVariable name:String):List<AlbumDataDto>{
        return albumService.getAlbumsByNameExact(name)
    }

}
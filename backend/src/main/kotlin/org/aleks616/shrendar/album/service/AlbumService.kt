package org.aleks616.shrendar.album.service

import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.album.model.Albums
import org.aleks616.shrendar.album.model.AlbumsByDateDto
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.springframework.stereotype.Service
import java.util.Calendar

@Service
class AlbumService(private val repository:AlbumRepository){
    fun getAll():List<Albums> =repository.findAll()

    fun getAlbumAnniversariesByDate(month:Int,day:Int):List<AlbumsByDateDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("month and day aren't valid")
        val albumsInDate=getAll().filter{it.releaseDate?.monthValue==month && it.releaseDate?.dayOfMonth==day}

        val year=Calendar.getInstance().get(Calendar.YEAR)

        return albumsInDate.map{a->
            AlbumsByDateDto(
                id=a.id,
                bands=a.bands?.let {AlbumsByDateDto.BandsDto(it.id,it.name)},
                title=a.title,
                releaseDate=a.releaseDate,
                type=a.type,
                importance=a.importance,
                yearsSince=year-(a.releaseDate?.year!!)
            )

        }

    }
}
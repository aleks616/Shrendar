package org.aleks616.shrendar.album.service

import org.aleks616.shrendar.album.model.*
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.common.Utils
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class AlbumService(private val albumRepository:AlbumRepository){
    fun doesBandExist(bandId:Int):Boolean{
        return albumRepository.existsById(bandId)
    }
    //endregion

    fun getAll():List<AlbumDataDto>{
        return albumRepository.findAll().map { AlbumDataDto(
            id=it.id,
            band=BandDto(id=it.band?.id,name=it.band?.name),
            title=it.title,
            releaseDate=it.releaseDate,
            type=it.type,
            importance=it.importance,
            genre=it.genre,
            artworkUrl=it.artworkUrl,
        ) }
    }

    fun getById(id:Int):Album{
        return albumRepository.findAlbumById(id)
    }

    fun getByIdWiki(id:Int):AlbumWikiDto {
        val dataRaw=getById(id)
        val band=BandDto(dataRaw.band?.id,dataRaw.band?.name)
        val age=dataRaw.releaseDate!!.until(LocalDate.now()).years
        val daysTillAnniversary=Utils.getDaysTillNextAnniversary(dataRaw.releaseDate!!)

        return AlbumWikiDto(
            id=dataRaw.id,
            albumName=dataRaw.title,
            band=band,
            releaseDate=dataRaw.releaseDate,
            albumAge=age,
            daysTillAnniversary=daysTillAnniversary,
            type=dataRaw.type,
            genre=dataRaw.genre,
            description=dataRaw.description,
            artworkUrl=dataRaw.artworkUrl,
            importance=dataRaw.importance,
        )
    }

    fun getAlbumsByBandId(bandId:Int):List<AlbumDataDto>{
        val albums=albumRepository.findByBandId(bandId)
        return albums
    }

    fun getAlbumsByBandName(name:String):List<AlbumDataDto>{
        return albumRepository.findByBandNameContainingIgnoreCase((name))
    }

    fun getAlbumsByYear(year:Int):List<AlbumDataDto>{
        return albumRepository.findByYear(year)
    }

    fun getAlbumsByName(name:String):List<AlbumDataDto>{
        return albumRepository.findByTitleContainingIgnoreCase((name))
    }

    fun getAlbumsByNameExact(name:String):List<AlbumDataDto>{
        return albumRepository.findByTitleIgnoreCase((name))
    }

    fun getAlbumAnniversariesByDate(month:Int,day:Int):List<AlbumByDateDto>{
        val albumsInDate=getAll().filter{it.releaseDate?.monthValue==month&&it.releaseDate.dayOfMonth==day}
        val year=Calendar.getInstance().get(Calendar.YEAR)

        return albumsInDate.map{a->
            AlbumByDateDto(
                id=a.id,
                band=a.band?.let {BandDto(it.id,it.name)},
                title=a.title,
                releaseDate=a.releaseDate,
                type=a.type,
                importance=a.importance,
                yearsSince=year-(a.releaseDate?.year!!),
                genre=a.genre
            )

        }

    }
}
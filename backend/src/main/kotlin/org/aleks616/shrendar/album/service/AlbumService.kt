package org.aleks616.shrendar.album.service

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.album.model.AlbumWikiDto
import org.aleks616.shrendar.album.model.BandDto
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.common.Utils
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class AlbumService(private val albumRepository:AlbumRepository){
    //region utils
   /* fun getAlbumData(albums:List<Album>):List<Album>{
        return albums.map { a->
            AlbumDataDto(
                id=a.id,
                band=a.band?.let {BandDto(it.id,it.name)},
                title=a.title,
                releaseDate=a.releaseDate,
                type=a.type,
                importance=a.importance,
                genre=a.genre,
                artworkUrl=a.artworkUrl
            )
        }
    }*/

    fun doesBandExist(bandId:Int):Boolean{
        return albumRepository.existsById(bandId)
    }
    //endregion

    fun getAll():List<Album>{
        return albumRepository.findAll()
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

    fun getAlbumsByBandId(bandId:Int):List<Album>{
        val albums=albumRepository.findByBandId(bandId)
        return albums
    }

    fun getAlbumsByBandName(name:String):List<Album>{
        return albumRepository.findByBandNameContainingIgnoreCase((name))
    }

    fun getAlbumsByYear(year:Int):List<Album>{
        return albumRepository.findByYear(year)
    }

    fun getAlbumsByName(name:String):List<Album>{
        return albumRepository.findByTitleContainingIgnoreCase((name))
    }

    fun getAlbumsByNameExact(name:String):List<Album>{
        return albumRepository.findByTitleIgnoreCase((name))
    }

    fun getAlbumAnniversariesByDate(month:Int,day:Int):List<AlbumByDateDto>{
        val albumsInDate=getAll().filter{it.releaseDate?.monthValue==month&&it.releaseDate!!.dayOfMonth==day}
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
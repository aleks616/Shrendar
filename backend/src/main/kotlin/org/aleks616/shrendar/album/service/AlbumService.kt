package org.aleks616.shrendar.album.service

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.album.model.AlbumDataDto
import org.aleks616.shrendar.album.model.BandDto
import org.aleks616.shrendar.album.repository.AlbumRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class AlbumService(private val albumRepository:AlbumRepository){
    //region utils
    fun getAlbumData(albums:List<Album>):List<AlbumDataDto>{
        return albums.map { a->
            AlbumDataDto(
                id=a.id,
                band=a.band?.let {BandDto(it.id,it.name)},
                title=a.title,
                releaseDate=a.releaseDate,
                type=a.type,
                importance=a.importance,
                genre=getAlbumGenre(a.id),
                artworkUrl=a.artworkUrl
            )
        }
    }

    fun doesBandExist(bandId:Int):Boolean{
        return albumRepository.existsById(bandId)
    }

    fun getAlbumGenre(albumId:Int?):String{
        return albumRepository.getAlbumGenre(albumId)?: ""
    }
    //endregion

    fun getAll():List<AlbumDataDto>{
        val albums=albumRepository.findAll()
        return getAlbumData(albums)
    }

    fun getAlbumsByBandId(bandId:Int):List<AlbumDataDto>{
        val albums=albumRepository.findByBandId(bandId)
        return getAlbumData(albums)
    }

    fun getAlbumsByBandName(name:String):List<AlbumDataDto>{
        val albums=albumRepository.findByBandNameContainingIgnoreCase((name))
        return getAlbumData(albums)
    }

    fun getAlbumsByYear(year:Int):List<AlbumDataDto>{
        val albums=albumRepository.findByYear(year)
        return getAlbumData(albums)
    }

    fun getAlbumsByName(name:String):List<AlbumDataDto>{
        val albums=albumRepository.findByTitleContainingIgnoreCase((name))
        return getAlbumData(albums)
    }

    fun getAlbumsByNameExact(name:String):List<AlbumDataDto>{
        val albums=albumRepository.findByTitleIgnoreCase((name))
        return getAlbumData(albums)
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
                genre=getAlbumGenre(a.id)
            )

        }

    }
}
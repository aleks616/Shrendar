package org.aleks616.shrendar.genre.service

import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.springframework.stereotype.Service

@Service
class GenreService(
    private val genreRepository:GenreRepository
){
    fun getAll():List<Genre>{
        return genreRepository.findAll()
    }

    fun getBandAlbumGenresList(id:Int):List<GenreDto>{
        val data= genreRepository.findBandAlbumGenresList(id)
        return data.map { d->
            GenreDto(
                id=d.id,
                name=d.name,
                value=d.value?.toInt()
            )
        }
    }

}
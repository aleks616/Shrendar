package org.aleks616.shrendar.genre.service

import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.model.Genres
import org.springframework.stereotype.Service

@Service
class GenreService(private val repository:GenreRepository){
    fun getAll():List<Genres> =repository.findAll()
}
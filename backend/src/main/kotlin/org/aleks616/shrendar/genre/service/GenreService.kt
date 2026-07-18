package org.aleks616.shrendar.genre.service

import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.model.Genre
import org.springframework.stereotype.Service

@Service
class GenreService(private val repository:GenreRepository){
    fun getAll():List<Genre> =repository.findAll()
}
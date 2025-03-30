package org.aleks616.shrendar.services

import org.aleks616.shrendar.entities.Genres
import org.aleks616.shrendar.repositories.GenreRepository
import org.springframework.stereotype.Service

@Service
class GenreService(private val repository:GenreRepository){
    fun getAll():List<Genres> =repository.findAll()
}
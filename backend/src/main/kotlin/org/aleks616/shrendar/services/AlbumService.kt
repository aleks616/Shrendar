package org.aleks616.shrendar.services

import org.aleks616.shrendar.entities.Albums
import org.aleks616.shrendar.repositories.AlbumRepository
import org.springframework.stereotype.Service

@Service
class AlbumService(private val repository:AlbumRepository){
    fun getAll():List<Albums> =repository.findAll()
}
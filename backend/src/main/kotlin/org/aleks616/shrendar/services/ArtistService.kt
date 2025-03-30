package org.aleks616.shrendar.services

import org.aleks616.shrendar.entities.Artists
import org.aleks616.shrendar.repositories.ArtistRepository
import org.springframework.stereotype.Service

@Service
class ArtistService(private val repository:ArtistRepository){
    fun getAll():List<Artists> =repository.findAll()
}
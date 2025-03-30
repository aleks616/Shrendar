package org.aleks616.shrendar.services

import org.aleks616.shrendar.entities.Bands
import org.aleks616.shrendar.repositories.BandRepository
import org.springframework.stereotype.Service

@Service
class BandService(private val repository:BandRepository){
    fun getAll():List<Bands> =repository.findAll()
}
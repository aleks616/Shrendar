package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.repository.BandRepository
import org.springframework.stereotype.Service

@Service
class BandService(private val repository:BandRepository){
    fun getAll():List<Band> =repository.findAll()
}
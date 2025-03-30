package org.aleks616.shrendar.services

import org.aleks616.shrendar.entities.Ranks
import org.aleks616.shrendar.repositories.RankRepository
import org.springframework.stereotype.Service

@Service
class RankService(private val repository:RankRepository){
    fun getAll():List<Ranks> =repository.findAll()
}
package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.user.model.Ranks
import org.aleks616.shrendar.user.repository.RankRepository
import org.springframework.stereotype.Service

@Service
class RankService(private val repository:RankRepository){
    fun getAll():List<Ranks> =repository.findAll()
}
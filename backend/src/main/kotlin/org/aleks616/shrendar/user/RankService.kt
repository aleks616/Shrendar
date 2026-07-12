package org.aleks616.shrendar.user

import org.aleks616.shrendar.repositories.RankRepository
import org.aleks616.shrendar.user.model.Ranks
import org.springframework.stereotype.Service

@Service
class RankService(private val repository:RankRepository){
    fun getAll():List<Ranks> =repository.findAll()
}
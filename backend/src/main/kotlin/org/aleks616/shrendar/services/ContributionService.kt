package org.aleks616.shrendar.services

import org.aleks616.shrendar.entities.Contributions
import org.aleks616.shrendar.repositories.ContributionRepository
import org.springframework.stereotype.Service

@Service
class ContributionService(private val repository:ContributionRepository){
    fun getAll():List<Contributions> =repository.findAll()
}
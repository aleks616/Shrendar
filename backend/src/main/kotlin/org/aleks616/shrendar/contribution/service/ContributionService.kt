package org.aleks616.shrendar.contribution.service

import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.springframework.stereotype.Service

@Service
class ContributionService(private val repository:ContributionRepository){
    fun getAll():List<Contribution> =repository.findAll()
}
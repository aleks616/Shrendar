package org.aleks616.shrendar.contribution.repository

import org.aleks616.shrendar.contribution.model.Contribution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContributionRepository :JpaRepository<Contribution,Int>
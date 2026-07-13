package org.aleks616.shrendar.contribution.repository

import org.aleks616.shrendar.contribution.model.Contributions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContributionRepository :JpaRepository<Contributions,Int>
package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.Rank
import org.springframework.data.jpa.repository.JpaRepository

interface
RankRepository:JpaRepository<Rank,Int>
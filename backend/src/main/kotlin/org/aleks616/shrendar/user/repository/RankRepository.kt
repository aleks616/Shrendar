package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.Ranks
import org.springframework.data.jpa.repository.JpaRepository

interface
RankRepository:JpaRepository<Ranks,Int>
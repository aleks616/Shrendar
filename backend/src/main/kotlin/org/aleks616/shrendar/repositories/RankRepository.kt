package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Ranks
import org.springframework.data.jpa.repository.JpaRepository

interface RankRepository:JpaRepository<Ranks,Int>
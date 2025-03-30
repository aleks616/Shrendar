package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.UserGenre
import org.springframework.data.jpa.repository.JpaRepository

interface UserGenreRepository:JpaRepository<UserGenre,Long> {
}
package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.user.model.UserGenre
import org.springframework.data.jpa.repository.JpaRepository

interface UserGenreRepository:JpaRepository<UserGenre,Long> {
}
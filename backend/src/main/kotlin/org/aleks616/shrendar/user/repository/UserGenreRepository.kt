package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.UserGenre
import org.springframework.data.jpa.repository.JpaRepository

interface UserGenreRepository:JpaRepository<UserGenre,Long> {
}
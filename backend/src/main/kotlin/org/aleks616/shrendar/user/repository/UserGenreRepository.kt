package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersGenres
import org.springframework.data.jpa.repository.JpaRepository

interface UserGenreRepository:JpaRepository<UsersGenres,Long> {
    fun findByGenreAndUser(genre:Genre,user:User):UsersGenres?
}
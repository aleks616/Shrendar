package org.aleks616.shrendar.genre.repository

import org.aleks616.shrendar.genre.model.Genre
import org.springframework.data.jpa.repository.JpaRepository

interface GenreRepository:JpaRepository<Genre,Int>
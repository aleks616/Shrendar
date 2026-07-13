package org.aleks616.shrendar.genre.repository

import org.aleks616.shrendar.genre.model.Genres
import org.springframework.data.jpa.repository.JpaRepository

interface GenreRepository:JpaRepository<Genres,Int>
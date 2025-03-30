package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Genres
import org.springframework.data.jpa.repository.JpaRepository

interface GenreRepository:JpaRepository<Genres,Int>
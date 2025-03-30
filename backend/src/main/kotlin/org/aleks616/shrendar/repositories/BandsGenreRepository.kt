package org.aleks616.shrendar.repositories
import org.aleks616.shrendar.entities.BandsGenres
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BandsGenreRepository : JpaRepository<BandsGenres, Int>
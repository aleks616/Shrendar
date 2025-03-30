package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Albums
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AlbumRepository : JpaRepository<Albums, Int>
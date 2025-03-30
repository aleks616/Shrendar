package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Artists
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArtistRepository : JpaRepository<Artists, Int>
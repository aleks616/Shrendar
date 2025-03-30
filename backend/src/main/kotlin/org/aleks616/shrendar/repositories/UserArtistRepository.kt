package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.UserArtist
import org.springframework.data.jpa.repository.JpaRepository

interface UserArtistRepository:JpaRepository<UserArtist,Long> {
}
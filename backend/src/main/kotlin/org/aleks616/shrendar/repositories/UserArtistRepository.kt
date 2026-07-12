package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.user.model.UserArtist
import org.springframework.data.jpa.repository.JpaRepository

interface UserArtistRepository:JpaRepository<UserArtist,Long> {
}
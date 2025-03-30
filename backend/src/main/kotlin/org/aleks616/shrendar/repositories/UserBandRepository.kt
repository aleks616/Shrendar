package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.UserBand
import org.springframework.data.jpa.repository.JpaRepository

interface UserBandRepository:JpaRepository<UserBand,Long> {
}
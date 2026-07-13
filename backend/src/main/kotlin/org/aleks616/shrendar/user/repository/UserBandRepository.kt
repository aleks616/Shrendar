package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.UserBand
import org.springframework.data.jpa.repository.JpaRepository

interface UserBandRepository:JpaRepository<UserBand,Long> {
}
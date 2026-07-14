package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.UsersBands
import org.springframework.data.jpa.repository.JpaRepository

interface UserBandRepository:JpaRepository<UsersBands,Long> {
}
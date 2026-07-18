package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.UserLog
import org.springframework.data.jpa.repository.JpaRepository

interface UserLogRepository:JpaRepository<UserLog,Int> {
}
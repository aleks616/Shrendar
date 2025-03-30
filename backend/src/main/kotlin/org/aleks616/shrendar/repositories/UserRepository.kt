package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Users
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<Users, Int> {
}
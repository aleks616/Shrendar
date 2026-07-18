package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.UserPasswordHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserPasswordHistoryRepository:JpaRepository<UserPasswordHistory,Long> {

    @Query("SELECT u FROM UserPasswordHistory u WHERE u.user.id = :userId")
    fun findAllByUserId(userId:Int):List<UserPasswordHistory>
}
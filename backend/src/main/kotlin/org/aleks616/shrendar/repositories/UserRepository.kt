package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<Users, Int>{
    @Query("SELECT u FROM Users u WHERE u.email=:email")
    fun findByEmail(email:String):Users

    @Query("SELECT u FROM Users u WHERE u.login=:login")
    fun findByLogin(login:String):Users
}
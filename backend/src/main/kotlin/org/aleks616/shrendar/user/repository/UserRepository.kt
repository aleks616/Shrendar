package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserRepository:JpaRepository<User,Int> {
    @Query("SELECT u FROM User u WHERE u.email=:email")
    fun findByEmail(email:String):User?

    @Query("SELECT u FROM User u WHERE u.login=:login")
    fun findByLogin(login:String):User?
    @Modifying
    @Query("DELETE FROM User u WHERE u.id=:id")
    fun deleteUserById(id:Int)
}
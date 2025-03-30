package org.aleks616.shrendar.services

import org.aleks616.shrendar.entities.Users
import org.aleks616.shrendar.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val repository:UserRepository){
    fun getAll():List<Users> =repository.findAll()
}
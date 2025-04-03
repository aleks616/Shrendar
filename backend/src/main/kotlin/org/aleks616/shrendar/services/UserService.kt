package org.aleks616.shrendar.services

import org.aleks616.shrendar.controllers.AllControllers.RegisterRequest
import org.aleks616.shrendar.dto.UsersDto
import org.aleks616.shrendar.entities.Users
import org.aleks616.shrendar.repositories.RankRepository
import org.aleks616.shrendar.repositories.UserRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val repository:UserRepository,
    private val rankRepository:RankRepository
){
    //todo: created at to int
    fun getUsers():List<Users> =repository.findAll()

    fun getUsersDto():List<UsersDto>{
        return getUsers().map{u ->
            UsersDto(
                id=u.id,
                login=u.login,
                username=u.username,
                passwordHash=u.passwordHash,
                email=u.email,
                createdAt=u.createdAt?.toEpochMilli(),
                birthDate=u.birthDate?.toString(),
                ranks=u.ranks?.let{UsersDto.RanksDto(it.id,it.name)},
                xp=u.xp
            )
        }
    }

    fun getAll():List<Users> =repository.findAll()

    fun createUser(req:RegisterRequest){
        val encryptedPassword=BCryptPasswordEncoder().encode(req.password)
        repository.save(Users().apply {
            login=req.login
            username=req.displayName
            passwordHash=encryptedPassword
            email=req.email
            createdAt=Instant.now()
            ranks=rankRepository.findById(1).orElseThrow()
            xp=0
        })
    }
}
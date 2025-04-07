package org.aleks616.shrendar.services

import org.aleks616.shrendar.controllers.AllControllers
import org.aleks616.shrendar.controllers.AllControllers.RegisterRequest
import org.aleks616.shrendar.dto.UsersDto
import org.aleks616.shrendar.entities.Users
import org.aleks616.shrendar.repositories.RankRepository
import org.aleks616.shrendar.repositories.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val repository:UserRepository,
    private val rankRepository:RankRepository
){
    fun matches(raw:String,encrypted:String):Boolean{
        val encoder=BCryptPasswordEncoder()
        return encoder.matches(raw,encrypted)
    }

    val encoder=BCryptPasswordEncoder()
    fun getUsers():List<Users> =repository.findAll()

    fun doesLoginExist(login:String):Boolean{
        return getUsers().any{it.login.equals(login,ignoreCase=true)}
    }
    fun doesAccountWithEmailExist(email:String):Boolean{
        return getUsers().any{it.email.equals(email,ignoreCase=true)}
    }

    fun isPasswordCorrect(req:AllControllers.LoginRequest):Boolean{
        if(req.email==null&&req.login==null) return false
        val user=if(req.email!=null) repository.findByEmail(req.email) else repository.findByLogin(req.login!!)

        println( matches(req.password, user.passwordHash?:""))
        return matches(req.password, user.passwordHash?:"")
    }

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
                xp=u.xp,
                verified=u.verified
            )
        }
    }

    fun getAll():List<Users> =repository.findAll()

    fun createUser(req:RegisterRequest){
        println(req.password)
        println(req.password.map{it.code})
        val encryptedPassword=encoder.encode(req.password)
        repository.save(Users().apply {
            login=req.login
            username=req.displayName
            passwordHash=encryptedPassword
            email=req.email
            createdAt=Instant.now()
            ranks=rankRepository.findById(1).orElseThrow()
            xp=0
            verified=false
        })
    }
}
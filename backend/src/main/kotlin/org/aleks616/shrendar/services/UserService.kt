package org.aleks616.shrendar.services

import org.aleks616.shrendar.controllers.AllControllers.RegisterRequest
import org.aleks616.shrendar.dto.UsersDto
import org.aleks616.shrendar.entities.Users
import org.aleks616.shrendar.repositories.RankRepository
import org.aleks616.shrendar.repositories.UserRepository
import org.aleks616.shrendar.VerificationCodeGenerator
import org.aleks616.shrendar.VerificationCodeStorage
import org.aleks616.shrendar.controllers.AllControllers
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val repository:UserRepository,
    private val rankRepository:RankRepository,
    private val verificationCodeStorage:VerificationCodeStorage,
    private val emailService:SendEmailService,
    private val verificationCodeGenerator:VerificationCodeGenerator,
    private val encoder:BCryptPasswordEncoder
) {
    fun matches(raw:String,encrypted:String):Boolean {
        return encoder.matches(raw,encrypted)
    }

    fun getUsers():List<Users> =repository.findAll()

    fun doesLoginExist(login:String):Boolean {
        return getUsers().any {it.login.equals(login,ignoreCase=true)}
    }

    fun doesAccountWithEmailExist(email:String):Boolean {
        return getUsers().any {it.email.equals(email,ignoreCase=true)}
    }

    fun authenticate(req:AllControllers.LoginRequest):String? {
        val user=if(req.email!=null) repository.findByEmail(req.email)
        else if(req.login!=null) repository.findByLogin(req.login)
        else null
        if(user==null) return null

        return if(matches(req.password,user.passwordHash?:"")) user.login else null
    }

    fun getUsersDto():List<UsersDto> {
        return getUsers().map {u->
            UsersDto(
                id=u.id,
                login=u.login,
                username=u.username,
                passwordHash=u.passwordHash,
                email=u.email,
                createdAt=u.createdAt?.toEpochMilli(),
                birthDate=u.birthDate?.toString(),
                ranks=u.ranks?.let {UsersDto.RanksDto(it.id,it.name)},
                xp=u.xp,
                verified=u.verified
            )
        }
    }

    fun initiateRegistration(req:RegisterRequest):Boolean {
        if(doesLoginExist(req.login)||doesAccountWithEmailExist(req.email)) return false
        if(!verificationCodeStorage.canSendCode(req.email)) return false
        val code=verificationCodeGenerator.generateVerificationCode()
        verificationCodeStorage.storeCode(req.email,code)
        emailService.sendVerificationCode(req.email,code)
        return true
    }

    fun createUser(req:RegisterRequest,code:String):Boolean {
        if(!verificationCodeStorage.validateCode(req.email,code)) return false
        val encryptedPassword=encoder.encode(req.password)
        repository.save(Users().apply {
            login=req.login
            username=req.displayName
            passwordHash=encryptedPassword
            email=req.email
            createdAt=Instant.now()
            ranks=rankRepository.findById(1).orElseThrow()
            xp=0
            verified=true
        })
        return true
    }
}
package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.mail.service.EmailService
import org.aleks616.shrendar.securityCode.CodeGenerator
import org.aleks616.shrendar.securityCode.CodeStorage
import org.aleks616.shrendar.user.controller.UserAccountController
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UserLog
import org.aleks616.shrendar.user.model.UsersDto
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserLogRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UserService(
    private val userRepository:UserRepository,
    private val userLogRepository:UserLogRepository,
    private val rankRepository:RankRepository,
    @Qualifier("registrationCodeStorage") private val registrationCodeStorage:CodeStorage,
    @Qualifier("passwordResetCodeStorage") private val passwordResetCodeStorage:CodeStorage,
    private val emailService:EmailService,
    private val encoder:BCryptPasswordEncoder
) {
    fun matches(raw:String,encrypted:String):Boolean {
        return encoder.matches(raw,encrypted)
    }

    fun getUsers():List<User> =userRepository.findAll()

    fun doesAccountExist(accountKey:String):Boolean {
        return getUsers().any {it.login.equals(accountKey,ignoreCase=true)||it.email.equals(accountKey,ignoreCase=true)}
    }

    fun authenticate(req:UserAccountController.LoginRequest):String? {
        val user=if(req.email!=""&&req.email!=null) userRepository.findByEmail(req.email)
        else if(req.login!=""&&req.login!=null) userRepository.findByLogin(req.login)
        else null
        if(user==null) return null
        val userLog=userLogRepository.findById(user.id!!).orElseThrow {IllegalStateException("UserLog not found for user id ${user.id}")}
        userLog.lastLoginTime=Instant.now()
        userLogRepository.save(userLog)

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
                //createdAt=u.createdAt?.toEpochMilli(),
                birthDate=u.birthDate?.toString(),
                ranks=u.rank?.let {UsersDto.RanksDto(it.id,it.name)},
                xp=u.xp,
                verified=u.verified
            )
        }
    }

    fun initiateRegistration(req:UserAccountController.RegisterRequest):Boolean {
        if(doesAccountExist(req.login)||doesAccountExist(req.email)) return false
        if(!registrationCodeStorage.canSendCode(req.email)) return false
        val code=CodeGenerator.generateCode()
        registrationCodeStorage.storeCode(req.email,code)
        emailService.sendVerificationCode(req.email,code)
        return true
    }

    fun createUser(req:UserAccountController.RegisterRequest,code:String):Boolean {
        if(!registrationCodeStorage.validateCode(req.email,code)) return false
        val encryptedPassword=encoder.encode(req.password)
        userRepository.save(User().apply {
            login=req.login
            username=req.displayName
            passwordHash=encryptedPassword
            email=req.email
            rank=rankRepository.findById(1).orElseThrow()
            xp=0
            verified=true
        })
        userLogRepository.save(UserLog().apply {
            user=userRepository.findByLogin(req.login)!!
            accountCreatedTime=Instant.now()
            passwordChangedTime=Instant.now()
        })

        emailService.sendAccountCreatedMessage(req.email)
        return true
    }

    fun requestPasswordReset(accountKey:String):Boolean {
        if(!doesAccountExist(accountKey)) return false
        if(!passwordResetCodeStorage.canSendCode(accountKey)) return false
        val code=CodeGenerator.generateCode(numericOnly=true)
        passwordResetCodeStorage.storeCode(accountKey,code)
        val email=if(doesAccountExist(accountKey)) accountKey
        else userRepository.findByLogin(accountKey)?.email?:return false

        emailService.sendPasswordResetMessage(email,code)
        return true
    }

    fun changePassword(email:String,password:String,resetCode:String):Boolean {
        if(!passwordResetCodeStorage.validateCode(email,resetCode)) return false
        val encryptedPassword=encoder.encode(password)
        val userToChange=userRepository.findAll().firstOrNull {it.email.equals(email,ignoreCase=true)}?:return false
        userToChange.passwordHash=encryptedPassword
        userRepository.save(userToChange)
        val userLog=userLogRepository.findById(userToChange.id!!).orElseThrow {IllegalStateException("UserLog not found for user id ${userToChange.id}")}
        userLog.passwordChangedTime=Instant.now()
        userLogRepository.save(userLog)

        emailService.sendPasswordHasBeenChangedMessage(email)
        return true
    }

    fun changeUsername(email:String, newUsername:String):Boolean{
        val user=userRepository.findByEmail(email)?:return false
        user.username=newUsername
        userRepository.save(user)
        val userLog=userLogRepository.findById(user.id!!).orElseThrow {IllegalStateException("UserLog not found for user id ${user.id}")}
        if(userLog.displayNameChangedTime!=null){
            if(ChronoUnit.DAYS.between(userLog.displayNameChangedTime,Instant.now())<90)
                return false
        }
        userLog.displayNameChangedTime=Instant.now()
        userLogRepository.save(userLog)

        return true
    }
}
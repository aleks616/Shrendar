package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.mail.service.EmailService
import org.aleks616.shrendar.securityCode.CodeGenerator
import org.aleks616.shrendar.securityCode.CodeStorage
import org.aleks616.shrendar.user.controller.UserAccountController
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UserLog
import org.aleks616.shrendar.user.model.UserPasswordHistory
import org.aleks616.shrendar.user.model.UsersDto
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserLogRepository
import org.aleks616.shrendar.user.repository.UserPasswordHistoryRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class UserService(
    private val userRepository:UserRepository,
    private val userLogRepository:UserLogRepository,
    private val rankRepository:RankRepository,
    private val userPasswordHistoryRepository:UserPasswordHistoryRepository,
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

    fun authenticate(req:UserAccountController.LoginRequest,log:Boolean=true):String? {
        val user=if(req.email!=""&&req.email!=null) userRepository.findByEmail(req.email)
        else if(req.login!=""&&req.login!=null) userRepository.findByLogin(req.login)
        else null
        if(user==null) return null
        val userLog=findUserLog(user.id!!)

        if(userLog.accountDeletionScheduledTime!=null){
            userLog.accountDeletionScheduledTime=null
            userLogRepository.save(userLog)
            emailService.sendAccountDeletionCancelledMessage(user.email!!)
        }
        if(log){
            userLog.lastLoginTime=Instant.now()
            userLogRepository.save(userLog)
        }

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
        val email=if(accountKey.contains("@")) accountKey
        else userRepository.findByLogin(accountKey)?.email?:return false

        emailService.sendPasswordResetMessage(email,code)
        return true
    }

    fun changePassword(email:String,newpassword:String,resetCode:String):Boolean {
        if(!passwordResetCodeStorage.validateCode(email,resetCode)) return false
        val encryptedPassword=encoder.encode(newpassword)
        val userToChange=userRepository.findAll().firstOrNull {it.email.equals(email,ignoreCase=true)}?:return false
        val userPasswordHistory=UserPasswordHistory()
        val passwordHistory=userPasswordHistoryRepository.findAllByUserId(userToChange.id!!)
        passwordHistory.forEach {
            if(it.password==encryptedPassword) return false
        }
        userPasswordHistoryRepository.save(userPasswordHistory).apply {
            user=userToChange
            password=encryptedPassword
        }
        deleteOldPasswordHistory(userToChange.id!!)

        userToChange.passwordHash=encryptedPassword
        userRepository.save(userToChange)
        val userLog=findUserLog(userToChange.id!!)
        userLog.passwordChangedTime=Instant.now()
        userLogRepository.save(userLog)

        emailService.sendPasswordHasBeenChangedMessage(email)
        return true
    }

    fun changeUsername(email:String, newUsername:String):Boolean{
        val user=userRepository.findByEmail(email)?:return false
        user.username=newUsername
        userRepository.save(user)
        val userLog=findUserLog(user.id!!)
        if(userLog.displayNameChangedTime!=null){
            if(ChronoUnit.DAYS.between(userLog.displayNameChangedTime,Instant.now())<90)
                return false
        }
        userLog.displayNameChangedTime=Instant.now()
        userLogRepository.save(userLog)
        return true
    }

    fun changeEmail(email:String, newEmail:String):Boolean{
        val user=userRepository.findByEmail(email)?:return false
        user.email=newEmail
        userRepository.save(user)
        return true
    }

    fun addBirthday(email:String, date:LocalDate):Boolean{
        val user=userRepository.findByEmail(email)?:return false
        user.birthDate=date
        val userLog=findUserLog(user.id!!)
        if(userLog.birthdayChangedTime!=null){
            if(ChronoUnit.DAYS.between(userLog.birthdayChangedTime,Instant.now())<180)
                return false
        }
        userLog.birthdayChangedTime=Instant.now()
        userRepository.save(user)
        userLogRepository.save(userLog)
        return true
    }

    fun findUserLog(userId:Int):UserLog{
        return userLogRepository.findById(userId).orElseThrow {IllegalStateException("UserLog not found for user id $userId")}
    }

    fun requestDeletion(userEmail:String):Boolean{
        val user=userRepository.findByEmail(userEmail)?:return false
        val userLog=findUserLog(user.id!!)
        userLog.accountDeletionScheduledTime=Instant.now()
        userLogRepository.save(userLog)
        emailService.sendAccountScheduledForDeletionMessage(user.email!!)
        return true
    }

    @Scheduled(fixedRate=24*60*60*1000)
    @Transactional
    fun checkAccountScheduledToBeDeleted(){
        val users=userRepository.findAll()
        users.forEach{user->
            val userLog=findUserLog(user.id!!)
            if(userLog.accountDeletionScheduledTime!=null){
                if(ChronoUnit.DAYS.between(userLog.accountDeletionScheduledTime,Instant.now())>=21){
                    userLogRepository.deleteById(userLog.id)
                    userLogRepository.flush()
                    userRepository.deleteUserById(user.id!!)
                    emailService.sendAccountDeletedMessage(user.email!!)
                }
            }
        }
    }
    fun deleteOldPasswordHistory(userId:Int) {
        val history=userPasswordHistoryRepository.findAllByUserId(userId)
        if(history.size>10) {
            val toDelete=history.sortedBy {it.id}.first()
            userPasswordHistoryRepository.delete(toDelete)
        }
    }

}
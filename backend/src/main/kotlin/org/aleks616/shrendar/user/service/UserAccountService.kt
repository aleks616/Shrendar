package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.common.model.SupportedLanguages
import org.aleks616.shrendar.exception.ForbiddenLoginException
import org.aleks616.shrendar.exception.InvalidOTPCodeException
import org.aleks616.shrendar.exception.ReusedPasswordException
import org.aleks616.shrendar.mail.service.EmailService
import org.aleks616.shrendar.securityCode.CodeGenerator
import org.aleks616.shrendar.securityCode.CodeStorage
import org.aleks616.shrendar.user.model.*
import org.aleks616.shrendar.user.repository.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
class UserAccountService(
    private val userRepository:UserRepository,
    private val userLogRepository:UserLogRepository,
    private val rankRepository:RankRepository,
    private val userPasswordHistoryRepository:UserPasswordHistoryRepository,
    @Qualifier("registrationCodeStorage") private val registrationCodeStorage:CodeStorage,
    @Qualifier("passwordResetCodeStorage") private val passwordResetCodeStorage:CodeStorage,
    private val emailService:EmailService,
    private val encoder:BCryptPasswordEncoder,
    private val xpService:XpService,
) {

    fun getUserByLogin(login:String):User? {
        return userRepository.findByLogin(login)
    }

    fun matches(raw:String,encrypted:String):Boolean {
        return encoder.matches(raw,encrypted)
    }

    fun getUsers():List<User> =userRepository.findAll()

    fun doesUserExist(id:Int):Boolean {
        return userRepository.existsById(id)
    }

    fun doesAccountExist(accountKey:String):Boolean {
        return getUsers().any {it.login.equals(accountKey,ignoreCase=true)||it.email.equals(accountKey,ignoreCase=true)}
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
                ranks=u.rank.let {UsersDto.RanksDto(it.id,it.name)},
                xp=u.xp,
                verified=u.verified
            )
        }
    }

    @Transactional
    fun addBio(bio:String,login:String){
        val user=userRepository.findByLogin(login)?:throw IllegalStateException("User not found")
        user.bio=bio
        userRepository.save(user)
    }
    //region account
    fun authenticate(req:LoginRequestDto,log:Boolean=true):String? {
        val user=if(req.email!=""&&req.email!=null) userRepository.findByEmail(req.email)
        else if(req.login!=""&&req.login!=null) userRepository.findByLogin(req.login)
        else null
        if(user==null) return null
        if(user.deleted==true) return null
        val userLog=findUserLog(user.id)

        if(userLog.accountDeletionScheduledTime!=null){
            userLog.accountDeletionScheduledTime=null
            userLogRepository.save(userLog)
            emailService.sendAccountDeletionCancelledMessage(user.email!!)
        }
        if(log){
            userLog.lastLoginTime=Instant.now()
            userLogRepository.save(userLog)
        }

        val zone=ZoneId.of("UTC")
        val lastLoginDate=LocalDate.ofInstant(Instant.now(),zone)
        val hasLoggedInToday:Boolean=lastLoginDate==LocalDate.now(zone)
        if(!hasLoggedInToday)
            xpService.increaseUserXp(user.login!!,6)

        return if(matches(req.password,user.passwordHash?:"")) user.login else null
    }

    fun initiateRegistration(req:RegisterRequestDto) {
        if(ForbiddenLogins.isForbidden(req.login)) throw ForbiddenLoginException("forbidden_login")
        if(doesAccountExist(req.login)||doesAccountExist(req.email)) throw IllegalArgumentException("Account with login/email already exists") //todo add strings
        if(!registrationCodeStorage.canSendCode(req.email)) throw IllegalStateException("too_many_email_requests")
        val code=CodeGenerator.generateCode(numericOnly=true)
        registrationCodeStorage.storeCode(req.email,code)
        emailService.sendVerificationCode(req.email,code,req.language?:SupportedLanguages.EN)
    }

    fun createUser(req:RegisterRequestDto,code:String) {
        if(!registrationCodeStorage.validateCode(req.email,code)) throw InvalidOTPCodeException()
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

        emailService.sendAccountCreatedMessage(req.email,req.language?:SupportedLanguages.EN)
    }

    fun requestPasswordReset(accountKey:String,language:SupportedLanguages) {
        if(!doesAccountExist(accountKey)) throw IllegalArgumentException("account_not_found")
        if(!passwordResetCodeStorage.canSendCode(accountKey)) throw IllegalStateException("too_many_email_requests")
        val code=CodeGenerator.generateCode(numericOnly=true)
        //store code for EMAIL, NEVER FOR LOGIN
        val user=userRepository.findByLogin(accountKey)?:userRepository.findByEmail(accountKey)?:throw IllegalArgumentException("account_not_found")
        passwordResetCodeStorage.storeCode(user.email!!,code)

        emailService.sendPasswordResetMessage(user.email!!,code,language)
    }
    fun changePassword(request:ResetPasswordDto) {
        if(!passwordResetCodeStorage.validateCode(request.email,request.code)) throw InvalidOTPCodeException()
        val encryptedPassword=encoder.encode(request.newPassword)
        val userToChange=userRepository.findAll().firstOrNull {it.email.equals(request.email,ignoreCase=true)}?:throw IllegalStateException("account_not_found")
        val userPasswordHistory=UserPasswordHistory()
        val passwordHistory=userPasswordHistoryRepository.findAllByUserId(userToChange.id)
        passwordHistory.forEach {
            if(it.password==encryptedPassword) throw ReusedPasswordException()
        }
        userPasswordHistory.user=userToChange
        userPasswordHistory.password=encryptedPassword
        userPasswordHistoryRepository.save(userPasswordHistory)
        deleteOldPasswordHistory(userToChange.id)

        userToChange.passwordHash=encryptedPassword
        userRepository.save(userToChange)
        val userLog=findUserLog(userToChange.id)
        userLog.passwordChangedTime=Instant.now()
        userLogRepository.save(userLog)

        emailService.sendPasswordHasBeenChangedMessage(request.email,request.language?:SupportedLanguages.EN)
    }

    fun changeUsername(email:String, newUsername:String){
        val user=userRepository.findByEmail(email)?:throw IllegalStateException("account_not_found")
        user.username=newUsername
        userRepository.save(user)
        val userLog=findUserLog(user.id)
        if(userLog.displayNameChangedTime!=null){
            if(ChronoUnit.DAYS.between(userLog.displayNameChangedTime,Instant.now())<90)
                throw IllegalStateException("username_change_limit")
        }
        userLog.displayNameChangedTime=Instant.now()
        userLogRepository.save(userLog)
    }

    fun changeEmail(email:String, newEmail:String){
        val user=userRepository.findByEmail(email)!!
        user.email=newEmail
        userRepository.save(user)
    }

    fun addBirthday(email:String, date:LocalDate){
        val user=userRepository.findByEmail(email)?:throw IllegalStateException("account_not_found")
        user.birthDate=date
        val userLog=findUserLog(user.id)
        if(userLog.birthdayChangedTime!=null){
            if(ChronoUnit.DAYS.between(userLog.birthdayChangedTime,Instant.now())<180)
                throw IllegalStateException("birthday_change_limit")
        }
        userLog.birthdayChangedTime=Instant.now()
        userRepository.save(user)
        userLogRepository.save(userLog)
    }

    fun findUserLog(userId:Int):UserLog{
        return userLogRepository.findById(userId).orElseThrow {IllegalStateException("UserLog not found for user id $userId")}
    }

    fun requestDeletion(userEmail:String){
        val user=userRepository.findByEmail(userEmail)?:throw IllegalStateException("account_not_found")
        val userLog=findUserLog(user.id)
        userLog.accountDeletionScheduledTime=Instant.now()
        userLogRepository.save(userLog)
        emailService.sendAccountScheduledForDeletionMessage(user.email!!)
    }

    @Scheduled(fixedRate=24*60*60*1000)
    @Transactional
    fun checkAccountScheduledToBeDeleted(){
        val users=userRepository.findAll()
        users.forEach{user->
            val userLog=findUserLog(user.id)
            if(userLog.accountDeletionScheduledTime!=null){
                if(ChronoUnit.DAYS.between(userLog.accountDeletionScheduledTime,Instant.now())>=21){
                    user.deleted=true
                    userRepository.save(user)
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
    //endregion

}
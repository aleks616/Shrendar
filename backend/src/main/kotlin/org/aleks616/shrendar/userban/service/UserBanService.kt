package org.aleks616.shrendar.userban.service

import org.aleks616.shrendar.user.repository.UserRepository
import org.aleks616.shrendar.userban.model.*
import org.aleks616.shrendar.userban.repository.UsersBanRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UserBanService(private val usersBanRepository:UsersBanRepository,private val userRepository:UserRepository) {

    fun isBanned(userId:Int):Boolean {
        return usersBanRepository.findIfUserBanned(userId)==1
    }

    fun isBanned(login:String):Boolean {
        return usersBanRepository.findIfUserBanned(login)==1
    }

    fun getActiveBans():List<BansDto> {
        return usersBanRepository.findByUntilIsAfterNow().map {
            BansDto(
                id=it.id,
                userId=it.user?.id,
                userLogin=it.user?.login,
                userUsername=it.user?.username,
                userRank=it.user?.rank?.id,
                at=it.at,
                until=it.until,
                length=Duration.between(it.at,it.until),
                description=it.description,
                byId=it.by?.id,
                byLogin=it.by?.login,
                byUsername=it.by?.username,
                byRank=it.by?.rank?.id,
                appealReason=it.appealReason,
            )
        }
    }

    fun getBansWithAppeal():List<BansDto> {
        return usersBanRepository.findByActiveAndHasAppeal().map {
            BansDto(
                id=it.id,
                userId=it.user?.id,
                userLogin=it.user?.login,
                userUsername=it.user?.username,
                userRank=it.user?.rank?.id,
                at=it.at,
                until=it.until,
                length=Duration.between(it.at,it.until),
                description=it.description,
                byId=it.by?.id,
                byLogin=it.by?.login,
                byUsername=it.by?.username,
                byRank=it.by?.rank?.id,
                appealReason=it.appealReason,
            )
        }
    }

    fun getAllBansOfUser(userId:Int):UserBansDto {
        return usersBanRepository.findByUser_Id(userId).toUserBansDto()
    }

    fun getCurrentUserBan(userId:Int):BansDto {
        val data=usersBanRepository.findCurrentUserBanData(userId)?:throw IllegalStateException("user is not banned")
        return BansDto(
            id=data.id,
            userId=data.user?.id,
            userLogin=data.user?.login,
            userUsername=data.user?.username,
            userRank=data.user?.rank?.id,
            at=data.at,
            until=data.until,
            length=Duration.between(data.at,data.until),
            description=data.description,
            byId=data.by?.id,
            byLogin=data.by?.login,
            byUsername=data.by?.username,
            byRank=data.by?.rank?.id,
            appealReason=data.appealReason,
        )
    }

    fun getCurrentUserBanData(userId:Int):UsersBan {
        return usersBanRepository.findCurrentUserBanData(userId)?:throw IllegalStateException("user is not banned")
    }

    fun List<UsersBan>.toUserBansDto():UserBansDto {
        val user=firstOrNull()?.user?:return UserBansDto()
        return UserBansDto(
            userId=user.id,
            userLogin=user.login,
            userUsername=user.username,
            userRank=user.rank?.id,
            bans=mapNotNull {b->
                UserBansDetailsDto(
                    id=b.id,
                    at=b.at,
                    until=b.until,
                    length=Duration.between(b.at,b.until),
                    description=b.description,
                    byId=b.by?.id,
                    byLogin=b.by?.login,
                    byUsername=b.by?.username,
                    byRank=b.by?.rank?.id,
                    appealed=b.appealed,
                    appealReason=b.appealReason,
                    appealedBy=b.appealedBy?.id
                )
            }
        )
    }

    fun getBansByMod(userId:Int):UserBansDto {
        return usersBanRepository.findByBy_Id(userId).toUserBansModsDto()
    }

    fun List<UsersBan>.toUserBansModsDto():UserBansDto {
        val mod=firstOrNull()?.by?:return UserBansDto()
        return UserBansDto(
            userId=mod.id,
            userLogin=mod.login,
            userUsername=mod.username,
            userRank=mod.rank?.id,
            bans=mapNotNull {b->
                UserBansDetailsDto(
                    id=b.id,
                    at=b.at,
                    until=b.until,
                    length=Duration.between(b.at,b.until),
                    description=b.description,
                    byId=b.user?.id,
                    byLogin=b.user?.login,
                    byUsername=b.user?.username,
                    byRank=b.user?.rank?.id,
                    appealed=b.appealed,
                    appealReason=b.appealReason,
                    appealedBy=b.appealedBy?.id
                )
            }
        )
    }

    fun banUser(data:BanDto,userLogin:String) {
        val byUser=userRepository.findByLogin(userLogin)
        val now=Instant.now()
        val duration=Duration.of(data.duration!!.toLong(),ChronoUnit.HOURS)
        usersBanRepository.save(UsersBan().apply {
            user=userRepository.findUserById(data.userId!!)
            at=now
            until=now.plus(duration)
            description=data.description
            by=byUser
        })
    }

    fun appealBan(reason:String,login:String){
        if(!isBanned(login)) throw IllegalStateException("user is not banned")
        val user=userRepository.findByLogin(login)?:throw IllegalStateException("user not found")
        val usersBan=getCurrentUserBanData(user.id!!)
        if(usersBan.appealReason!=null) throw IllegalStateException("you already filed an appeal")
        usersBan.appealReason=reason
        usersBanRepository.save(usersBan)
    }

    fun cancelBan(userId:Int,modLogin:String){
        if(!isBanned(userId)) throw IllegalStateException("user is not banned")
        val user=userRepository.findUserById(userId)?:throw IllegalStateException("user not found")
        val usersBan=getCurrentUserBanData(user.id!!)
        val mod=userRepository.findByLogin(modLogin)?:throw IllegalStateException("mod not found")
        usersBan.appealed=true
        usersBan.appealedBy=mod
        usersBanRepository.save(usersBan)
    }

}

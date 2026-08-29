package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.time.LocalDate

@Service
class XpService(
    private val userRepository:UserRepository,
    private val rankRepository:RankRepository
) {
    @Scheduled(fixedRate=24*60*60*1000)
    @Transactional
    fun increaseAllUsersXp() {
        if(!File("last-xp-update-date").exists())
            File("last-xp-update-date").createNewFile()

        val date=LocalDate.now().toString()
        val previousDate=File("last-xp-update-date").readText()
        if(date==previousDate) return
        val users=userRepository.findAll()
        users.forEach {user->
            user.xp=(user.xp?:0)+1
        }
        userRepository.saveAll(users)
        updateAllUsersRanks()
        File("last-xp-update-date").writeText(date)
    }

    @Transactional
    fun updateAllUsersRanks() {
        val users=userRepository.findAll()
        val ranks:List<Rank> =rankRepository.findAll().sortedByDescending {it.id}
        users.forEach {user->
            val newRank:Rank=ranks.first {(user.xp!!)>=(it.minXp!!)}
            if(user.rank!=newRank) {
                user.rank=newRank
            }
        }
        userRepository.saveAll(users)
    }

    /**
     * Updates user's xp by @amount
     * also for decrease with -amount
     * @login - user's login, DO NOT CONFUSE WITH DISPLAY NAME
     * **/
    @Transactional
    fun increaseUserXp(login:String,amount:Int) {
        val user=userRepository.findByLogin(login)?:return
        user.xp=(user.xp?:0)+amount
        userRepository.save(user)
        updateUserRank(user)
    }

    @Transactional
    fun updateUserRank(user:User) {
        val ranks:List<Rank> =rankRepository.findAll().sortedByDescending {it.id}
        val newRank:Rank=ranks.first {(user.xp!!)>=(it.minXp!!)}
        if(user.rank!=newRank) {
            user.rank=newRank
        }
        userRepository.save(user)
        //todo save notification
    }

    @Transactional
    fun manualRankAssign(login:String,rankId:Int){
        val user=userRepository.findByLogin(login)?:throw RuntimeException("User with login $login not found")
        val ranks=rankRepository.findAll()
        val rank:Rank=ranks.find {it.id==rankId}?:throw IllegalArgumentException("Rank with id $rankId not found")
        val minRankXp=rank.minXp
        user.rank=rank
        user.xp=minRankXp
        userRepository.save(user)
    }
}
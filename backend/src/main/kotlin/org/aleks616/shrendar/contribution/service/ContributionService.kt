package org.aleks616.shrendar.contribution.service

import jakarta.transaction.Transactional
import org.aleks616.shrendar.contribution.model.Contribution
import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.service.UserService
import org.springframework.stereotype.Service

@Service
class ContributionService(
    private val contributionRepository:ContributionRepository,
    private val userService:UserService

){
    fun getAll():List<Contribution> =contributionRepository.findAll()

    fun getContributionCountByUser(userId:Int):Int{
        return contributionRepository.getContributionCountByUser(userId)
    }

    @Transactional
    fun confirmDataAddRequest(changeId:Int,confirmedUserLogin:String):Boolean{
        val confirmingUser:User=userService.getUserByLogin(confirmedUserLogin)!!
        if(confirmingUser.rank!!.id!!<10) return false
        val contributions=contributionRepository.getByChangeId(changeId)
        contributions.forEach {
            it.confirmed=true
            it.confirmedBy=confirmingUser.id
            contributionRepository.save(it)
        }
        return true
    }
}
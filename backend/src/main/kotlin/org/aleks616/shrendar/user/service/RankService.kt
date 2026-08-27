package org.aleks616.shrendar.user.service

import org.aleks616.shrendar.contribution.repository.ContributionRepository
import org.aleks616.shrendar.exception.ContributionLimitExceededException
import org.aleks616.shrendar.user.model.Rank
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.repository.RankRepository
import org.springframework.stereotype.Service

@Service
class RankService(
    private val rankRepository:RankRepository,
    private val contributionRepository:ContributionRepository
){

    fun checkRank(requestingUser:User):ContributionLimitExceededException?{
        val rankId:Int=requestingUser.rank!!.id!!
        val rankLimit=rankRepository.getRankById(rankId).allowedContributions!!
        val recentContributionCount=contributionRepository.getContributionCountByUser(requestingUser.id!!)
        if(recentContributionCount>=rankLimit) return ContributionLimitExceededException("You have reached your weekly limit. Limit for rank $rankId is $rankLimit")
        return null
    }
}
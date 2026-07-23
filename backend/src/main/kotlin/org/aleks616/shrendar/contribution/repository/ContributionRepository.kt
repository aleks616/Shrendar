package org.aleks616.shrendar.contribution.repository

import org.aleks616.shrendar.contribution.model.Contribution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ContributionRepository :JpaRepository<Contribution,Int> {

    @Query("""SELECT c.changeId FROM Contribution c ORDER BY c.changeId DESC LIMIT 1""")
    fun findTopChangeId():Int


    @Query("""
        SELECT COUNT(distinct c.changeId)
        FROM Contribution c
        WHERE c.user.id=:user
        AND (CURRENT_TIMESTAMP-c.changedAt)<7*24*60*60
    """)
    fun getContributionCountByUser(user:Int):Int
    fun getByChangeId(changeId:Int):List<Contribution>
}
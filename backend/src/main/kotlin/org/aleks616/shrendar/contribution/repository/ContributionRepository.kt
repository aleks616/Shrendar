package org.aleks616.shrendar.contribution.repository

import org.aleks616.shrendar.contribution.model.Contribution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ContributionRepository:JpaRepository<Contribution,Int> {

    @Query("""SELECT c.changeId FROM Contribution c ORDER BY c.changeId DESC LIMIT 1""")
    fun findTopChangeId():Int?

    @Query(
        """
        SELECT COUNT(distinct c.changeId)
        FROM Contribution c
        WHERE c.user.id=:user
        AND (CURRENT_TIMESTAMP-c.changedAt)<7*24*60*60
        AND c.changedTable!='bands_members'
    """
    )
    fun getContributionCountByUser(user:Int):Int
    fun getByChangeId(changeId:Int):List<Contribution>

    @Query(
        """
        SELECT *
        FROM contribution c
        WHERE c.user_id=:userId
    """,nativeQuery=true
    )
    fun findContributionsByUserId(userId:Int):MutableList<Contribution>

    @Query(
        """
        SELECT *
        FROM contribution c
        WHERE c.confirmed_by=:confirmedBy
    """,nativeQuery=true
    )
    fun findContributionsByConfirmedBy(confirmedBy:Int):MutableList<Contribution>

    @Query(
        """
        SELECT *
        FROM contribution c
        WHERE c.changed_table=:table
    """,nativeQuery=true
    )
    fun findContributionsByChangedTable(table:String):MutableList<Contribution>

    @Query(
        """
        SELECT *
        FROM contribution c
        WHERE c.changed_table=:table AND c.changed_record_id=:recordId
    """,nativeQuery=true
    )
    fun findContributionsByChangedTableAndChangedRecordId(table:String,recordId:Int):MutableList<Contribution>

    @Query(
        """
       SELECT *
       FROM contribution c
       WHERE DATE(c.changed_at) BETWEEN DATE(:changedAtAfter) AND DATE(:changedAtBefore)
    """,nativeQuery=true
    )
    fun findContributionsByChangedAtBetween(
        changedAtAfter:LocalDate,
        changedAtBefore:LocalDate
    ):MutableList<Contribution>

    @Query(
        """
       SELECT *
       FROM contribution c
       WHERE DATE(c.changed_at) BETWEEN DATE(:changedAtAfter) AND DATE(:changedAtBefore)
       AND c.user_id=:user
    """,nativeQuery=true
    )
    fun findContributionsByChangedAtBetweenAndUser(
        changedAtAfter:LocalDate,
        changedAtBefore:LocalDate,
        user:Int
    ):MutableList<Contribution>

    @Query(
        """
            SELECT *
            FROM contribution c
            WHERE c.action=:action
            AND c.user_id=:user
        """,nativeQuery=true
    )
    fun findContributionsByActionAndUserId(action:String,user:Int):MutableList<Contribution>

}
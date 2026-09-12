package org.aleks616.shrendar.userreport.repository

import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface UsersReportRepository:JpaRepository<UsersReport,Long> {
    fun findByReportedUser(reportedUser:User):MutableList<UsersReport>
    fun findByResolved(resolved:Boolean):MutableList<UsersReport>

    @Query("""
        SELECT r 
        FROM UsersReport r
        WHERE r.reportedUser.id=:reportUserId AND r.requestingUser.id=:requestingUser AND r.at>:start
    """)

    fun findRecentByUser(requestingUser:Int,reportUserId:Int,start:Instant):List<UsersReport>
    fun findUsersReportById(id:Long):UsersReport
}
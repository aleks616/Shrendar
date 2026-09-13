package org.aleks616.shrendar.userban.repository

import org.aleks616.shrendar.userban.model.UsersBan
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface UsersBanRepository:JpaRepository<UsersBan,Int> {

    @Query("""
        SELECT
        CASE WHEN EXISTS(
        SELECT b.id
        FROM users_bans b JOIN user u ON b.user=u.user_id
        WHERE u.user_id=:userId AND b.until>CURRENT_TIMESTAMP
        )
        THEN 1
        ELSE 0
        END as banned
    """,nativeQuery=true)
    fun findIfUserBanned(userId:Int):Int

    @Query("""
        SELECT
        CASE WHEN EXISTS(
        SELECT b.id
        FROM users_bans b JOIN user u ON b.user=u.user_id
        WHERE u.login=:login AND b.until>CURRENT_TIMESTAMP
        )
        THEN 1
        ELSE 0
        END as banned
    """,nativeQuery=true)
    fun findIfUserBanned(login:String):Int


    @Query("""
        SELECT *
        FROM users_bans b JOIN user u ON b.user=u.user_id
        WHERE u.user_id=:userId
        AND b.until>CURRENT_TIMESTAMP AND b.appealed=false
    """,nativeQuery=true)
    fun findCurrentUserBanData(userId:Int):UsersBan?

    @Query("""
        SELECT *
        FROM users_bans b
        WHERE b.until>CURRENT_TIMESTAMP AND b.appealed=false
    """,nativeQuery=true)
    fun findByUntilIsAfterNow():MutableList<UsersBan>

    @Query("""
        SELECT *
        FROM users_bans b
        WHERE b.until>CURRENT_TIMESTAMP AND b.appealed=false
        AND b.appeal_reason IS NOT NULL
    """,nativeQuery=true)
    fun findByActiveAndHasAppeal():MutableList<UsersBan>
    fun findByUser_Id(userId:Int):MutableList<UsersBan>
    fun findByBy_Id(byId:Int):MutableList<UsersBan>
}
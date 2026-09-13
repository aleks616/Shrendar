package org.aleks616.shrendar.userreport.model

import java.io.Serializable
import java.time.Instant
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.user.model.UsersReport}
 */
data class UsersReportDto(
    val id:Long=0L,
    val reportedUserId:Int?=null,
    val reportedUserLogin:String?=null,
    val reportedUserUsername:String?=null,
    val reportedUserRankId:Int?=null,
    val reportedUserBirthDate:LocalDate?=null,
    val reportedUserXp:Int?=null,
    val reportedUserVerified:Boolean?=false,
    val reportedUserBio:String?=null,
    val requestingUserId:Int?=null,
    val requestingUserLogin:String?=null,
    val requestingUserRankId:Int?=null,
    val at:Instant?=null,
    val resolved:Boolean=false,
    val description:String?=null
):Serializable
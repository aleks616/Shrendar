package org.aleks616.shrendar.userreport.model

import java.io.Serializable
import java.time.Instant
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.user.model.UsersReportDto}
 */
data class ReportsByUserDto(
    val userId:Int?=null,
    val login:String?=null,
    val username:String?=null,
    val rankId:Int?=null,
    val birthDate:LocalDate?=null,
    val xp:Int?=null,
    val verified:Boolean?=false,
    val bio:String?=null,
    val reports:List<ReportDetailsDto>?=null
):Serializable

data class ReportDetailsDto(
    val id:Long=0L,
    val requestingUserId:Int?=null,
    val requestingUserLogin:String?=null,
    val requestingUserRankId:Int?=null,
    val at:Instant?=null,
    val resolved:Boolean=false,
    val description:String?=null
)
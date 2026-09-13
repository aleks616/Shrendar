package org.aleks616.shrendar.userban.model

import java.io.Serializable
import java.time.Duration
import java.time.Instant

/**
 * DTO for {@link org.aleks616.shrendar.userban.model.BansDto}
 */
data class UserBansDto(
    val userId:Int?=null,
    val userLogin:String?=null,
    val userUsername:String?=null,
    val userRank:Int?=null,
    val bans:List<UserBansDetailsDto>?=null
):Serializable

data class UserBansDetailsDto(
    val id:Int=0,
    val at:Instant=Instant.now(),
    val until:Instant=Instant.now(),
    val length:Duration?=null,
    val description:String?=null,
    val byId:Int?=null,
    val byLogin:String?=null,
    val byUsername:String?=null,
    val byRank:Int?=null,
    val appealed:Boolean?=null,
    val appealReason:String?=null,
    val appealedBy:Int?=null,
)
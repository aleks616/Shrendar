package org.aleks616.shrendar.dto

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.entities.Users}
 */
data class UsersDto(
    val id:Int?=null,
    val login:String?=null,
    val username:String?=null,
    val passwordHash:String?=null,
    val email:String?=null,
    val createdAt:Long?=null,
    val ranks:RanksDto?=null,
    val birthDate:String?=null,
    val xp:Int?=null,
    val verified:Boolean?=false
):Serializable{
    /**
     * DTO for {@link org.aleks616.shrendar.entities.Ranks}
     */
    data class RanksDto(
        val id:Int?=null,
        val name:String?=null
    ):Serializable
}
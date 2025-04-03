package org.aleks616.shrendar

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val login:String,
    val displayName:String,
    val email:String,
    val password:String
)

@Serializable
data class Users(
    val id:Int,
    val login:String,
    val displayName:String,
    val password:String,
    val email:String,
    val createdAt:Int,
    val xp:Int?
)

@Serializable
data class Ranks(
    val id:Int?,
    val name:String?,
    val minXp:Int?
)
@Serializable
data class UsersDto(
    val id:Int?=null,
    val login:String?=null,
    val username:String?=null,
    val passwordHash:String?=null,
    val email:String?=null,
    val createdAt:Long?=null,
    val ranks:RanksDto?=null,
    val birthDate:String?=null,
    val xp:Int?=null
)
@Serializable
data class RanksDto(
    val id:Int?=null,
    val name:String?=null
)

expect class NetworkClient() {
    suspend fun fetchRanks():List<Ranks>
    suspend fun fetchUsers():List<UsersDto>
    suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray)
}

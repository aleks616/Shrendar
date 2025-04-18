package org.aleks616.shrendar

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val login:String?,
    val email:String?,
    val password:String
)

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
    val xp:Int?=null,
    val verified:Boolean?=null
)
@Serializable
data class RanksDto(
    val id:Int?=null,
    val name:String?=null
)
@Serializable
data class ArtistsBirthDayDto(
    val id:Int?=null,
    val name:String?=null,
    val birthDay:Int?=null,
    val birthMonth:Int?=null,
    val birthYear:Int?=null,
    val age:Int?=null
)

expect class NetworkClient() {
    suspend fun fetchRanks():List<Ranks>
    suspend fun fetchUsers():List<UsersDto>
    suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray)
    suspend fun doesLoginExist(login:String):Boolean
    suspend fun doesEmailExist(email:String):Boolean

    suspend fun isPasswordCorrect(email:String?=null,login:String?=null,password:CharArray):Boolean
    suspend fun fetchArtistsBirthdays(month:Int?,day:Int?):List<ArtistsBirthDayDto>
}

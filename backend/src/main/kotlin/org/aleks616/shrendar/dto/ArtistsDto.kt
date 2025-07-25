package org.aleks616.shrendar.dto

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.entities.Artists}
 */
data class ArtistsBirthDayDto(
    val id:Int?=null,
    val name:String?=null,
    val birthDay:Int?=null,
    val birthMonth:Int?=null,
    val birthYear:Int?=null,
    val age:Int?=null
    ):Serializable

data class ArtistsDeathDayDto(
    val id:Int=-1,
    val name:String="",
    val deathDay:Int?=null,
    val deathMonth:Int?=null,
    val deathYear:Int?=null,
    val age:Int?=null
):Serializable

data class RecentDeathAnniversariesDTO(
    val id:Int=-1,
    val name:String="",
    val birthDate:String?=null,
    val deathDate:String?=null,
   // val age:Int?=null
):Serializable

//todo: died *RECENTLY*
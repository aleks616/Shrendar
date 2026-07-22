package org.aleks616.shrendar.artist.model

import java.io.Serializable
import java.time.LocalDate


data class ArtistWikiDto(
    val id:Int?=null,
    val name:String?=null,
    val birthDate:LocalDate?=null,
    val daysTillBirthday:Int?=null,
    val deathDate:LocalDate?=null,
    val daysTillDeathAnniversary:Int?=null,
    val age:Int?=null,
    val gender:String?=null,
    val country:String?=null,
    val zodiacSign:ZodiacSign?=null,
    val chineseZodiacSign:ChineseZodiacSign?=null,
    val description:String?=null,
    val artistImageUrl:String?=null
):Serializable


/**
 * DTO for {@link org.aleks616.shrendar.entities.Artists}
 *//*

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
):Serializable*/

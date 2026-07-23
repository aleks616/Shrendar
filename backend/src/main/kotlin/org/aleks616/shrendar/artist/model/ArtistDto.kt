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


data class ArtistAddDto(
    val name:String?=null,
    val birthDate:LocalDate?=null,
    val deathDate:LocalDate?=null,
    val gender:Char?=null,
    val country:Int?=null,
    val description:String?=null,
    val artistImageUrl:String?=null
):Serializable
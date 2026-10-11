package com.example.client.artist

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistWikiDto(
    val id:Long?=null,
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
    val artistImageUrl:String?=null,
    val bands:List<ArtistsBandsHistoryDto>?=null,
    val favorite:Boolean=false
)
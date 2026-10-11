package org.aleks616.shrendar.artist.model

import org.aleks616.shrendar.band.model.ArtistBandsHistoryDto
import java.io.Serializable
import java.time.LocalDate


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
    val bands:List<ArtistBandsHistoryDto>?=null,
    val favorite:Boolean?=null
):Serializable
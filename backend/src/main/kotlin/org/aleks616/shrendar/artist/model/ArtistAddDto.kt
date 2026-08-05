package org.aleks616.shrendar.artist.model

import java.io.Serializable
import java.time.LocalDate

data class ArtistAddDto(
    val id:Int?=null,
    val name:String?=null,
    val birthDate:LocalDate?=null,
    val deathDate:LocalDate?=null,
    val gender:Char?=null,
    val country:Int?=null,
    val description:String?=null,
    val artistImageUrl:String?=null
):Serializable
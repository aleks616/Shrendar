package com.example.client.artist

import kotlinx.datetime.LocalDate
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistAddDto(
    val id:Long?=null,
    val name:String?=null,
    val birthDate:String?=null,
    val deathDate:String?=null,
    val gender:String?=null,
    val country:Int?=null,
    val description:String?=null,
    val artistImageUrl:String?=null
)
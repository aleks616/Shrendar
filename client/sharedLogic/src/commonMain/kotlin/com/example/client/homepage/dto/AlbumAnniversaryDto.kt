package com.example.client.homepage.dto

import kotlinx.datetime.LocalDate
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class AlbumAnniversaryDto(
    val id:Long?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:String?=null,
    val artworkUrl:String?=null,
    val age:Int=0,
    val daysTillAnniversary:Int?=null,
)
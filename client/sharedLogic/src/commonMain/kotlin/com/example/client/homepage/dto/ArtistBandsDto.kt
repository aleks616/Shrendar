package com.example.client.homepage.dto

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistBandsDto(
    val id:Long?=null,
    val artistId:Long?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val role:String?=null,
    val joinedYear:Int?=null,
    val leftYear:Int?=null,
    val nickname:String?=null
)
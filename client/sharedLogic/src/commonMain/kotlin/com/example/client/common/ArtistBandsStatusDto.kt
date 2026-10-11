package com.example.client.common

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistBandsStatusDto(
    val artistId:Long?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val current:Boolean?=null,
)

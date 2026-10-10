package com.example.client.artist

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistsBandsHistoryDto(
    val memberId:Long?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val nickname:String?=null,
    val yearRole:MutableList<String>?=null
)
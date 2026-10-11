package com.example.client.band

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class BandsMembersWikiDto(
    val id:Long?=null,
    val artistId:Long?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val nickname:String?=null,
    var yearRole:MutableList<String>?=null,
)
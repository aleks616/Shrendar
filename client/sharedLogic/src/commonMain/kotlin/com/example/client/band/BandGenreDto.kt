package com.example.client.band

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class BandGenreDto(
    var id:Int?=null,
    var name:String?=null,
    var formedYear:Int?=null,
    var country:String?=null,
    var similarity:Double?=null
)
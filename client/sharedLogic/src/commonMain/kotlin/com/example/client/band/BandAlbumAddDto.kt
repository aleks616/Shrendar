package com.example.client.band

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class BandAlbumAddDto(
    val id:Int=0,
    val name:String?=null,
    val formedYear:Int?=null,
    val disbandedYear:Int?=null,
    val country:String?=null
)
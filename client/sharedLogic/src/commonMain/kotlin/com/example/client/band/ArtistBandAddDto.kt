package com.example.client.band

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistBandAddDto(
    var id:Long?=null,
    var bandId:Int?=null,
    var artistId:Long?=null,
    var nickname:String?=null,
    var role:String?=null,
    var joinedYear:Int?=null,
    var leftYear:Int?=null
)
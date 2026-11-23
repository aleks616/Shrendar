package com.example.client.band

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class BandAddDto(
    var id:Int?=null,
    var name:String?=null,
    var formedYear:Int?=null,
    var status:String?=null,
    var disbandedYear:Int?=null,
    var country:Int?=null,
    var description:String?=null,
    var imageUrl:String?=null
)
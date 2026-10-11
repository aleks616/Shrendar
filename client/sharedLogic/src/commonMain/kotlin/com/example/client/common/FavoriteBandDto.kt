package com.example.client.common

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class FavoriteBandDto(
    val id:Int?=null,
    val name:String?=null,
    val country:String?=null,
    val activeYears:String?=null
)

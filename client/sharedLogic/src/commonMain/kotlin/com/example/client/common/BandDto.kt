package com.example.client.common

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class BandDto(
    val id:Int?=null,
    val name:String?=null
)
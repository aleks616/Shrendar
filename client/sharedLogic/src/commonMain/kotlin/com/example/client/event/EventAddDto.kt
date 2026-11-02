package com.example.client.event

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class EventAddDto(
    val id:Int?=null,
    val bandId:Int?=null,
    val date:String?=null,
    val name:String?=null,
    val description:String?=null
)
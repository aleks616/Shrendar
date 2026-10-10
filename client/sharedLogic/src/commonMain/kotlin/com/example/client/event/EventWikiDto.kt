package com.example.client.event

import kotlinx.datetime.LocalDate
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class EventWikiDto(
    val id:Int?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val date:LocalDate?=null,
    val daysTillAnniversary:Int?=null,
    val name:String?=null,
    val description:String?=null,
    val yearsSince:Int?=null
)
package com.example.client.common

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

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
package com.example.client.homepage.dto

import kotlinx.datetime.LocalDate
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistAnniversaryDto(
    val id:Long?=null,
    val name:String?=null,
    val anniversaryDate:LocalDate?=null,
    val daysTillAnniversary:Int?=null,
    val yearsSince:Int=0,
    val country:String?=null,
    val imageUrl:String?=null
)
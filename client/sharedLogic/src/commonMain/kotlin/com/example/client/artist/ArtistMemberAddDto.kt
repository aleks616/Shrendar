package com.example.client.artist

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class ArtistMemberAddDto(
    val id:Long=0,
    val name:String?=null,
    val birthYear:Int?=null,
    val deathYear:Int?=null,
    val country:String?=null
)
package com.example.client.album

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class AlbumAddDto(
    val id:Long?=null,
    val bandId:Int?=null,
    val title:String?=null,
    val releaseDate:String?=null,
    val type:String?=null,
    val description:String?=null,
    val mainSubgenre:Int?=null,
    val importance:Int?=null,
    val artworkUrl:String?=null
)
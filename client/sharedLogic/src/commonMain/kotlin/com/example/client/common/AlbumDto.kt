package com.example.client.common

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@ExperimentalJsExport
@JsExport
@Serializable
data class AlbumDto(
    val id:Long=0,
    val title:String="",
    val releaseDate:LocalDate?=null,
    val type:String?=null,
    val importance:Byte?=null,
    val genreId:Int=0,
    val genreName:String="",
    val artworkUrl:String?=null,
    val description:String?=null
)
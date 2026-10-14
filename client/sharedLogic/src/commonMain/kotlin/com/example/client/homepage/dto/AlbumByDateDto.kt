package com.example.client.homepage.dto

import com.example.client.Genre
import com.example.client.common.BandDto
import kotlinx.datetime.LocalDate
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class AlbumByDateDto(
    val id:Long?=null,
    val band:BandDto?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:String?=null,
    val importance:Byte?=null,
    val yearsSince:Int?=null,
    val genre:Genre?=null,
    val artworkUrl:String?=null,
)
package com.example.client.album

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import com.example.client.Genre
import com.example.client.common.BandDto

@ExperimentalJsExport
@JsExport
@Serializable
data class AlbumWikiDto(
    val id:Long?=null,
    val albumName:String?=null,
    val band:BandDto?=null,
    val releaseDate:LocalDate?=null,
    val albumAge:Int?=null,
    val daysTillAnniversary:Int?=null,
    val type:String?=null,
    val genre:Genre?=null,
    val description:String?=null,
    val artworkUrl:String?=null,
    val importance:Byte?=null,
)
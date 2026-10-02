package com.example.client.common

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class FavoriteArtistDto(
    val id:Long?=null,
    val name:String?=null,
    val bands:List<ArtistBandsStatusDto>?=null
)

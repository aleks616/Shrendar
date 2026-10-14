package com.example.client.homepage.dto

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class UpcomingAnniversariesDto(
    val artistBirthdays:List<ArtistAnniversaryDto>?=null,
    val artistDeathAnns:List<ArtistAnniversaryDto>?=null,
    val albums:List<AlbumAnniversaryDto>?=null
)
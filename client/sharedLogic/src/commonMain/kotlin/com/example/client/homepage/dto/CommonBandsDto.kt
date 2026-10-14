package com.example.client.homepage.dto

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class CommonBandsDto(
    val bands:List<ArtistBandsDto>?=null
)
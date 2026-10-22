package com.example.client.searching

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class SearchResultItem(
    val name:String,
    val info:String?=null,
    val description:String,
    val link:String?=null,
    val type:String,
    val imageUrl:String?=null
)
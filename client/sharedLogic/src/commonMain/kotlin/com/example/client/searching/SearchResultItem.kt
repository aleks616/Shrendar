package com.example.client.searching

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class SearchResultItem(
    val name:String?=null,
    val description:String?=null,
    val link:String?=null,
    val type:String?=null,
    val imageUrl:String?=null
)
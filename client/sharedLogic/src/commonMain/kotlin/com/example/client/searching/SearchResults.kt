package com.example.client.searching

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class SearchResults(
    val items:List<SearchResultItem>?=null
)
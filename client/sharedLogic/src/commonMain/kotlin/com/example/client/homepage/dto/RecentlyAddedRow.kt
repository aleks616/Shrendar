package com.example.client.homepage.dto

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class RecentlyAddedRow(
    val id:Long?=null,
    val changedColumn:String?=null,
    val oldValue:String?=null,
    val newValue:String?=null
)
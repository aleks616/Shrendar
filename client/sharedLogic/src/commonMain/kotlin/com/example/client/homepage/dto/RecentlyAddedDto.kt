package com.example.client.homepage.dto

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class RecentlyAddedDto(
    val changeId:Long?=null,
    val userId:Int?=null,
    val changedRecordId:Long?=null,
    val changedTable:String?=null,
    val data:List<RecentlyAddedRow>?=null,
    val changedAt:String?=null,
)
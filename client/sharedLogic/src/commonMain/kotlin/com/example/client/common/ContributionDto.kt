package com.example.client.common

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
@Serializable
data class ContributionDto(
    val id:Long?=null,
    val changeId:Long?=null,
    val userId:Int?=null,
    val action:String?=null,
    val changedTable:String?=null,
    val changedColumn:String?=null,
    val changedRecordId:Long?=null,
    val oldValue:String?=null,
    val newValue:String?=null,
    val changedAt:String?=null,
    val confirmed:Boolean?=null,
    val confirmedBy:Int?=null
)
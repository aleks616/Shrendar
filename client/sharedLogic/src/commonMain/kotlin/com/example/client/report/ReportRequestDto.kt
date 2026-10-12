package com.example.client.report

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class ReportRequestDto(
    val reportedUserId:Int=0,
    val reason:String?=null
)
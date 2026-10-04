package com.example.client.common

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class UserDto(
    val login:String?=null,
    val username:String?=null,
    val rankId:Int?=null,
    val xp:Int?=null,
)
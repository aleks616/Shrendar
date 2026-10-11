package com.example.client.account

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class LoginRequestDto(
    val login:String?,
    val email:String?,
    val password:String
)

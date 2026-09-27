package com.example.client.account

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
@JsExport
@ExperimentalJsExport
data class ResetPasswordDto(
    val email:String,
    val newPassword:String,
    val code:String,
    val language:String
)

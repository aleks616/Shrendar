package com.example.client.register

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class RegisterRequestDto(
    val login: String,
    val displayName: String,
    val email: String,
    val password: String,
    val language:String="EN"
)

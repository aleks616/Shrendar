package com.example.client.common

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class TranslationRequestDto(
    val text: String,
    val targetLanguage: String
)
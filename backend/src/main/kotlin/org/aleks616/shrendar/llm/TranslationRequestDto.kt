package org.aleks616.shrendar.llm

import java.io.Serializable

data class TranslationRequestDto(
    val text: String,
    val targetLanguage: String
):Serializable
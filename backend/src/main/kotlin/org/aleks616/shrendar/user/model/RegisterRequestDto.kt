package org.aleks616.shrendar.user.model

import org.aleks616.shrendar.common.model.SupportedLanguages
import java.io.Serializable

data class RegisterRequestDto(
    val login:String,
    val displayName:String,
    val email:String,
    val password:String,
    val language:SupportedLanguages?=SupportedLanguages.EN
):Serializable
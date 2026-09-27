package org.aleks616.shrendar.user.model

import org.aleks616.shrendar.common.model.SupportedLanguages

data class ResetPasswordDto(
    val email:String,
    val newPassword:String,
    val code:String,
    val language:SupportedLanguages
)
package org.aleks616.shrendar.user.model

data class ResetPasswordDto(
    val email:String,
    val newPassword:String
)
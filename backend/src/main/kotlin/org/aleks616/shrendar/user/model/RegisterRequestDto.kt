package org.aleks616.shrendar.user.model

import java.io.Serializable

data class RegisterRequestDto(
    val login:String,
    val displayName:String,
    val email:String,
    val password:String
):Serializable
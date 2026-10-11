package org.aleks616.shrendar.security

import java.io.Serializable

data class GoogleLoginRequestDto(
    val googleToken: String
):Serializable

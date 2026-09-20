package com.example.client.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val login: String,
    val displayName: String,
    val email: String,
    val password: String
)

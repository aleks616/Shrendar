package com.example.client.account

data class ResetPasswordDto(
    val email:String,
    val newPassword:String,
    val code:String,
    val language:String="EN"
)

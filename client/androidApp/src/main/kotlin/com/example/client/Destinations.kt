package com.example.client

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface Destinations:Destination {
    @Serializable
    data object Welcome:Destinations

    @Serializable
    data object Register:Destinations

    @Serializable
    data object SignIn:Destinations

    @Serializable
    data object RequestPasswordReset:Destinations

    @Serializable
    data object PasswordReset:Destinations

    @Serializable
    data object Profile:Destinations

    @Serializable
    data object Settings:Destinations
}

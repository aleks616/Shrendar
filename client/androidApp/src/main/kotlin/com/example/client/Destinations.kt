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
}

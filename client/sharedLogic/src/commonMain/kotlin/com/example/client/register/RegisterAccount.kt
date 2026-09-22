package com.example.client.register

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class RegisterAccount {
    suspend fun register(request:RegisterRequest):String{
        return RegisterClient.register(request)
    }
}

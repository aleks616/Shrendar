package com.example.client.register

class RegisterAccount {
    suspend fun register(request:RegisterRequest):String{
        return RegisterApi.register(request)
    }
}

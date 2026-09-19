package com.example.client.register

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object RegisterApi {
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun doesEmailExist(email:String):Boolean {
        return client.get("$BASE_URL/user-account/emailCheck") {
            parameter("email",email)
        }.body()
    }

    suspend fun doesLoginExist(login:String):Boolean {
        return client.get("$BASE_URL/user-account/loginCheck") {
            parameter("login",login)
        }.body()
    }

    suspend fun register(request:RegisterRequest):String {
        return client.post("$BASE_URL/user-account/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }


        const val BASE_URL="http://vvcpc4ndryhj.shares.zrok.io/api"
}

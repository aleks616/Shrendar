package com.example.client.register

import com.example.client.BASE_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


class RegisterApi private constructor(
    private val baseUrl:String,
    private val client:HttpClient
) {
    constructor():this(BASE_URL,createHttpClient())
    //constructor(baseUrl:String):this(baseUrl,createHttpClient())
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

    suspend fun register(request:RegisterRequestDto):String {
        return try{
            client.post("$BASE_URL/user-account/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
        catch (e:ClientRequestException) {
            e.response.bodyAsText()
        }
    }

    suspend fun registerConfirm(request:RegisterRequestDto,code:String):String {
        return try {
            client.post("$BASE_URL/user-account/register/confirm") {
                contentType(ContentType.Application.Json)
                setBody(request)
                parameter("code",code)
            }.body()
        }
        catch (e:ClientRequestException) {
            e.response.bodyAsText()
        }
    }

    companion object {
        private fun createHttpClient()=HttpClient {
            expectSuccess=true
            install(DefaultRequest) {
                header("skip_zrok_interstitial", "1")
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys=true
                })
            }
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object RegisterClient{
    suspend fun doesEmailExist(email:String):Boolean=RegisterApi().doesEmailExist(email)
    suspend fun doesLoginExist(login:String):Boolean=RegisterApi().doesLoginExist(login)
    suspend fun register(request:RegisterRequestDto):String=RegisterApi().register(request)
    suspend fun registerConfirm(request:RegisterRequestDto,code:String):String=RegisterApi().registerConfirm(request,code)
}

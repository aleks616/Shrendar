package com.example.client.common

import com.example.client.BASE_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

class LlmApi private constructor(
    private val baseUrl:String,
    private val client:HttpClient
) {
    constructor():this(BASE_URL,createHttpClient())
    internal constructor(testClient:HttpClient):this(BASE_URL,testClient)

    @OptIn(ExperimentalJsExport::class)
    suspend fun translate(request:TranslationRequestDto):String {
        return try {
            client.post("$BASE_URL/ai/translate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
        catch(e:ClientRequestException) {
            if(e.response.status.value==404) throw Exception("not_found")
            else throw Exception(e.response.bodyAsText())
        }
    }

    companion object {
        private fun createHttpClient()=HttpClient {
            expectSuccess=true
            install(DefaultRequest) {
                header("skip_zrok_interstitial","1")
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
object LlmClient {
    suspend fun translate(request:TranslationRequestDto)=LlmApi().translate(request)
}
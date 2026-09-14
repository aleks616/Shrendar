package com.example.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Genre(
    val id: Int,
    val name: String,
    val properties: String
)

class GenreApi private constructor(
    private val baseUrl: String,
    private val client: HttpClient
) {
    constructor() : this(DEFAULT_BASE_URL, createHttpClient())
    constructor(baseUrl: String) : this(baseUrl, createHttpClient())

    suspend fun getAll(): List<Genre> =
        client.get("$baseUrl/api/genre/all").body()

    companion object {
        const val DEFAULT_BASE_URL = "http://localhost:8081"

        private fun createHttpClient() = HttpClient {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object GenreClient {
    suspend fun getAll(): Array<Genre> = GenreApi().getAll().toTypedArray()
}

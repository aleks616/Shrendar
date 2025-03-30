package org.aleks616.shrendar

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

actual class NetworkClient actual constructor() {
    private val client=HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    actual suspend fun fetchRanks():List<Ranks>=
        client.get("http://localhost:8081/api/ranks").body()
}
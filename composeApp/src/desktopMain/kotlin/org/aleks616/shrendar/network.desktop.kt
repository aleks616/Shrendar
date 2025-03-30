package org.aleks616.shrendar

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

actual class NetworkClient actual constructor() {
    private val client=HttpClient(CIO)

    actual suspend fun fetchRanks():List<Ranks> {
        val response=client.get("http://127.0.0.1:8081/api/ranks")
        return Json.decodeFromString(response.body())
    }
}
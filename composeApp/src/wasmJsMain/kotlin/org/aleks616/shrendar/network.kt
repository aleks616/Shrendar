package org.aleks616.shrendar

import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.await
import org.w3c.fetch.Response

private val json=Json {
    ignoreUnknownKeys=true
    explicitNulls=false
}

actual class NetworkClient actual constructor() {
    actual suspend fun fetchRanks():List<Ranks> {
        val response=window.fetch("http://localhost:8081/api/ranks").await<Response>()
        val text=response.text().await<Response>()
        return json.decodeFromString(text.toString())
    }
}
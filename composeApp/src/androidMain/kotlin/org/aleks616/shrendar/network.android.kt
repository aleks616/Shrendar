package org.aleks616.shrendar

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import org.aleks616.shrendar.Ranks


actual class NetworkClient actual constructor(){
    private val client=HttpClient(OkHttp){
        engine{
            config{
                retryOnConnectionFailure(true)
            }
        }
    }

    actual suspend fun fetchRanks():List<Ranks>{
        val response=client.get("http://10.0.2.2:8081/api/ranks")
        return Json.decodeFromString(response.body())
    }
}
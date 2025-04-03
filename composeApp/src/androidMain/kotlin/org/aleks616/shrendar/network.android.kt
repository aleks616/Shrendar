package org.aleks616.shrendar

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation


actual class NetworkClient actual constructor(){
    private val client=HttpClient(OkHttp){
        engine{
            config{
                retryOnConnectionFailure(true)
            }
        }
        install(ContentNegotiation){
            json()
        }
    }

    actual suspend fun fetchRanks():List<Ranks>{
        val response=client.get("http://10.0.2.2:8081/api/ranks")
        return Json.decodeFromString(response.body())
    }

    actual suspend fun fetchUsers():List<UsersDto>{
        val response=client.get("http://10.0.2.2:8081/api/users")
        return Json.decodeFromString(response.body())
    }

    actual suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray){
        CoroutineScope(Dispatchers.IO).launch{
            val response=client.post("http://10.0.2.2:8081/api/register"){
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        login=login,
                        displayName=displayName,
                        email=email,
                        password=password.concatToString()
                    )
                )
            }
        }
    }
}
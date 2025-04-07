package org.aleks616.shrendar

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

actual class NetworkClient actual constructor() {
    private val client=HttpClient(CIO){
        install(ContentNegotiation) { json() }

    }

    actual suspend fun fetchRanks():List<Ranks> {
        val response=client.get("http://127.0.0.1:8081/api/ranks")
        return Json.decodeFromString(response.body())
    }

    actual suspend fun fetchUsers():List<UsersDto> {
        val response=client.get("http://127.0.0.1:8081/api/users")
        return Json.decodeFromString(response.body())
    }


    actual suspend fun isPasswordCorrect(email:String?,login:String?,password:CharArray):Boolean{
        val response=client.post("http://localhost:8081/api/passwordCheck"){
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(login=login,email=email,password=password.concatToString())
                )
            }
        return Json.decodeFromString(response.body())
    }

    actual suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray){
        CoroutineScope(Dispatchers.IO).launch{
            val response=client.post("http://localhost:8081/api/register"){
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
            if(!response.status.isSuccess()) {
                throw Exception(response.status.toString())
            }
        }
    }

    actual suspend fun doesLoginExist(login:String):Boolean{
        val response=client.get("http://127.0.0.1:8081/api/loginCheck?login=$login")
        return Json.decodeFromString(response.body())
    }

    actual suspend fun doesEmailExist(email:String):Boolean{
        val response=client.get("http://127.0.0.1:8081/api/emailCheck?email=$email")
        return Json.decodeFromString(response.body())
    }

}
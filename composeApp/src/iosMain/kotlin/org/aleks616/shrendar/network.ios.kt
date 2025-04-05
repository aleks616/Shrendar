package org.aleks616.shrendar

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

actual class NetworkClient actual constructor() {
    private val client=HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    actual suspend fun fetchRanks():List<Ranks> =
        client.get("http://localhost:8081/api/ranks").body()

    actual suspend fun fetchUsers():List<UsersDto> =
        client.get("http://localhost:8081/api/users").body()

    actual suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray){
        CoroutineScope(Dispatchers.IO).launch{
            val response=client.post("https://localhost:8081/api/register"){
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

    actual suspend fun doesLoginExist(login:String):Boolean=client.get("http://localhost:8081/api/loginCheck?login=$login").body()

    actual suspend fun doesEmailExist(email:String):Boolean=client.get("http://localhost:8081/api/emailCheck?email=$email").body()
}
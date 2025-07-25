package org.aleks616.shrendar

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual class NetworkClient actual constructor(){
    private val client=HttpClient(Js){
        install(ContentNegotiation){
            json(Json{ignoreUnknownKeys=true})
        }
    }


    actual suspend fun fetchRanks():List<Ranks> =
        client.get("http://127.0.0.1:8081/api/ranks").body()

    actual suspend fun fetchUsers():List<UsersDto> =
        client.get("http://127.0.0.1:8081/api/users").body()

    actual suspend fun isPasswordCorrect(email:String?,login:String?,password:CharArray):Boolean=
        client.post("http://localhost:8081/api/passwordCheck"){
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(login,email,password.concatToString()))
        }.body()

    actual suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray){
        client.post("http://localhost:8081/api/register"){
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(login,displayName,email,password.concatToString()))
        }
    }

    actual suspend fun doesLoginExist(login:String):Boolean=
        client.get("http://127.0.0.1:8081/api/loginCheck?login=$login").body()

    actual suspend fun doesEmailExist(email:String):Boolean=
        client.get("http://127.0.0.1:8081/api/emailCheck?email=$email").body()

    actual suspend fun fetchArtistsBirthdays(month:Int?,day:Int?):List<ArtistsBirthDayDto> =
        client.get("http://127.0.0.1:8081/api/artistBirthdays?month=$month&day=$day").body()
}
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.aleks616.shrendar

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.fetch.Response
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalSerializationApi::class)
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

    actual suspend fun fetchUsers():List<UsersDto> {
        val response=window.fetch("http://localhost:8081/api/users").await<Response>()
        val text=response.text().await<Response>()
        return json.decodeFromString(text.toString())
    }

    actual suspend fun doesLoginExist(login:String):Boolean{
        val response=window.fetch("http://localhost:8081/api/loginCheck?login=$login").await<Response>()
        val text=response.text().await<Response>()
        return json.decodeFromString(text.toString())
    }

    actual suspend fun doesEmailExist(email:String):Boolean{
        val response=window.fetch("http://localhost:8081/api/emailCheck?email=$email").await<Response>()
        val text=response.text().await<Response>()
        return json.decodeFromString(text.toString())
    }

    actual suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray){
        val requestBody=Json.encodeToString(
            RegisterRequest(
                login=login,
                displayName=displayName,
                email=email,
                password=password.concatToString()
            )
        )

        suspendCoroutine {continuation ->
            val xhr=XMLHttpRequest()
            xhr.open("POST","http://localhost:8081/api/register")
            xhr.setRequestHeader("Content-Type","application/json")
            xhr.onload={
                continuation.resume(xhr)
            }
            xhr.send(requestBody)
        }
    }
}
package com.example.client.account

import com.example.client.BASE_URL
import com.example.client.common.Date
import com.example.client.common.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

class AccountApi private constructor(
    private val baseUrl:String,
    private val client:HttpClient
) {
    constructor():this(BASE_URL,createHttpClient())
    internal constructor(testClient:HttpClient):this(BASE_URL,testClient)

    suspend fun login(loginRequest:LoginRequestDto):String {
        return try {
            client.post("$BASE_URL/user-account/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }.body()
        }
        catch(e:ClientRequestException) {
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    suspend fun logout(token:String):String {
        return try {
            client.post("$BASE_URL/user-account/logout") {
                contentType(ContentType.Application.Json)
                header("Authorization","Bearer $token")
            }.body()
        }
        catch(e:ClientRequestException) {
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    suspend fun requestPasswordReset(accountKey:String,language:String):String {
        return try {
            client.post("$BASE_URL/user-account/requestPasswordReset") {
                parameter("accountKey",accountKey)
                parameter("language",language)
            }.body()
        }
        catch(e:ClientRequestException) {
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    suspend fun resetPassword(passwordRequest:ResetPasswordDto):String {
        return try {
            client.post("$BASE_URL/user-account/resetPassword") {
                contentType(ContentType.Application.Json)
                setBody(passwordRequest)
            }.body()
        }
        catch(e:ClientRequestException) {
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    @OptIn(ExperimentalJsExport::class)
    suspend fun getUserData(token:String):UserDto {
        return try{
            client.get("$BASE_URL/user-account/me"){
                header("Authorization","Bearer $token")
            }.body()
        }
        catch(e:ClientRequestException) {
            return UserDto()
        }
    }

    suspend fun authWithGoogle(googleToken:String):String {
        return try {
            client.post("$BASE_URL/user-account/with-google") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("googleToken" to googleToken))
            }.body()
        }
        catch(e:ClientRequestException) {
            return e.response.bodyAsText()
        }
    }

    suspend fun updateUsername(token:String,newUsername:String):String {
        return try {
            client.post("$BASE_URL/user-account/updateUsername") {
                header("Authorization","Bearer $token")
                parameter("newUsername",newUsername)
            }.body()
        }
        catch(e:ClientRequestException) {
            return e.response.bodyAsText()
        }
    }

    suspend fun updateEmail(token:String,newEmail:String):String {
        return try {
            client.post("$BASE_URL/user-account/updateEmail") {
                header("Authorization","Bearer $token")
                parameter("newEmail",newEmail)
            }.body()
        }
        catch(e:ClientRequestException) {
            return e.response.bodyAsText()
        }
    }

    @OptIn(ExperimentalJsExport::class)
    suspend fun addBirthday(token:String,birthday:Date):String {
        val birthdayString="${birthday.year}-${if(birthday.month<10) "0${birthday.month}" else birthday.month}-${if(birthday.day<10) "0${birthday.day}" else birthday.day}"
        return try {
            client.post("$BASE_URL/user-account/addBirthday") {
                header("Authorization","Bearer $token")
                parameter("stringDate",birthdayString)
            }.body()
        }
        catch(e:ClientRequestException) {
            return e.response.bodyAsText()
        }
    }

    suspend fun deleteAccount(token:String,request:LoginRequestDto):String{
        return try {
            client.post("$BASE_URL/user-account/deleteAccount") {
                header("Authorization","Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
        catch(e:ClientRequestException) {
            return e.response.bodyAsText()
        }
    }

    companion object {
        private fun createHttpClient()=HttpClient {
            expectSuccess=true
            install(DefaultRequest) {
                header("skip_zrok_interstitial","1")
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys=true
                })
            }
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object AccountClient {
    suspend fun login(request:LoginRequestDto):String=AccountApi().login(request)
    suspend fun logout(token:String):String=AccountApi().logout(token)
    suspend fun requestPasswordReset(accountKey:String,language:String):String=AccountApi().requestPasswordReset(accountKey,language)
    suspend fun resetPassword(request:ResetPasswordDto):String=AccountApi().resetPassword(request)
    suspend fun getUserData(token:String):UserDto=AccountApi().getUserData(token)
    suspend fun authWithGoogle(googleToken:String):String=AccountApi().authWithGoogle(googleToken)
    suspend fun updateUsername(token:String,newUsername:String):String=AccountApi().updateUsername(token,newUsername)
    suspend fun updateEmail(token:String,newEmail:String):String=AccountApi().updateEmail(token,newEmail)
    suspend fun addBirthday(token:String,birthday:Date):String=AccountApi().addBirthday(token,birthday)
    suspend fun deleteAccount(token:String,request:LoginRequestDto):String=AccountApi().deleteAccount(token,request)
}
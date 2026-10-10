package com.example.client.profile

import com.example.client.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

class ProfileApi private constructor(
    private val baseUrl:String,
    private val client:HttpClient
){
    constructor():this(BASE_URL,createHttpClient())
    internal constructor(testClient:HttpClient):this(BASE_URL,testClient)

    suspend fun getUserProfile(login:String,token:String?):UserProfileDto?{
        return try{
            client.get("$BASE_URL/user/@$login"){
                header("Authorization","Bearer $token")
            }.body()
        }
        catch(_:Exception){
            null
        }
    }

    suspend fun toggleFavoriteBand(bandId:Int,token:String?):String?{
        return try{
            client.post("$BASE_URL/band/favorite"){
                header("Authorization","Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(bandId)
            }.body()
        }
        catch(e:ClientRequestException){
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    suspend fun toggleFavoriteArtist(artistId:Long,token:String?):String?{
        return try{
            client.post("$BASE_URL/artist/favorite"){
                header("Authorization","Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(artistId)
            }.body()
        }
        catch(e:ClientRequestException){
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    suspend fun toggleFavoriteGenre(genreId:Int,token:String?):String?{
        return try{
            client.post("$BASE_URL/genre/favorite"){
                header("Authorization","Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(genreId)
            }.body()
        }
        catch(e:ClientRequestException){
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    suspend fun toggleFavoriteArtistAll(bandId:Int,token:String?):String?{
        return try{
            client.post("$BASE_URL/artist/favoriteAll"){
                header("Authorization","Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(bandId)
            }.body()
        }
        catch(e:ClientRequestException){
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    suspend fun updateBio(bio:String,token:String?):String?{
        return try{
            client.post("$BASE_URL/user-account/bio/add"){
                header("Authorization","Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(bio)
            }.body()
        }
        catch(e:ClientRequestException){
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
        }
    }

    companion object {
        private fun createHttpClient()=HttpClient {
            expectSuccess=true
            install(DefaultRequest) {
                header("skip_zrok_interstitial", "1")
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys=true
                })
            }
        }
    }
}

@ExperimentalJsExport
@JsExport
object ProfileClient {
    suspend fun getUserProfile(login:String,token:String?):UserProfileDto?=ProfileApi().getUserProfile(login,token)
    suspend fun toggleFavoriteBand(bandId:Int,token:String?):String?=ProfileApi().toggleFavoriteBand(bandId,token)
    suspend fun toggleFavoriteArtist(artistId:Long,token:String?):String?=ProfileApi().toggleFavoriteArtist(artistId,token)
    suspend fun toggleFavoriteGenre(genreId:Int,token:String?):String?=ProfileApi().toggleFavoriteGenre(genreId,token)
    suspend fun toggleFavoriteArtistAll(bandId:Int,token:String?):String?=ProfileApi().toggleFavoriteArtistAll(bandId,token)
    suspend fun updateBio(bio:String,token:String?):String?=ProfileApi().updateBio(bio,token)
}
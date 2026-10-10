package com.example.client.artist

import com.example.client.BASE_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
class ArtistApi private constructor(
    private val baseUrl:String,
    private val client:HttpClient
) {
    constructor():this(BASE_URL,createHttpClient())
    internal constructor(testClient:HttpClient):this(BASE_URL,testClient)

    suspend fun getArtistWikiPageDataById(id:Long,token:String?):ArtistWikiDto{
        try{
            val response:ArtistWikiDto=client.get("$BASE_URL/artist/wiki/$id"){
                header("Authorization","Bearer $token")
            }.body()
            return response
        }
        catch(e:ClientRequestException){
            if(e.response.status.value==404) throw Exception("not_found")
            else throw Exception(e.response.bodyAsText())
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
object ArtistClient {
    suspend fun getArtistWikiPageDataById(id:Long,token:String?):ArtistWikiDto=ArtistApi().getArtistWikiPageDataById(id,token)
}
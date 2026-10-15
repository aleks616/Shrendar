package com.example.client.homepage

import com.example.client.BASE_URL
import com.example.client.homepage.dto.*
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
class HomepageApi private constructor(
    private val baseUrl:String,
    private val client:HttpClient
) {
    constructor():this(BASE_URL,createHttpClient())
    internal constructor(testClient:HttpClient):this(BASE_URL,testClient)


    suspend fun getTodayAnniversaries(token:String?):HomepageMainDto {
        try {
            return client.get("$BASE_URL/main/") {
                if(token!=null) {
                    header("Authorization","Bearer $token")
                }
            }.body()
        }
        catch(e:ClientRequestException) {
            throw Exception(e.response.bodyAsText())
        }
    }

    suspend fun getUpcoming(token:String?):UpcomingAnniversariesDto {
        try {
            return client.get("$BASE_URL/main/upcoming") {
                if(token!=null) {
                    header("Authorization","Bearer $token")
                }
            }.body()
        }
        catch(e:ClientRequestException) {
            throw Exception(e.response.bodyAsText())
        }
    }

    suspend fun getCommonBands(token:String):CommonBandsDto {
        try {
            return client.get("$BASE_URL/main/commonBands") {
                header("Authorization","Bearer $token")
            }.body()
        }
        catch(e:ClientRequestException) {
            throw Exception(e.response.bodyAsText())
        }
    }

    suspend fun getRecentlyAdded(token:String):List<RecentlyAddedDto> {
        try {
            return client.get("$BASE_URL/main/recentlyAdded") {
                header("Authorization","Bearer $token")
            }.body()
        }
        catch(e:ClientRequestException) {
            throw Exception(e.response.bodyAsText())
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
object HomepageClient {
    suspend fun getTodayAnniversaries(token:String?):HomepageMainDto=HomepageApi().getTodayAnniversaries(token)
    suspend fun getUpcoming(token:String?):UpcomingAnniversariesDto=HomepageApi().getUpcoming(token)
    suspend fun getCommonBands(token:String):CommonBandsDto =HomepageApi().getCommonBands(token)
    suspend fun getRecentlyAdded(token:String):List<RecentlyAddedDto> =HomepageApi().getRecentlyAdded(token)
}
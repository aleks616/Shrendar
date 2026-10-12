package com.example.client.report

import com.example.client.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
@OptIn(ExperimentalJsExport::class)
class ReportApi private constructor(
    private val baseUrl:String,
    private val client:HttpClient
) {
    constructor():this(BASE_URL,createHttpClient())
    internal constructor(testClient:HttpClient):this(BASE_URL,testClient)

    suspend fun canReport(userId:Int,token:String):Boolean{
        return try{
            client.post("$BASE_URL/report/check"){
                header("Authorization","Bearer $token")
                setBody(userId)
            }.body()
        }
        catch(_:Exception){
            false
        }
    }

    suspend fun reportUser(report:ReportRequestDto,token:String?):String{
        return try{
            client.post("$BASE_URL/report/user"){
                header("Authorization","Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(report)
            }.body()
        }
        catch(e:ClientRequestException) {
            if(e.response.status.value==404) "not_found"
            else e.response.bodyAsText()
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
object ReportClient {
    suspend fun canReport(userId:Int,token:String)=ReportApi().canReport(userId,token)
    suspend fun reportUser(report:ReportRequestDto,token:String?)=ReportApi().reportUser(report,token)
}
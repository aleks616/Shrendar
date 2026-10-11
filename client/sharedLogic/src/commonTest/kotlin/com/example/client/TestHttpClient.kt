package com.example.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.statement.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun mockClient(
    handler:suspend MockRequestHandleScope.(HttpRequestData)->HttpResponseData
):HttpClient {
    return HttpClient(MockEngine(handler)) {
        expectSuccess=true
        install(ContentNegotiation) {
            json(Json {ignoreUnknownKeys=true})
        }
    }
}

internal fun MockRequestHandleScope.respondText(
    text:String,
    status:HttpStatusCode=HttpStatusCode.OK
):HttpResponseData {
    return respond(
        content=text,
        status=status,
        headers=headersOf(HttpHeaders.ContentType,ContentType.Text.Plain.toString())
    )
}

internal fun MockRequestHandleScope.respondJson(
    text:String,
    status:HttpStatusCode=HttpStatusCode.OK
):HttpResponseData {
    return respond(
        content=text,
        status=status,
        headers=headersOf(HttpHeaders.ContentType,ContentType.Application.Json.toString())
    )
}

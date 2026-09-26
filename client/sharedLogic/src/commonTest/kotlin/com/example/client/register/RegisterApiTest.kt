package com.example.client.register

import com.example.client.BASE_URL
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegisterApiTest {
    @Test
    fun doesEmailExistReturnsTheServerResultAndSendsTheEmail()=runTest {
        var request:HttpRequestData?=null
        val client=client {capturedRequest->
            request=capturedRequest
            respondJson("true")
        }

        try {
            assertTrue(RegisterApi(client).doesEmailExist("alice@example.com"))
            assertEquals(HttpMethod.Get,request?.method)
            assertEquals("$BASE_URL/user-account/emailCheck",request?.url?.toString()?.substringBefore("?"))
            assertEquals("alice@example.com",request?.url?.parameters?.get("email"))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun doesEmailExistReturnsFalseForAClientError()=runTest {
        val client=client {
            respondJson("failure",HttpStatusCode.BadRequest)
        }

        try {
            assertFalse(RegisterApi(client).doesEmailExist("alice@example.com"))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun doesLoginExistReturnsTheServerResultAndSendsTheLogin()=runTest {
        var request:HttpRequestData?=null
        val client=client {capturedRequest->
            request=capturedRequest
            respondJson("false")
        }

        try {
            assertFalse(RegisterApi(client).doesLoginExist("alice"))
            assertEquals(HttpMethod.Get,request?.method)
            assertEquals("$BASE_URL/user-account/loginCheck",request?.url?.toString()?.substringBefore("?"))
            assertEquals("alice",request?.url?.parameters?.get("login"))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun doesLoginExistReturnsFalseForAClientError()=runTest {
        val client=client {
            respondJson("failure",HttpStatusCode.BadRequest)
        }

        try {
            assertFalse(RegisterApi(client).doesLoginExist("alice"))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun registerSendsTheRequestAndReturnsTheResponse()=runTest {
        var request:HttpRequestData?=null
        val client=client {capturedRequest->
            request=capturedRequest
            respondText("verification_code_sent")
        }

        try {
            assertEquals("verification_code_sent",RegisterApi(client).register(sampleRequest()))
            assertEquals(HttpMethod.Post,request?.method)
            assertEquals("$BASE_URL/user-account/register",request?.url?.toString())
            assertTrue((request?.body as TextContent).text.contains("\"displayName\":\"Alice\""))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun registerReturnsNotFoundForANotFoundResponse()=runTest {
        val client=client {
            respondText("missing",HttpStatusCode.NotFound)
        }

        try {
            assertEquals("not_found",RegisterApi(client).register(sampleRequest()))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun registerReturnsTheResponseBodyForOtherClientErrors()=runTest {
        val client=client {
            respondText("email already exists",HttpStatusCode.BadRequest)
        }

        try {
            assertEquals(
                "email already exists",
                RegisterApi(client).register(sampleRequest())
            )
        }
        finally {
            client.close()
        }
    }

    @Test
    fun registerConfirmSendsTheCodeAndReturnsTheResponse()=runTest {
        var request:HttpRequestData?=null
        val client=client {capturedRequest->
            request=capturedRequest
            respondText("account_created")
        }

        try {
            assertEquals(
                "account_created",
                RegisterApi(client).registerConfirm(sampleRequest(),"123456")
            )
            assertEquals(HttpMethod.Post,request?.method)
            assertEquals(
                "$BASE_URL/user-account/register/confirm",
                request?.url?.toString()?.substringBefore("?")
            )
            assertEquals("123456",request?.url?.parameters?.get("code"))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun registerConfirmReturnsNotFoundForANotFoundResponse()=runTest {
        val client=client {
            respondText("missing",HttpStatusCode.NotFound)
        }

        try {
            assertEquals(
                "not_found",
                RegisterApi(client).registerConfirm(sampleRequest(),"123456")
            )
        }
        finally {
            client.close()
        }
    }

    private fun sampleRequest()=RegisterRequestDto(
        login="alice",
        displayName="Alice",
        email="alice@example.com",
        password="Password1!"
    )

    private fun client(
        handler:suspend MockRequestHandleScope.(HttpRequestData)->HttpResponseData
    ):HttpClient {
        return HttpClient(MockEngine(handler)) {
            expectSuccess=true
            install(ContentNegotiation) {
                json(Json {ignoreUnknownKeys=true})
            }
        }
    }

    private fun MockRequestHandleScope.respondJson(
        body:String,
        status:HttpStatusCode=HttpStatusCode.OK
    ):HttpResponseData {
        return respond(
            content=body,
            status=status,
            headers=headersOf(
                HttpHeaders.ContentType,
                ContentType.Application.Json.toString()
            )
        )
    }

    private fun MockRequestHandleScope.respondText(
        text:String,
        status:HttpStatusCode=HttpStatusCode.OK
    ):HttpResponseData {
        return respond(
            content=text,
            status=status,
            headers=io.ktor.http.headersOf(
                HttpHeaders.ContentType,
                ContentType.Text.Plain.toString()
            )
        )
    }
}

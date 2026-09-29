package com.example.client.account

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
import kotlin.test.assertTrue

class AccountApiTest {
    @Test
    fun loginSendsTheLoginRequestAndReturnsTheResponse()=runTest {
        var request:HttpRequestData?=null
        val client=client {capturedRequest->
            request=capturedRequest
            respondText("login_success")
        }

        try {
            val result=AccountApi(client).login(
                LoginRequestDto("alice",null,"Password1!")
            )

            assertEquals("login_success",result)
            assertEquals(HttpMethod.Post,request?.method)
            assertEquals("$BASE_URL/user-account/login",request?.url?.toString())
            assertTrue((request?.body as TextContent).text.contains("\"login\":\"alice\""))
        }
        finally {
            client.close()
        }
    }

    @Test
    fun loginReturnsNotFoundForANotFoundResponse()=runTest {
        val client=client {
            respondText("missing",HttpStatusCode.NotFound)
        }

        try {
            assertEquals(
                "not_found",AccountApi(client).login(LoginRequestDto(null,"alice@example.com","Password1!"))
            )
        }
        finally {
            client.close()
        }
    }

    @Test
    fun loginReturnsTheResponseBodyForOtherClientErrors()=runTest {
        val client=client {
            respondText("invalid credentials",HttpStatusCode.BadRequest)
        }

        try {
            assertEquals(
                "invalid credentials",AccountApi(client).login(LoginRequestDto("alice",null,"wrong"))
            )
        }
        finally {
            client.close()
        }
    }

    @Test
    fun logoutSendsTheLogoutRequestAndReturnsTheResponse()=runTest {
        var request:HttpRequestData?=null
        val client=client {capturedRequest->
            request=capturedRequest
            respondText("logged_out")
        }

        try {
            assertEquals("logged_out",AccountApi(client).logout("token"))
            assertEquals(HttpMethod.Post,request?.method)
            assertEquals("$BASE_URL/user-account/logout",request?.url?.toString())
        }
        finally {
            client.close()
        }
    }

    @Test
    fun logoutReturnsNotFoundForANotFoundResponse()=runTest {
        val client=client {
            respondText("missing",HttpStatusCode.NotFound)
        }

        try {
            assertEquals("not_found",AccountApi(client).logout("token"))
        }
        finally {
            client.close()
        }
    }


    @Test
    fun requestPasswordResetSendsTheAccountAndLanguageAndReturnsTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=client {capturedRequest->
                request=capturedRequest
                respondText("password_link_sent")
            }

            try {
                assertEquals(
                    "password_link_sent",AccountApi(client).requestPasswordReset("alice@example.com","EN")
                )
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/user-account/requestPasswordReset",request?.url?.encodedPath)
                assertEquals("alice@example.com",request?.url?.parameters?.get("accountKey"))
                assertEquals("EN",request?.url?.parameters?.get("language"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun requestPasswordResetReturnsNotFoundForANotFoundResponse() {
        runTest {
            val client=client {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertEquals("not_found",AccountApi(client).requestPasswordReset("alice","EN"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun requestPasswordResetReturnsOtherClientErrors() {
        runTest {
            val client=client {
                respondText("too_many_user_requests",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals(
                    "too_many_user_requests",AccountApi(client).requestPasswordReset("alice","EN")
                )
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun resetPasswordSendsThePasswordRequestAndReturnsTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=client {capturedRequest->
                request=capturedRequest
                respondText("password_changed")
            }

            try {
                assertEquals(
                    "password_changed",AccountApi(client).resetPassword(
                        ResetPasswordDto("alice@example.com","NewPassword1!","123456","EN")
                    )
                )
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/user-account/resetPassword",request?.url?.encodedPath)
                val body=(request?.body as TextContent).text
                assertTrue(body.contains("\"email\":\"alice@example.com\""))
                assertTrue(body.contains("\"newPassword\":\"NewPassword1!\""))
                assertTrue(body.contains("\"code\":\"123456\""))
                assertTrue(body.contains("\"language\":\"EN\""))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun resetPasswordReturnsNotFoundForANotFoundResponse() {
        runTest {
            val client=client {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertEquals(
                    "not_found",AccountApi(client).resetPassword(
                        ResetPasswordDto("alice@example.com","NewPassword1!","123456","EN")
                    )
                )
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun resetPasswordReturnsOtherClientErrors() {
        runTest {
            val client=client {
                respondText("invalid_code",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals(
                    "invalid_code",AccountApi(client).resetPassword(
                        ResetPasswordDto("alice@example.com","NewPassword1!","123456","EN")
                    )
                )
            }
            finally {
                client.close()
            }
        }
    }

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

    private fun MockRequestHandleScope.respondText(
        text:String,status:HttpStatusCode=HttpStatusCode.OK
    ):HttpResponseData {
        return respond(
            content=text,status=status,headers=headersOf(
                HttpHeaders.ContentType,ContentType.Text.Plain.toString()
            )
        )
    }
}

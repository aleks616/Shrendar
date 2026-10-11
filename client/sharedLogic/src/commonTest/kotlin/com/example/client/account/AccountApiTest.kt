package com.example.client.account

import com.example.client.common.Date
import com.example.client.mockClient
import com.example.client.respondJson
import com.example.client.respondText
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountApiTest {
    @Test
    fun loginSendsTheLoginRequestAndReturnsTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("login_success")
            }

            try {
                val result=AccountApi(client).login(
                    LoginRequestDto("alice",null,"Password1!")
                )

                assertEquals("login_success",result)
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("http://localhost:9876/user-account/login",request?.url?.toString())
                assertTrue((request?.body as TextContent).text.contains("\"login\":\"alice\""))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun loginReturnsNotFoundForANotFoundResponse() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertEquals(
                    "not_found",
                    AccountApi(client).login(LoginRequestDto(null,"alice@example.com","Password1!"))
                )
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun loginReturnsTheResponseBodyForOtherClientErrors() {
        runTest {
            val client=mockClient {
                respondText("invalid credentials",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals(
                    "invalid credentials",
                    AccountApi(client).login(LoginRequestDto("alice",null,"wrong"))
                )
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun logoutSendsTheLogoutRequestAndReturnsTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("logged_out")
            }

            try {
                assertEquals("logged_out",AccountApi(client).logout("token"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("http://localhost:9876/user-account/logout",request?.url?.toString())
                assertEquals("******",request?.headers?.get("Authorization"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun logoutReturnsNotFoundForANotFoundResponse() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertEquals("not_found",AccountApi(client).logout("token"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun requestPasswordResetSendsTheAccountAndLanguageAndReturnsTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("password_link_sent")
            }

            try {
                assertEquals(
                    "password_link_sent",
                    AccountApi(client).requestPasswordReset("alice@example.com","EN")
                )
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/requestPasswordReset",request?.url?.encodedPath)
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
            val client=mockClient {
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
            val client=mockClient {
                respondText("too_many_user_requests",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals(
                    "too_many_user_requests",
                    AccountApi(client).requestPasswordReset("alice","EN")
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
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("password_changed")
            }

            try {
                assertEquals(
                    "password_changed",
                    AccountApi(client).resetPassword(
                        ResetPasswordDto("alice@example.com","NewPassword1!","123456","EN")
                    )
                )
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/resetPassword",request?.url?.encodedPath)
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
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertEquals(
                    "not_found",
                    AccountApi(client).resetPassword(
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
            val client=mockClient {
                respondText("invalid_code",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals(
                    "invalid_code",
                    AccountApi(client).resetPassword(
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
    fun getUserDataSendsTheAuthorizedRequestAndParsesTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondJson(
                    """
                    {"login":"alice","username":"Alice","email":"alice@example.com","birthDate":{"year":1999,"month":5,"day":20},"rankId":2,"xp":50}
                    """.trimIndent()
                )
            }

            try {
                val result=AccountApi(client).getUserData("token")

                assertEquals(HttpMethod.Get,request?.method)
                assertEquals("http://localhost:9876/user-account/me",request?.url?.toString())
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("alice",result.login)
                assertEquals("Alice",result.username)
                assertEquals("alice@example.com",result.email)
                assertEquals(Date(1999,5,20),result.birthDate)
                assertEquals(2,result.rankId)
                assertEquals(50,result.xp)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun getUserDataReturnsAnEmptyUserWhenTheRequestFails() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                val result=AccountApi(client).getUserData("token")

                assertNull(result.login)
                assertNull(result.username)
                assertNull(result.email)
                assertNull(result.birthDate)
                assertNull(result.rankId)
                assertNull(result.xp)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun authWithGoogleSendsTheGoogleCredentialAndReturnsTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("{\"token\":\"jwt\"}")
            }

            try {
                val result=AccountApi(client).authWithGoogle("google-credential")

                assertEquals("{\"token\":\"jwt\"}",result)
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/with-google",request?.url?.encodedPath)
                assertTrue((request?.body as TextContent).text.contains("\"googleToken\":\"google-credential\""))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun authWithGoogleReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("authorization_failed",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals("authorization_failed",AccountApi(client).authWithGoogle("bad-token"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun updateUsernameSendsTheNewUsernameAsARequestParameter() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("username_changed")
            }

            try {
                assertEquals("username_changed",AccountApi(client).updateUsername("token","new-user"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/updateUsername",request?.url?.encodedPath)
                assertEquals("new-user",request?.url?.parameters?.get("newUsername"))
                assertEquals("******",request?.headers?.get("Authorization"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun updateUsernameReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("username_taken",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals("username_taken",AccountApi(client).updateUsername("token","taken"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun updateEmailSendsTheNewEmailAsARequestParameter() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("email_changed")
            }

            try {
                assertEquals("email_changed",AccountApi(client).updateEmail("token","new@example.com"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/updateEmail",request?.url?.encodedPath)
                assertEquals("new@example.com",request?.url?.parameters?.get("newEmail"))
                assertEquals("******",request?.headers?.get("Authorization"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun updateEmailReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("new_email_exists",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals("new_email_exists",AccountApi(client).updateEmail("token","used@example.com"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun addBirthdayFormatsTheDateWithLeadingZeroes() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("birthday_added")
            }

            try {
                assertEquals("birthday_added",AccountApi(client).addBirthday("token",Date(1995,2,3)))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/addBirthday",request?.url?.encodedPath)
                assertEquals("1995-02-03",request?.url?.parameters?.get("stringDate"))
                assertEquals("******",request?.headers?.get("Authorization"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun addBirthdayReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("invalid_user_birthdate",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals(
                    "invalid_user_birthdate",
                    AccountApi(client).addBirthday("token",Date(2019,1,1))
                )
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun deleteAccountSendsTheLanguageAndCredentials() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("confirmed")
            }

            try {
                val result=AccountApi(client).deleteAccount(
                    "token",
                    LoginRequestDto("alice",null,"Password1!"),
                    "EN"
                )

                assertEquals("confirmed",result)
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/deleteAccount",request?.url?.encodedPath)
                assertEquals("EN",request?.url?.parameters?.get("lang"))
                assertEquals("******",request?.headers?.get("Authorization"))
                val body=(request?.body as TextContent).text
                assertTrue(body.contains("\"login\":\"alice\""))
                assertTrue(body.contains("\"password\":\"Password1!\""))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun deleteAccountReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("invalid_credentials",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals(
                    "invalid_credentials",
                    AccountApi(client).deleteAccount(
                        "token",
                        LoginRequestDto("alice",null,"wrong"),
                        "EN"
                    )
                )
            }
            finally {
                client.close()
            }
        }
    }
}

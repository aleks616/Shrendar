package com.example.client.common

import com.example.client.mockClient
import com.example.client.respondText
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LlmApiTest {
    @Test
    fun translateSendsTheTranslationRequestAndReturnsTheTranslatedText() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("Przetłumaczony opis")
            }

            try {
                val result=LlmApi(client).translate(TranslationRequestDto("Original","PL"))

                assertEquals("Przetłumaczony opis",result)
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/ai/translate",request?.url?.encodedPath)
                val body=(request?.body as TextContent).text
                assertTrue(body.contains("\"text\":\"Original\""))
                assertTrue(body.contains("\"targetLanguage\":\"PL\""))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun translateThrowsNotFoundWhenTheBackendReturns404() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                val error=assertFailsWith<Exception> {
                    LlmApi(client).translate(TranslationRequestDto("Original","PL"))
                }

                assertEquals("not_found",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun translateThrowsTheResponseBodyForOtherClientErrors() {
        runTest {
            val client=mockClient {
                respondText("quota_exceeded",HttpStatusCode.BadRequest)
            }

            try {
                val error=assertFailsWith<Exception> {
                    LlmApi(client).translate(TranslationRequestDto("Original","PL"))
                }

                assertEquals("quota_exceeded",error.message)
            }
            finally {
                client.close()
            }
        }
    }
}

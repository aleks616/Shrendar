package com.example.client.profile

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

class ProfileApiTest {
    @Test
    fun getUserProfileSendsTheProfileRequestAndParsesTheResponse() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondJson(
                    """
                    {
                      "login":"alice",
                      "username":"Alice",
                      "rankId":3,
                      "xp":130,
                      "bio":"Metal fan",
                      "accountAge":"2 years",
                      "lastLogin":"today",
                      "favoriteBands":[{"id":21,"name":"Nightwish","country":"FINLAND","activeYears":"1996 - present"}],
                      "favoriteArtists":[{"id":144,"name":"Floor Jansen","bands":[]}],
                      "favoriteGenres":[{"id":7,"name":"Symphonic metal"}],
                      "contributions":[{"id":1,"changeId":2,"userId":3,"action":"EDIT","changedTable":"band","changedColumn":"name","changedRecordId":4,"oldValue":"Old","newValue":"New","changedAt":"today","confirmed":true,"confirmedBy":5}],
                      "user":true
                    }
                    """.trimIndent()
                )
            }

            try {
                val result=ProfileApi(client).getUserProfile("alice","token")

                assertEquals(HttpMethod.Get,request?.method)
                assertEquals("http://localhost:9876/user/@alice",request?.url?.toString())
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("alice",result?.login)
                assertEquals("Alice",result?.username)
                assertEquals(3,result?.rankId)
                assertEquals(130,result?.xp)
                assertEquals("Metal fan",result?.bio)
                assertEquals("Nightwish",result?.favoriteBands?.single()?.name)
                assertEquals(21,result?.favoriteBands?.single()?.id)
                assertEquals(144L,result?.favoriteArtists?.single()?.id)
                assertEquals("Symphonic metal",result?.favoriteGenres?.single()?.name)
                assertEquals("band",result?.contributions?.single()?.changedTable)
                assertEquals(true,result?.user)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun getUserProfileReturnsNullWhenTheRequestFails() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertNull(ProfileApi(client).getUserProfile("ghost","token"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteBandSendsTheBandIdentifierAsJson() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("band_toggled")
            }

            try {
                assertEquals("band_toggled",ProfileApi(client).toggleFavoriteBand(21,"token"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/band/favorite",request?.url?.encodedPath)
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("21",(request?.body as TextContent).text)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteBandReturnsNotFoundForANotFoundResponse() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertEquals("not_found",ProfileApi(client).toggleFavoriteBand(21,"token"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteArtistSendsTheArtistIdentifierAsJson() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("artist_toggled")
            }

            try {
                assertEquals("artist_toggled",ProfileApi(client).toggleFavoriteArtist(144L,"token"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/artist/favorite",request?.url?.encodedPath)
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("144",(request?.body as TextContent).text)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteArtistReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("artist_not_exist",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals("artist_not_exist",ProfileApi(client).toggleFavoriteArtist(144L,"token"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteGenreSendsTheGenreIdentifierAsJson() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("genre_toggled")
            }

            try {
                assertEquals("genre_toggled",ProfileApi(client).toggleFavoriteGenre(7,"token"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/genre/favorite",request?.url?.encodedPath)
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("7",(request?.body as TextContent).text)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteGenreReturnsNotFoundForANotFoundResponse() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                assertEquals("not_found",ProfileApi(client).toggleFavoriteGenre(7,"token"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteArtistAllSendsTheBandIdentifierAsJson() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("artist_toggled")
            }

            try {
                assertEquals("artist_toggled",ProfileApi(client).toggleFavoriteArtistAll(21,"token"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/artist/favoriteAll",request?.url?.encodedPath)
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("21",(request?.body as TextContent).text)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun toggleFavoriteArtistAllReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("band_not_exist",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals("band_not_exist",ProfileApi(client).toggleFavoriteArtistAll(21,"token"))
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun updateBioSendsTheBioAsJson() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondText("bio_added")
            }

            try {
                assertEquals("bio_added",ProfileApi(client).updateBio("Updated bio","token"))
                assertEquals(HttpMethod.Post,request?.method)
                assertEquals("/api/user-account/bio/add",request?.url?.encodedPath)
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("\"Updated bio\"",(request?.body as TextContent).text)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun updateBioReturnsTheResponseBodyForClientErrors() {
        runTest {
            val client=mockClient {
                respondText("bio_too_long",HttpStatusCode.BadRequest)
            }

            try {
                assertEquals("bio_too_long",ProfileApi(client).updateBio("x","token"))
            }
            finally {
                client.close()
            }
        }
    }
}

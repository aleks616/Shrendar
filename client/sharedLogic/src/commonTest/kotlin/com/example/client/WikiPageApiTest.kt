package com.example.client

import com.example.client.album.AlbumApi
import com.example.client.artist.ArtistApi
import com.example.client.artist.ChineseZodiacSign
import com.example.client.artist.ZodiacSign
import com.example.client.band.BandApi
import com.example.client.event.EventApi
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WikiPageApiTest {
    @Test
    fun albumApiLoadsAlbumData() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondJson(
                    """
                    {"id":255,"albumName":"Once","band":{"id":21,"name":"Nightwish"},"releaseDate":"2004-06-07","albumAge":20,"daysTillAnniversary":5,"type":"Studio","genre":{"id":7,"name":"Symphonic metal","properties":"{}"},"description":"Classic album","artworkUrl":"https://example.com/once.jpg","importance":1}
                    """.trimIndent()
                )
            }

            try {
                val result=AlbumApi(client).getAlbumWikiPageData(255)

                assertEquals(HttpMethod.Get,request?.method)
                assertEquals("http://localhost:9876/album/wiki/255",request?.url?.toString())
                assertEquals("Once",result.albumName)
                assertEquals("Nightwish",result.band?.name)
                assertEquals("2004-06-07",result.releaseDate.toString())
                assertEquals("Symphonic metal",result.genre?.name)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun albumApiThrowsNotFoundFor404Responses() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                val error=assertFailsWith<Exception> {
                    AlbumApi(client).getAlbumWikiPageData(255)
                }

                assertEquals("not_found",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun albumApiThrowsTheResponseBodyForOtherClientErrors() {
        runTest {
            val client=mockClient {
                respondText("album_id_not_exist",HttpStatusCode.BadRequest)
            }

            try {
                val error=assertFailsWith<Exception> {
                    AlbumApi(client).getAlbumWikiPageData(255)
                }

                assertEquals("album_id_not_exist",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun artistApiLoadsArtistData() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondJson(
                    """
                    {"id":144,"name":"Floor Jansen","birthDate":"1981-02-21","daysTillBirthday":12,"age":43,"gender":"FEMALE","country":"NETHERLANDS","zodiacSign":"PISCES","chineseZodiacSign":"ROOSTER","description":"Singer","artistImageUrl":"https://example.com/floor.jpg","bands":[{"memberId":1,"bandId":21,"bandName":"Nightwish","yearRole":["2013- vocals"]}],"favorite":true}
                    """.trimIndent()
                )
            }

            try {
                val result=ArtistApi(client).getArtistWikiPageDataById(144L,"token")

                assertEquals(HttpMethod.Get,request?.method)
                assertEquals("http://localhost:9876/artist/wiki/144",request?.url?.toString())
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("Floor Jansen",result.name)
                assertEquals("1981-02-21",result.birthDate.toString())
                assertEquals(ZodiacSign.PISCES,result.zodiacSign)
                assertEquals(ChineseZodiacSign.ROOSTER,result.chineseZodiacSign)
                assertEquals("Nightwish",result.bands?.single()?.bandName)
                assertEquals(true,result.favorite)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun artistApiThrowsNotFoundFor404Responses() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                val error=assertFailsWith<Exception> {
                    ArtistApi(client).getArtistWikiPageDataById(144L,"token")
                }

                assertEquals("not_found",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun artistApiThrowsTheResponseBodyForOtherClientErrors() {
        runTest {
            val client=mockClient {
                respondText("artist_not_exist",HttpStatusCode.BadRequest)
            }

            try {
                val error=assertFailsWith<Exception> {
                    ArtistApi(client).getArtistWikiPageDataById(144L,"token")
                }

                assertEquals("artist_not_exist",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun bandApiLoadsBandData() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondJson(
                    """
                    {"name":"Nightwish","formedYear":1996,"status":"Active","country":"FINLAND","description":"Symphonic metal band","imageUrl":"https://example.com/nightwish.jpg","computedGenres":[{"id":7,"name":"Symphonic metal"}],"bandMembers":[{"id":1,"artistId":144,"artistName":"Floor Jansen","bandId":21,"yearRole":["2013- vocals"]}],"albums":[{"id":255,"title":"Once","releaseDate":"2004-06-07","type":"Studio","importance":1,"genreId":7,"genreName":"Symphonic metal"}],"similar":[{"id":77,"name":"Epica","formedYear":2002,"country":"NETHERLANDS","similarity":0.82}],"favorite":true}
                    """.trimIndent()
                )
            }

            try {
                val result=BandApi(client).getBandWikiPageDataById(21,"token")

                assertEquals(HttpMethod.Get,request?.method)
                assertEquals("http://localhost:9876/band/wiki/21",request?.url?.toString())
                assertEquals("******",request?.headers?.get("Authorization"))
                assertEquals("Nightwish",result.name)
                assertEquals("Symphonic metal",result.computedGenres?.single()?.name)
                assertEquals("Floor Jansen",result.bandMembers?.single()?.artistName)
                assertEquals("Once",result.albums?.single()?.title)
                assertEquals("Epica",result.similar?.single()?.name)
                assertEquals(true,result.favorite)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun bandApiThrowsNotFoundFor404Responses() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                val error=assertFailsWith<Exception> {
                    BandApi(client).getBandWikiPageDataById(21,"token")
                }

                assertEquals("not_found",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun bandApiThrowsTheResponseBodyForOtherClientErrors() {
        runTest {
            val client=mockClient {
                respondText("band_not_exist",HttpStatusCode.BadRequest)
            }

            try {
                val error=assertFailsWith<Exception> {
                    BandApi(client).getBandWikiPageDataById(21,"token")
                }

                assertEquals("band_not_exist",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun eventApiLoadsEventData() {
        runTest {
            var request:HttpRequestData?=null
            val client=mockClient {capturedRequest->
                request=capturedRequest
                respondJson(
                    """
                    {"id":2,"bandId":21,"bandName":"Nightwish","date":"2005-08-01","daysTillAnniversary":9,"name":"Wacken 2005","description":"Festival appearance","yearsSince":19}
                    """.trimIndent()
                )
            }

            try {
                val result=EventApi(client).getEventData(2)

                assertEquals(HttpMethod.Get,request?.method)
                assertEquals("http://localhost:9876/event/2",request?.url?.toString())
                assertEquals("Wacken 2005",result.name)
                assertEquals("Nightwish",result.bandName)
                assertEquals("2005-08-01",result.date.toString())
                assertEquals(19,result.yearsSince)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun eventApiThrowsNotFoundFor404Responses() {
        runTest {
            val client=mockClient {
                respondText("missing",HttpStatusCode.NotFound)
            }

            try {
                val error=assertFailsWith<Exception> {
                    EventApi(client).getEventData(2)
                }

                assertEquals("not_found",error.message)
            }
            finally {
                client.close()
            }
        }
    }

    @Test
    fun eventApiThrowsTheResponseBodyForOtherClientErrors() {
        runTest {
            val client=mockClient {
                respondText("event_not_exist",HttpStatusCode.BadRequest)
            }

            try {
                val error=assertFailsWith<Exception> {
                    EventApi(client).getEventData(2)
                }

                assertEquals("event_not_exist",error.message)
            }
            finally {
                client.close()
            }
        }
    }
}

package org.aleks616.shrendar.genre.controller

import jakarta.servlet.http.HttpServletRequest
import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.service.GenreService
import org.aleks616.shrendar.security.RateLimiter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class GenreControllerTest {
    private val genreService=mock(GenreService::class.java)
    private val rateLimiter=mock(RateLimiter::class.java)
    private val controller=GenreController(genreService,rateLimiter)
    private val mockMvc:MockMvc=MockMvcBuilders.standaloneSetup(controller).build()
    private val request=mock(HttpServletRequest::class.java)

    @BeforeEach
    fun setup() {
        SecurityContextHolder.getContext().authentication=
            UsernamePasswordAuthenticationToken("user",null,emptyList())
        `when`(request.remoteAddr).thenReturn("127.0.0.1")
        `when`(rateLimiter.allowRequest(anyString(),eq(Utils.LIMIT_HIGH),eq(60))).thenReturn(true)
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getGenre returns all genres`() {
        val genres=listOf(Genre().apply {id=1; name="Rock"})
        `when`(genreService.getAll()).thenReturn(genres)

        mockMvc.get("/api/genre/all").andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'Rock','properties':'0000000'}]")}
        }
    }

    @Test
    fun `getBandAlbumGenresList returns genres for band`() {
        val genres=listOf(GenreDto(id=1,name="Rock",value=8))
        `when`(genreService.getBandAlbumGenresList(4)).thenReturn(genres)

        mockMvc.get("/api/genre/allAlbums/4").andExpect {
            status {isOk()}
            content {json("[{'id':1,'name':'Rock','value':8}]")}
        }
    }

    @Test
    fun `favoriteGenre should toggle existing genre successfully`() {
        `when`(genreService.doesGenreExist(1)).thenReturn(true)

        val result=controller.favoriteGenre(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        assertEquals("Genre favorite toggled successfully",result.body)
        verify(genreService).toggleFavoriteGenre(1,"user")
    }

    @Test
    fun `favoriteGenre should work if IP is unknown`() {
        `when`(genreService.doesGenreExist(1)).thenReturn(true)
        `when`(request.remoteAddr).thenReturn(null)
        `when`(rateLimiter.allowRequest("reg:ip:unknown",Utils.LIMIT_HIGH,60)).thenReturn(true)

        val result=controller.favoriteGenre(1,request)

        assertEquals(HttpStatus.OK,result.statusCode)
        verify(rateLimiter).allowRequest("reg:ip:unknown",Utils.LIMIT_HIGH,60)
    }

    @Test
    fun `favoriteGenre should reject unauthenticated request`() {
        SecurityContextHolder.clearContext()

        val result=controller.favoriteGenre(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        verifyNoInteractions(rateLimiter,genreService)
    }

    @Test
    fun `favoriteGenre should reject request when IP rate limit is reached`() {
        `when`(rateLimiter.allowRequest("reg:ip:127.0.0.1",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=controller.favoriteGenre(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this IP",result.body)
        verifyNoInteractions(genreService)
    }

    @Test
    fun `favoriteGenre should reject request when login rate limit is reached`() {
        `when`(rateLimiter.allowRequest("login:acct:user",Utils.LIMIT_HIGH,60)).thenReturn(false)

        val result=controller.favoriteGenre(1,request)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS,result.statusCode)
        assertEquals("Too many requests from this user",result.body)
        verify(genreService,never()).doesGenreExist(anyInt())
    }

    @Test
    fun `favoriteGenre should reject unknown genre`() {
        `when`(genreService.doesGenreExist(1)).thenReturn(false)

        val result=controller.favoriteGenre(1,request)

        assertEquals(HttpStatus.BAD_REQUEST,result.statusCode)
        assertEquals("Genre with id 1 does not exist",result.body)
        verify(genreService,never()).toggleFavoriteGenre(anyInt(),anyString())
    }

    @Test
    fun `favoriteGenre should return internal server error when service fails`() {
        `when`(genreService.doesGenreExist(1)).thenReturn(true)
        doThrow(IllegalStateException("broken"))
            .`when`(genreService).toggleFavoriteGenre(1,"user")

        val result=controller.favoriteGenre(1,request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,result.statusCode)
        assertEquals("An unexpected error occurred: broken",result.body)
    }
}

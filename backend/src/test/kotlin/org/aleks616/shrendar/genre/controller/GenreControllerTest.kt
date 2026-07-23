package org.aleks616.shrendar.genre.controller

import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto
import org.aleks616.shrendar.genre.service.GenreService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class GenreControllerTest {

    private val genreService:GenreService=mock(GenreService::class.java)
    private val controller=GenreController(genreService)
    private val mockMvc:MockMvc=MockMvcBuilders.standaloneSetup(controller).build()

    @Test
    fun `getGenre should return all genres`() {
        val genres=listOf(Genre().apply {id=1; name="Heavy Metal"})
        `when`(genreService.getAll()).thenReturn(genres)

        mockMvc.get("/api/genre/all")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Heavy Metal'}]")}
            }
    }

    @Test
    fun `getBandAlbumGenresList should return genres for band`() {
        val genresDto=listOf(GenreDto(id=1,name="Heavy Metal",value=100))
        `when`(genreService.getBandAlbumGenresList(1)).thenReturn(genresDto)

        mockMvc.get("/api/genre/allAlbums/1")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Heavy Metal','value':100}]")}
            }
    }
}

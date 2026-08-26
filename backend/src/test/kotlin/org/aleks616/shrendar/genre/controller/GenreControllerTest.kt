package org.aleks616.shrendar.genre.controller

import org.aleks616.shrendar.album.repository.AlbumRepository
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.genre.service.GenreService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GenreControllerTest {
    @Autowired
    private lateinit var mockMvc:MockMvc

    @Autowired
    private lateinit var rateLimiter:org.aleks616.shrendar.security.RateLimiter
    @Autowired
    private val genreService:GenreService=mock(GenreService::class.java)
    @Autowired
    private val genreRepository:GenreRepository=mock(GenreRepository::class.java)
    @Autowired
    private val albumRepository:AlbumRepository=mock(AlbumRepository::class.java)

    @BeforeEach
    fun setup(){
        genreRepository.deleteAll()
        albumRepository.deleteAll()

        val storageField=org.aleks616.shrendar.security.RateLimiter::class.java.getDeclaredField("storage")
        storageField.isAccessible=true
        (storageField.get(rateLimiter) as MutableMap<*,*>).clear()
    }

    @Test
    fun `getGenre should return all genres`() {
        genreRepository.saveAndFlush(
            Genre().apply {id=1; name="Genre 1"; properties="1231021"}
        )

        mockMvc.get("/api/genre/all")
            .andExpect {
                status {isOk()}
                content {json("[{'id':1,'name':'Genre 1'}]")}
            }
    }
}

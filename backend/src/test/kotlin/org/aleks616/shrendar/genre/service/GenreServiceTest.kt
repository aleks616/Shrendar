package org.aleks616.shrendar.genre.service

import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto1
import org.aleks616.shrendar.genre.repository.GenreRepository
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersGenres
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.math.BigDecimal

class GenreServiceTest {
    private lateinit var genreRepository:GenreRepository
    private lateinit var userRepository:UserRepository
    private lateinit var userGenreRepository:UserGenreRepository
    private lateinit var service:GenreService
    private lateinit var genre:Genre
    private lateinit var user:User

    @BeforeEach
    fun setup() {
        genreRepository=mock(GenreRepository::class.java)
        userRepository=mock(UserRepository::class.java)
        userGenreRepository=mock(UserGenreRepository::class.java)
        service=GenreService(genreRepository,userRepository,userGenreRepository)
        genre=Genre().apply {id=3; name="Metal"; properties="1010101"}
        user=User().apply {id=7; login="tester"}
    }

    @Test
    fun `getAll returns repository genres`() {
        `when`(genreRepository.findAll()).thenReturn(listOf(genre))

        assertEquals(listOf(genre),service.getAll())
    }

    @Test
    fun `getBandAlbumGenresList maps values to bytes`() {
        `when`(genreRepository.findBandAlbumGenresList(2)).thenReturn(
            mutableListOf(
                GenreDto1(id=3,name="Metal",value=BigDecimal("258")),
                GenreDto1(id=4,name="Rock",value=null)
            )
        )

        assertEquals(
            listOf(
                org.aleks616.shrendar.genre.model.GenreDto(id=3,name="Metal",value=2),
                org.aleks616.shrendar.genre.model.GenreDto(id=4,name="Rock",value=null)
            ),
            service.getBandAlbumGenresList(2)
        )
    }

    @Test
    fun `doesGenreExist returns repository result`() {
        `when`(genreRepository.existsById(3)).thenReturn(true)
        `when`(genreRepository.existsById(4)).thenReturn(false)

        assertTrue(service.doesGenreExist(3))
        assertFalse(service.doesGenreExist(4))
    }

    @Test
    fun `toggleFavoriteGenre creates missing favorite`() {
        doReturn(user).`when`(userRepository).findByLogin("tester")
        doReturn(genre).`when`(genreRepository).findGenreById(3)
        doAnswer {UsersGenres().apply {id=-1L}}.`when`(userGenreRepository)
            .findByGenreAndUser(genre,user)
        doReturn(UsersGenres()).`when`(userGenreRepository)
            .saveAndFlush(any(UsersGenres::class.java))

        service.toggleFavoriteGenre(3,"tester")

        val captor=ArgumentCaptor.forClass(UsersGenres::class.java)
        verify(userGenreRepository).saveAndFlush(captor.capture())
        assertSame(user,captor.value.user)
        assertSame(genre,captor.value.genre)
        verify(userGenreRepository,never()).deleteById(anyLong())
    }

    @Test
    fun `toggleFavoriteGenre deletes existing favorite`() {
        `when`(userRepository.findByLogin("tester")).thenReturn(user)
        `when`(genreRepository.findGenreById(3)).thenReturn(genre)
        `when`(userGenreRepository.findByGenreAndUser(genre,user))
            .thenReturn(UsersGenres().apply {id=11})

        service.toggleFavoriteGenre(3,"tester")

        verify(userGenreRepository).deleteById(11)
        verify(userGenreRepository,never()).saveAndFlush(any(UsersGenres::class.java))
    }

    @Test
    fun `toggleFavoriteGenre throws error unknown user`() {
        `when`(userRepository.findByLogin("missing")).thenReturn(null)

        assertThrows<IllegalStateException> {
            service.toggleFavoriteGenre(3,"missing")
        }
        verifyNoInteractions(genreRepository,userGenreRepository)
    }
}

package org.aleks616.shrendar.homepage.controller

import org.aleks616.shrendar.homepage.model.HomePageMainDto
import org.aleks616.shrendar.homepage.service.HomePageService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class HomePageControllerTest {
    private val service=mock(HomePageService::class.java)
    private val controller=HomePageController(service)

    @BeforeEach
    fun setup() {
        SecurityContextHolder.getContext().authentication=
            UsernamePasswordAuthenticationToken("tester",null,emptyList())
    }

    @AfterEach
    fun tearDown() = SecurityContextHolder.clearContext()

    @Test
    fun `authenticated anniversary endpoints use the current user`() {
        controller.getUpcomingFavoriteArtistsBirthdays()
        controller.getUpcomingFavoriteArtistDeathAnniversaries()
        controller.getUpcomingFavoriteAlbumAnniversaries()
        controller.getCommonBands()

        verify(service).getUpcomingFavoriteArtistsBirthdays("tester")
        verify(service).getUpcomingFavoriteArtistsDeathAnniversaries("tester")
        verify(service).getUpcomingFavoriteAlbumAnniversaries("tester")
        verify(service).getCommonBands("tester")
    }

    @Test
    fun `anonymous anniversary endpoints use random data and hide common bands`() {
        anonymous()

        assertEquals(emptyList<Any>(),controller.getUpcomingFavoriteArtistsBirthdays())
        assertEquals(emptyList<Any>(),controller.getUpcomingFavoriteArtistDeathAnniversaries())
        assertEquals(emptyList<Any>(),controller.getUpcomingFavoriteAlbumAnniversaries())
        assertEquals(emptyList<Any>(),controller.getCommonBands())

        verify(service).getUpcomingRandomArtistsBirthdays()
        verify(service).getUpcomingRandomArtistsDeathAnniversaries()
        verify(service).getUpcomingRandomAlbumAnniversaries()
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `today anniversaries select authenticated and anonymous service variants`() {
        val authenticated=HomePageMainDto()
        `when`(service.getTodayAnniversaries("tester")).thenReturn(authenticated)
        assertEquals(authenticated,controller.getTodayAnniversaries())

        anonymous()
        val anonymous=HomePageMainDto()
        `when`(service.getTodayAnniversariesNoAuth()).thenReturn(anonymous)
        assertEquals(anonymous,controller.getTodayAnniversaries())
    }

    @Test
    fun `recent additions always delegates without requiring authentication`() {
        SecurityContextHolder.clearContext()
        val expected=listOf("addition")
        `when`(service.getRecentlyAdded()).thenReturn(expected)

        assertEquals(expected,controller.getRecentlyAdded())
        verify(service).getRecentlyAdded()
    }

    @Test
    fun `protected endpoints reject a missing authentication`() {
        SecurityContextHolder.clearContext()

        assertThrows<IllegalStateException> {controller.getUpcomingFavoriteArtistsBirthdays()}
        assertThrows<IllegalStateException> {controller.getUpcomingFavoriteArtistDeathAnniversaries()}
        assertThrows<IllegalStateException> {controller.getUpcomingFavoriteAlbumAnniversaries()}
        assertThrows<IllegalStateException> {controller.getTodayAnniversaries()}
        assertThrows<IllegalStateException> {controller.getCommonBands()}
        verifyNoInteractions(service)
    }

    private fun anonymous() {
        SecurityContextHolder.getContext().authentication=AnonymousAuthenticationToken(
            "test-key","anonymousUser",AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        )
    }
}

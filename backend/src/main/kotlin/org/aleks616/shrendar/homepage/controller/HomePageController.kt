package org.aleks616.shrendar.homepage.controller

import org.aleks616.shrendar.album.model.AlbumAnniversaryDto
import org.aleks616.shrendar.artist.model.ArtistAnniversaryDto
import org.aleks616.shrendar.homepage.model.HomePageMainDto
import org.aleks616.shrendar.homepage.service.HomePageService
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/main")
class HomePageController(
    private val homePageService:HomePageService
) {

    @GetMapping("/artist/upcomingBirthdays")
    fun getUpcomingFavoriteArtistsBirthdays():List<ArtistAnniversaryDto>{
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getUpcomingRandomArtistsBirthdays()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name

        return homePageService.getUpcomingFavoriteArtistsBirthdays(userLogin)
    }

    @GetMapping("/artist/upcomingDeathAnns")
    fun getUpcomingFavoriteArtistDeathAnniversaries():List<ArtistAnniversaryDto>{
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getUpcomingRandomArtistsDeathAnniversaries()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name

        return homePageService.getUpcomingFavoriteArtistsDeathAnniversaries(userLogin)
    }

    @GetMapping("/album/upcomingAnniversaries")
    fun getUpcomingFavoriteAlbumAnniversaries():List<AlbumAnniversaryDto>{
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getUpcomingRandomAlbumAnniversaries()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name

        return homePageService.getUpcomingFavoriteAlbumAnniversaries(userLogin)
    }

    @GetMapping("/today")
    fun getTodayAnniversaries():HomePageMainDto {
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getTodayAnniversariesNoAuth()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something went wrong")
        val userLogin=user.name
        return homePageService.getTodayAnniversaries(userLogin)
    }
}
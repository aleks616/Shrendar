package org.aleks616.shrendar.homepage.controller

import org.aleks616.shrendar.album.model.AlbumAnniversaryDto
import org.aleks616.shrendar.artist.model.ArtistAnniversaryDto
import org.aleks616.shrendar.band.model.ArtistBandsDto
import org.aleks616.shrendar.homepage.model.CommonBandsDto
import org.aleks616.shrendar.homepage.model.HomepageMainDto
import org.aleks616.shrendar.homepage.model.RecentlyAddedDto
import org.aleks616.shrendar.homepage.model.UpcomingAnniversariesDto
import org.aleks616.shrendar.homepage.service.HomepageService
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/main")
class HomepageController(
    private val homePageService:HomepageService
) {

    @GetMapping("/artist/upcomingBirthdays")
    fun getUpcomingFavoriteArtistsBirthdays():List<ArtistAnniversaryDto>{
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getUpcomingRandomArtistsBirthdays()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name

        return homePageService.getUpcomingFavoriteArtistsBirthdays(userLogin)
    }

    @GetMapping("/artist/upcomingDeathAnns")
    fun getUpcomingFavoriteArtistDeathAnniversaries():List<ArtistAnniversaryDto>{
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getUpcomingRandomArtistsDeathAnniversaries()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name

        return homePageService.getUpcomingFavoriteArtistsDeathAnniversaries(userLogin)
    }

    @GetMapping("/album/upcomingAnniversaries")
    fun getUpcomingFavoriteAlbumAnniversaries():List<AlbumAnniversaryDto>{
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getUpcomingRandomAlbumAnniversaries()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name

        return homePageService.getUpcomingFavoriteAlbumAnniversaries(userLogin)
    }

    @GetMapping("/upcoming")
    fun getUpcoming():UpcomingAnniversariesDto {
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getRandomUpcoming()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name

        return homePageService.getUpcoming(userLogin)
    }

    @GetMapping("/")
    fun getTodayAnniversaries():HomepageMainDto {
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return homePageService.getTodayAnniversariesNoAuth()
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name
        return homePageService.getTodayAnniversaries(userLogin)
    }

    @GetMapping("/commonBands")
    fun getCommonBands():CommonBandsDto? {
        if(SecurityContextHolder.getContext().authentication is AnonymousAuthenticationToken) {
            return null
        }
        val user=SecurityContextHolder.getContext().authentication?:throw IllegalStateException("something_wrong")
        val userLogin=user.name
        return homePageService.getCommonBands(userLogin)
    }

    @GetMapping("/recentlyAdded")
    fun getRecentlyAdded():List<RecentlyAddedDto>{
        return homePageService.getRecentlyAdded()
    }
}
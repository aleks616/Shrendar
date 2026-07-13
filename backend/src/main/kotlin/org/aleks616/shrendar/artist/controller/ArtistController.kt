package org.aleks616.shrendar.artist.controller

import org.aleks616.shrendar.artist.service.ArtistService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/api/artist")
class ArtistController(
    private val artistService:ArtistService,
) {
    @GetMapping("/artist")
    fun getArtist()=artistService.getAll()

    @GetMapping("/artistBirthdays")
    fun getArtistWithBirthdayInDate(@RequestParam month:Int,@RequestParam day:Int)=
        artistService.getTodayBirthdays(month,day)

    @GetMapping("/artistDeaths")
    fun getArtistWithDeathInDate(@RequestParam month:Int,@RequestParam day:Int)=
        artistService.getTodayDeathAnniversaries(month,day)

    @GetMapping("/recentDeaths")
    fun getRecentArtistDeathAnniversaries()=artistService.getRecentDeaths()
}
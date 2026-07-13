package org.aleks616.shrendar.user.controller

import org.aleks616.shrendar.user.repository.UserArtistRepository
import org.aleks616.shrendar.user.repository.UserBandRepository
import org.aleks616.shrendar.user.repository.UserGenreRepository
import org.aleks616.shrendar.user.service.RankService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class UserController (
    private val userArtistRepository:UserArtistRepository,
    private val userBandRepository:UserBandRepository,
    private val userGenreRepository:UserGenreRepository,
    private val rankService:RankService,
){
    @GetMapping("/userArtist")
    fun getUserArtist()=userArtistRepository.findAll()

    @GetMapping("/userBand")
    fun getUserBand()=userBandRepository.findAll()

    @GetMapping("/userGenre")
    fun getUserGenre()=userGenreRepository.findAll()

    @GetMapping("/ranks")
    fun getRank()=rankService.getAll()
}

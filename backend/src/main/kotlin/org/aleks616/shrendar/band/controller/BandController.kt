package org.aleks616.shrendar.band.controller

import org.aleks616.shrendar.band.repository.BandsGenreRepository
import org.aleks616.shrendar.band.repository.BandsMemberRepository
import org.aleks616.shrendar.band.service.BandService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/band")
class BandController (
    private val bandService:BandService,
    private val bandsGenreRepository:BandsGenreRepository,
    private val bandsMemberRepository:BandsMemberRepository,
){
    @GetMapping("/bands")
    fun getBand()=bandService.getAll()

    @GetMapping("/bandsGenres")
    fun getBandsGenre()=bandsGenreRepository.findAll()

    @GetMapping("/bandsMember")
    fun getBandsMember()=bandsMemberRepository.findAll()

}

package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArtistService(private val artistRepository:ArtistRepository) {

    fun getAll():List<Artist> {
        return artistRepository.findAll()
    }

    fun getById(id:Int):List<Artist> {
        if(!artistRepository.existsArtistById(id)) throw IllegalArgumentException("artist with id doesn't exist")
        return artistRepository.findArtistById(id)
    }

    fun getByNameLike(name:String):List<Artist> {
        return artistRepository.findArtistByNameContains(name)
    }

    fun getByFirstName(name:String):List<Artist> {
        return artistRepository.findArtistByNameStartsWith(name)
    }

    fun getByLastName(name:String):List<Artist> {
        return artistRepository.findArtistByNameEndsWithIgnoreCase(name)
    }

    fun getByBirthday(month:Int,day:Int):List<Artist> {
        return artistRepository.findArtistByBirthDate(month,day)
    }

    fun getByDeathDate(month:Int,day:Int):List<Artist> {
        return artistRepository.findArtistByDeathDate(month,day)
    }

    fun getByBirthdayBetween(startMonth:Int,startDay:Int,endMonth:Int,endDay:Int):List<Artist> {
        return artistRepository.findArtistByBirthdayBetween(startMonth,startDay,endMonth,endDay)
    }

    fun getByBirthYear(year:Int):List<Artist> {
        return artistRepository.findArtistsByBirthYear(year)
    }

    fun getByBirthYearBetween(startYear:Int,endYear:Int):List<Artist> {
        return artistRepository.findArtistsByBirthYearBetween(startYear,endYear)
    }

    fun getByCountry(countryId:Int):List<Artist> {
        return artistRepository.findArtistByCountry(countryId)
    }

    fun getRecentDeathsAnniversaries():List<Artist> {
        val today=LocalDate.now()
        val recentDate=LocalDate.now().minusDays(30)
        return artistRepository.findArtistByDeathDateBetween(recentDate.monthValue,recentDate.dayOfMonth,today.monthValue,today.dayOfMonth)
    }

    fun getRecentBirthdays():List<Artist> {
        val today=LocalDate.now()
        val recentDate=LocalDate.now().minusDays(30)
        return artistRepository.findArtistByBirthdayBetween(recentDate.monthValue,recentDate.dayOfMonth,today.monthValue,today.dayOfMonth)
    }

}
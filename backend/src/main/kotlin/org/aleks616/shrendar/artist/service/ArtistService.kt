package org.aleks616.shrendar.artist.service

import org.aleks616.shrendar.common.Utils
import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.artist.model.ArtistsBirthDayDto
import org.aleks616.shrendar.artist.model.ArtistsDeathDayDto
import org.aleks616.shrendar.artist.model.RecentDeathAnniversariesDTO
import org.aleks616.shrendar.artist.repository.ArtistRepository
import org.springframework.stereotype.Service

@Service
class ArtistService(private val repository:ArtistRepository){
    fun getAll():List<Artist> =repository.findAll()

    fun getTodayBirthdays(month:Int,day:Int):List<ArtistsBirthDayDto> {
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("month and day aren't valid")

        return repository.findByBirthday(month,day)
    }
    fun getTodayDeathAnniversaries(month: Int,day: Int):List<ArtistsDeathDayDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("month and day aren't valid")

        println(repository.findByDeathDate(month,day))
        return repository.findByDeathDate(month,day)
    }

    fun getRecentDeaths():List<RecentDeathAnniversariesDTO>{
        //TODO: ADD AGE! IMPORTANT!
        return repository.findRecentDeathAnniversaries()
    }

}
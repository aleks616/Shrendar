package org.aleks616.shrendar.services

import org.aleks616.shrendar.Utils
import org.aleks616.shrendar.dto.ArtistsBirthDayDto
import org.aleks616.shrendar.dto.ArtistsDeathDayDto
import org.aleks616.shrendar.dto.RecentDeathAnniversariesDTO
import org.aleks616.shrendar.entities.Artists
import org.aleks616.shrendar.repositories.ArtistRepository
import org.springframework.stereotype.Service

@Service
class ArtistService(private val repository:ArtistRepository){
    fun getAll():List<Artists> =repository.findAll()

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
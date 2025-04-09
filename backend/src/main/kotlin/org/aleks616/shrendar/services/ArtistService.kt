package org.aleks616.shrendar.services

import org.aleks616.shrendar.Utils
import org.aleks616.shrendar.dto.ArtistsBirthDayDto
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

}
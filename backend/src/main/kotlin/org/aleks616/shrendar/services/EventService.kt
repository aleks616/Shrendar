package org.aleks616.shrendar.services

import org.aleks616.shrendar.Utils
import org.aleks616.shrendar.dto.AlbumsByDateDto
import org.aleks616.shrendar.dto.EventBandDto
import org.aleks616.shrendar.entities.Event
import org.aleks616.shrendar.repositories.EventRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventService(private val repository:EventRepository){
    fun getAll():List<Event> =repository.findAll()

    fun getEventsByDate(month:Int,day:Int):List<EventBandDto>{
        if(!Utils.doesDateExist(month,day)) throw IllegalArgumentException("month and day aren't valid")
        val year=Calendar.getInstance().get(Calendar.YEAR)
        val eventInDate=getAll().filter {it.date?.monthValue==month && it.date?.dayOfMonth==day}

        return eventInDate.map{e->
            EventBandDto(
                id=e.id,
                band=e.band?.let {EventBandDto.BandsDto(it.id,it.name)},
                date=e.date,
                name=e.name,
                description=e.description,
                yearsSince=year-(e.date?.year!!)
            )
        }

    }
}
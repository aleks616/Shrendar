package org.aleks616.shrendar.event.repository

import org.aleks616.shrendar.event.model.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EventRepository:JpaRepository<Event,Int>{
    fun findEventById(id:Int):Event

}

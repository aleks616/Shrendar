package org.aleks616.shrendar.event.controller

import org.aleks616.shrendar.event.service.EventService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/api/event")
class EventController(
    private val eventService:EventService,
) {
    @GetMapping("/eventsInDate")
    fun getEventsByDate(@RequestParam month:Int,@RequestParam day:Int)=eventService.getEventsByDate(month,day)
}

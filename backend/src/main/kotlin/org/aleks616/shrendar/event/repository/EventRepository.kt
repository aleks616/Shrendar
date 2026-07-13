package org.aleks616.shrendar.event.repository

import org.aleks616.shrendar.event.model.Event
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository:JpaRepository<Event,Int> {
}
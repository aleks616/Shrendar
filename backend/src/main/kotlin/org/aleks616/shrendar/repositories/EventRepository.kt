package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Event
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository:JpaRepository<Event,Int> {
}
package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.entities.Country
import org.springframework.data.jpa.repository.JpaRepository

interface CountryRepository:JpaRepository<Country,Int> {
}
package org.aleks616.shrendar.common.repository

import org.aleks616.shrendar.common.model.Country
import org.springframework.data.jpa.repository.JpaRepository

interface CountryRepository:JpaRepository<Country,Int> {
}
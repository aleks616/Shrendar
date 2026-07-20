package org.aleks616.shrendar.common.repository

import org.aleks616.shrendar.common.model.Country
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CountryRepository:JpaRepository<Country,Int> {
    fun getCountryById(id:Int,pageable:Pageable):Page<Country>
    fun getCountryById(id:Int):MutableList<Country>
    fun getCountryByName(name:String):MutableList<Country>
}
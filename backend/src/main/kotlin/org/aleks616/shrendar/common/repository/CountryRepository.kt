package org.aleks616.shrendar.common.repository

import org.aleks616.shrendar.common.model.Country
import org.springframework.data.jpa.repository.JpaRepository

interface CountryRepository:JpaRepository<Country,Int> {

    //@Query("SELECT c FROM Country c WHERE c.id=:id")
    //fun getCountryById(id:Int):Country
    fun getCountryByName(name:String):MutableList<Country>
}
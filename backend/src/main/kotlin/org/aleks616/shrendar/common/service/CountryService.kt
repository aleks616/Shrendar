package org.aleks616.shrendar.common.service

import org.aleks616.shrendar.common.repository.CountryRepository
import org.springframework.stereotype.Service

@Service
class CountryService(
    val countryRepository:CountryRepository
) {
    fun doesCountryExist(id:Int):Boolean{
        return countryRepository.existsById(id)
    }
}

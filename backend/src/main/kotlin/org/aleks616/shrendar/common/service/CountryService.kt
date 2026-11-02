package org.aleks616.shrendar.common.service

import org.aleks616.shrendar.common.model.CountryDto
import org.aleks616.shrendar.common.repository.CountryRepository
import org.springframework.stereotype.Service

@Service
class CountryService(
    val countryRepository:CountryRepository
) {
    fun doesCountryExist(id:Int):Boolean {
        return countryRepository.existsById(id)
    }

    fun getAll():List<CountryDto> {
        return countryRepository.findAll().map {CountryDto(it.id,it.name)}
    }
}

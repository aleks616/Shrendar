package org.aleks616.shrendar.band.service

import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.BandDto
import org.aleks616.shrendar.band.model.CountryDto
import org.aleks616.shrendar.band.model.Status
import org.aleks616.shrendar.band.repository.BandRepository
import org.aleks616.shrendar.common.repository.CountryRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BandService(
    private val bandRepository:BandRepository,
    private val countryRepository:CountryRepository
){
    //region util
    fun getBandsCountry(bandId:Int):CountryDto?{
        return bandRepository.findCountryByBandId(bandId)
    }
    fun getBandData(bands:List<Band>):List<BandDto>{
        return bands.map{ b->
            BandDto(
                id=b.id,
                name=b.name,
                formedYear=b.formedYear,
                disbandedYear=b.disbandedYear,
                status=b.status,
                country=getBandsCountry(b.id!!),
                description=b.description
            )

        }
    }
    //endregion

    fun getAll():List<BandDto>{
        val bands=bandRepository.findAll()
        return getBandData(bands)
    }

    fun getBandById(id:Int):BandDto{
        val band=bandRepository.findById(id)
        return getBandData(listOf(band.get())).first()
    }

    fun getCountryByName(name:String):CountryDto?{
        val country=countryRepository.getCountryByName(name)
        return country.map {c->
            CountryDto(c.id,c.name)
        }.firstOrNull()
    }

    fun getBandsByName(name:String):List<BandDto>{
        val bands=bandRepository.findByNameContainingIgnoreCase(name)
        return getBandData(bands)
    }

    fun getBandsByNameExact(name:String):List<BandDto>{
        val bands=bandRepository.findByNameIgnoreCase(name)
        return getBandData(bands)
    }

    fun getBandsByCountry(name:String):List<BandDto>{
        val country=getCountryByName(name)
        val bands=bandRepository.findByCountry(country?.id!!)
        return getBandData(bands)
    }

    fun getBandsByCountryId(id:Int):List<BandDto>{
        val bands=bandRepository.findByCountry(id)
        return getBandData(bands)
    }

    fun getBandsByFoundedBetween(startYear:Int?,endYear:Int?):List<BandDto>{
        val start=startYear?:1900
        val end=endYear?:LocalDate.now().year
        val bands=bandRepository.findByFormedYearBetween(start,end)
        return getBandData(bands)
    }

    fun getBandsByStatus(status:Status):List<BandDto>{
        val bands=bandRepository.findByStatus(status)
        return getBandData(bands)
    }

}
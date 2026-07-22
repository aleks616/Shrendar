package org.aleks616.shrendar.band.repository

import org.aleks616.shrendar.band.model.Band
import org.aleks616.shrendar.band.model.CountryDto
import org.aleks616.shrendar.band.model.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BandRepository :JpaRepository<Band,Int>{

    @Query("""
        SELECT c.id, c.name
        FROM band b JOIN country c ON b.country=c.id
        WHERE b.band_id=:bandId
    """,nativeQuery=true)
    fun findCountryByBandId(bandId:Int):CountryDto?
    fun findByNameContainingIgnoreCase(name:String):List<Band>
    fun findByNameIgnoreCase(name:String):List<Band>
    fun findByCountry(country:Int):List<Band>
    fun findByFormedYearBetween(formedYearAfter:Int,formedYearBefore:Int):List<Band>
    fun findByStatus(status:Status):List<Band>

    @Query("SELECT b FROM Band b WHERE b.id=:id")
    fun findBandById(id:Int):Band

    @Query("SELECT b FROM Band b WHERE b.averageGenre IS NOT NULL")
    fun findBandsWithAvgGenre():List<Band>
}
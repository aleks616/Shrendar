package org.aleks616.shrendar.artist.repository

import org.aleks616.shrendar.artist.model.Artist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ArtistRepository:JpaRepository<Artist,Int> {
    fun existsArtistById(id:Int):Boolean
    fun findArtistById(id:Int):Artist
    fun findArtistByNameContains(name:String):MutableList<Artist>
    fun findArtistByNameStartsWith(name:String):MutableList<Artist>
    fun findArtistByNameEndsWithIgnoreCase(name:String):MutableList<Artist>

    @Query("""
        SELECT a
        FROM Artist a
        WHERE FUNCTION('MONTH',a.birthDate)=:month AND FUNCTION('DAYOFMONTH',a.birthDate)=:day
    """)
    fun findArtistByBirthDate(month:Int,day:Int):MutableList<Artist>

    @Query("""
        SELECT *
        FROM artist a
        WHERE DATE(CONCAT('2000-',MONTH(a.birth_date),'-',DAYOFMONTH(a.birth_date))) BETWEEN DATE(CONCAT('2000-',:startMonth,'-',:startDay)) AND DATE(CONCAT('2000-',:endMonth,'-',:endDay))
        ORDER BY MONTH(a.birth_date), DAYOFMONTH(a.birth_date)
    """,nativeQuery=true)
    fun findArtistByBirthdayBetween(startMonth:Int,startDay:Int,endMonth:Int,endDay:Int):List<Artist>

    @Query("""
        SELECT a
        FROM Artist a
        WHERE FUNCTION('YEAR',a.birthDate)=:year
    """)
    fun findArtistsByBirthYear(year:Int):MutableList<Artist>

    @Query("""
        SELECT a
        FROM Artist a
        WHERE FUNCTION('YEAR',a.birthDate)>=:startYear AND FUNCTION('YEAR',a.birthDate)>=:endYear
    """)
    fun findArtistsByBirthYearBetween(startYear:Int,endYear:Int):List<Artist>

    @Query("""
        SELECT a
        FROM Artist a
        WHERE FUNCTION('MONTH',a.deathDate)=:month AND FUNCTION('DAYOFMONTH',a.deathDate)=:day
    """)
    fun findArtistByDeathDate(month:Int,day:Int):MutableList<Artist>

    @Query("""
        SELECT *
        FROM artist a
        WHERE DATE(CONCAT('2000-',MONTH(a.death_date),'-',DAYOFMONTH(a.death_date))) BETWEEN DATE(CONCAT('2000-',:startMonth,'-',:startDay)) AND DATE(CONCAT('2000-',:endMonth,'-',:endDay))
        ORDER BY MONTH(a.death_date), DAYOFMONTH(a.death_date)
    """,nativeQuery=true)
    fun findArtistByDeathDateBetween(startMonth:Int,startDay:Int,endMonth:Int,endDay:Int):List<Artist>
    fun findArtistByCountry(country:Int):MutableList<Artist>

    @Query("SELECT a.id FROM Artist a WHERE a.name=:name ORDER BY a.id DESC LIMIT 1")
    fun findTopIdByName(name:String):Int
}
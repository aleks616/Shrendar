package org.aleks616.shrendar.artist.repository

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.common.model.NameValue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ArtistRepository:JpaRepository<Artist,Int> {
    fun existsArtistById(id:Long):Boolean
    fun findArtistById(id:Long):Artist
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
        SELECT NEW org.aleks616.shrendar.common.model.NameValue(g.name,COUNT(g.id))
        FROM Artist ar JOIN BandsMembers bm ON ar.id=bm.artist.id 
        JOIN Band b ON bm.band.id=b.id 
        JOIN Album al ON al.band.id=b.id 
        JOIN Genre g ON g.id=al.genre.id
        WHERE FUNCTION('YEAR',al.releaseDate) BETWEEN bm.joinedYear AND bm.leftYear 
        AND ar.id=:artistId
        GROUP BY g.name
        ORDER BY COUNT(g.id) DESC
    """)
    fun findArtistGenres(artistId:Long):List<NameValue>

    @Query("""
        SELECT *
        FROM artist a
        WHERE DATE(CONCAT('2000-',MONTH(a.death_date),'-',DAYOFMONTH(a.death_date))) BETWEEN DATE(CONCAT('2000-',:startMonth,'-',:startDay)) AND DATE(CONCAT('2000-',:endMonth,'-',:endDay))
        ORDER BY MONTH(a.death_date), DAYOFMONTH(a.death_date)
    """,nativeQuery=true)
    fun findArtistByDeathDateBetween(startMonth:Int,startDay:Int,endMonth:Int,endDay:Int):List<Artist>
    fun findArtistByCountry(country:Int):MutableList<Artist>

    @Query("SELECT a.id FROM Artist a WHERE a.name=:name ORDER BY a.id DESC LIMIT 1")
    fun findTopIdByName(name:String):Long
    fun existsById(id:Long):Boolean
    fun deleteById(id:Long)
}
package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.dto.ArtistsBirthDayDto
import org.aleks616.shrendar.dto.ArtistsDeathDayDto
import org.aleks616.shrendar.dto.RecentDeathAnniversariesDTO
import org.aleks616.shrendar.entities.Artists
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArtistRepository:JpaRepository<Artists, Int>{
@Query("""
    SELECT NEW org.aleks616.shrendar.dto.ArtistsBirthDayDto(
        a.id, a.name, 
        CAST(FUNCTION('DAYOFMONTH',a.birthDate) AS INTEGER), 
        CAST(FUNCTION('MONTH',a.birthDate) AS INTEGER),
        CAST(FUNCTION('YEAR', a.birthDate) AS INTEGER)
        ,FLOOR(CAST(FUNCTION('DATEDIFF', CURRENT_DATE, a.birthDate) AS INTEGER) / 365)
)
    FROM Artists a WHERE FUNCTION('MONTH',a.birthDate)=:month AND FUNCTION('DAYOFMONTH',a.birthDate)=:day
""")
    fun findByBirthday(@Param("month") month:Int,@Param("day") day:Int):List<ArtistsBirthDayDto>
    //THIS IS FOR BIRTHDAYS! SO IT DOESN'T HAVE TO BE ACCURATE IN WEIRD CASES!



    @Query("""
    SELECT NEW org.aleks616.shrendar.dto.ArtistsDeathDayDto(
        a.id, a.name, 
        CAST(FUNCTION('DAYOFMONTH',a.deathDate) AS INTEGER), 
        CAST(FUNCTION('MONTH',a.deathDate) AS INTEGER),
        CAST(FUNCTION('YEAR', a.deathDate) AS INTEGER)
        ,CAST(FLOOR(CAST(FUNCTION('DATEDIFF', a.deathDate, a.birthDate) AS DOUBLE) / 365.25) AS INTEGER ) 
)
    FROM Artists a WHERE FUNCTION('MONTH',a.deathDate)=:month AND FUNCTION('DAYOFMONTH',a.deathDate)=:day
""")
    fun findByDeathDate(@Param("month") month: Int, @Param("day") day: Int):List<ArtistsDeathDayDto>


@Query("""
    SELECT NEW org.aleks616.shrendar.dto.RecentDeathAnniversariesDTO(
        a.id, a.name, CAST(a.birthDate AS string), CAST(a.deathDate AS string)
    )
    FROM Artists a
    WHERE a.deathDate IS NOT NULL
    AND FUNCTION('DATEDIFF', CURRENT_DATE,
        FUNCTION('DATE', CONCAT('2025-', FUNCTION('MONTH', a.deathDate), '-', FUNCTION('DAY', a.deathDate)))) BETWEEN 0 AND 30
    ORDER BY FUNCTION('DATEDIFF',FUNCTION('DATE', CONCAT('2025-', FUNCTION('MONTH', a.deathDate), '-', FUNCTION('DAY', a.deathDate))),CURRENT_DATE) DESC
""")
    fun findRecentDeathAnniversaries():List<RecentDeathAnniversariesDTO>
}


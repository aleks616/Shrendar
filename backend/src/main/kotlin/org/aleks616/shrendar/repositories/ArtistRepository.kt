package org.aleks616.shrendar.repositories

import org.aleks616.shrendar.dto.ArtistsBirthDayDto
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
}
// FUNCTION('DAYOFMONTH',a.birthDate), FUNCTION('MONTH',a.birthDate), FUNCTION('YEAR',a.birthDate),
//FLOOR(FUNCTION('DATEDIFF',CURRENT_DATE,birthDate)/365)
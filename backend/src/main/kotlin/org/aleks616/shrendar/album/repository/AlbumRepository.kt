package org.aleks616.shrendar.album.repository

import org.aleks616.shrendar.album.model.Album
import org.aleks616.shrendar.album.model.AlbumAnniversaryDto
import org.aleks616.shrendar.genre.model.Genre
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AlbumRepository :JpaRepository<Album,Int>{

    @Query("""
        SELECT a
        FROM Album a
        WHERE a.band.id=:bandId
        ORDER BY a.releaseDate ASC
    """)
    fun findByBandId(bandId:Int):List<Album>

    @Query("""
        SELECT * 
        FROM Album a
        WHERE YEAR(a.release_date)=:year
    """, nativeQuery=true)
    fun findByYear(year:Int):List<Album>


    @Query("""
        SELECT * 
        FROM Album a
        WHERE MONTH(a.release_date)=:month AND DAY(a.release_date)=:day
    """,nativeQuery=true)
    fun findByReleaseDateMonthAndDay(month:Int,day:Int):List<Album>

    @Query("""
        SELECT a
        FROM Album a
        WHERE a.title LIKE CONCAT('%',:title,'%')
    """)
    fun findByTitleContainingIgnoreCase(title:String):List<Album>
    fun findByTitleIgnoreCase(title:String):List<Album>

    @Query("""
        SELECT a
        FROM Album a
        WHERE a.band.name LIKE CONCAT('%',:name,'%')
    """)
    fun findByBandNameContainingIgnoreCase(name:String):MutableList<Album>

    @Query("""
        SELECT a
        FROM Album a
        WHERE a.id=:id
    """)
    fun findAlbumById(id:Long):Album

    @Query("""
        SELECT a.id
        FROM Album a
        WHERE a.band.id=:bandId AND a.title=:title
        ORDER BY a.id DESC
        LIMIT 1
    """)
    fun findIdByData(bandId:Int,title:String):Long?
    fun findById(id:Long):Album
    fun deleteById(id:Long)
    fun existsById(id:Long):Boolean
    fun findByGenre(genre:Genre):MutableList<Album>

    @Query("""
        SELECT *
        FROM album a
        WHERE DATE(CONCAT(IF(MONTH(a.release_date)*100+DAYOFMONTH(a.release_date)<MONTH(NOW())*100+DAYOFMONTH(NOW()),'2001','2000'),'-',MONTH(a.release_date),'-',DAYOFMONTH(a.release_date)))
        BETWEEN DATE(CONCAT('2000-',MONTH(NOW()),'-',DAYOFMONTH(NOW()))+INTERVAL 1 DAY)
        AND (DATE(CONCAT('2000-',MONTH(NOW()),'-',DAYOFMONTH(NOW()))+INTERVAL :daysMax DAY))
        ORDER BY RAND()
        LIMIT 5
    """,nativeQuery=true)
    fun findAlbumsByUpcomingAnniversaries(daysMax:Int=15):MutableList<Album>
}
package org.aleks616.shrendar.album.repository

import org.aleks616.shrendar.album.model.Album
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AlbumRepository :JpaRepository<Album,Int>{

    @Query("""
        SELECT g.name
        FROM album a JOIN genre g ON a.genre_properties=g.properties
        WHERE a.album_id=:albumId
    """,nativeQuery=true)
    fun getAlbumGenre(albumId:Int?):String?

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
    fun findByTitleContainingIgnoreCase(title:String):List<Album>
    fun findByTitleIgnoreCase(title:String):List<Album>

    @Query("""
        SELECT a
        FROM Album a
        WHERE a.band.name LIKE %:name%
    """)
    fun findByBandNameContainingIgnoreCase(name:String):MutableList<Album>
}
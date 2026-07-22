package org.aleks616.shrendar.genre.repository

import org.aleks616.shrendar.band.model.BandGenreDto
import org.aleks616.shrendar.genre.model.Genre
import org.aleks616.shrendar.genre.model.GenreDto1
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GenreRepository:JpaRepository<Genre,Int>{

    @Query("""
        SELECT g.genre_id, g.name, SUM(importance) as 'value'
        FROM genre g
        JOIN album a ON g.genre_id = a.genre_id
        WHERE a.band_id=:id
        GROUP BY 2,1
        ORDER BY SUM(importance) DESC
        LIMIT 3
    """,nativeQuery=true)
    fun findBandAlbumGenresList(id:Int):MutableList<GenreDto1>

    fun findGenreById(id:Int):MutableList<Genre>
}
package org.aleks616.shrendar.band.model

import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.album.model.Album}
 */
data class AlbumDto(
    val id:Long=0,
    val title:String="",
    val releaseDate:LocalDate?=null,
    val type:String?=null,
    val importance:Byte?=null,
    val genreId:Int=0,
    val genreName:String="",
    val artworkUrl:String?=null,
    val description:String?=null
):Serializable
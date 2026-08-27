package org.aleks616.shrendar.album.model

import org.aleks616.shrendar.genre.model.Genre
import java.time.LocalDate


data class AlbumDataDto(
    val id:Long?=null,
    val band:BandDto?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val importance:Byte?=null,
    val genre:Genre?=null,
    val artworkUrl:String?=null,
)
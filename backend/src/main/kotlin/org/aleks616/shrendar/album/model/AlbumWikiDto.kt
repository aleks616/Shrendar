package org.aleks616.shrendar.album.model

import org.aleks616.shrendar.genre.model.Genre
import java.time.LocalDate

data class AlbumWikiDto(
    val id:Int?=null,
    val albumName:String?=null,
    val band:BandDto?=null,
    val releaseDate:LocalDate?=null,
    val albumAge:Int?=null,
    val daysTillAnniversary:Int?=null,
    val type:AlbumType?=null,
    val genre:Genre?=null,
    val description:String?=null,
    val artworkUrl:String?=null,
    val importance:Byte?=null,
)

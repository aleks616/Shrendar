package org.aleks616.shrendar.album.model

import java.time.LocalDate

data class AlbumAddDto(
    val id:Int?=null,
    val bandId:Int?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val description:String?=null,
    val mainSubgenre:Int?=null,
    val importance:Byte?=null,
    val artworkUrl:String?=null
)
package org.aleks616.shrendar.album.model

import java.time.LocalDate

data class AlbumAddDto(
    val id:Long=0,
    val bandId:Int=0,
    val title:String="",
    val releaseDate:LocalDate=LocalDate.now(),
    val type:AlbumType?=null,
    val description:String?=null,
    val mainSubgenre:Int?=null,
    val importance:Byte?=null,
    val artworkUrl:String?=null
)
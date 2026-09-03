package org.aleks616.shrendar.album.model

import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.album.model.Album}
 */
data class AlbumAnniversaryDto(
    val id:Long?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val artworkUrl:String?=null,
    val age:Int?=null,
    val daysTillAnniversary:Int?=null,
):Serializable
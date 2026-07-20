package org.aleks616.shrendar.album.model

import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.entities.Albums}
 */
data class AlbumByDateDto(
    val id:Int?=null,
    val band:BandDto?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val importance:Int?=null,
    val yearsSince:Int?=null,
    val genre:String?=null,
    val artworkUrl:String?=null,
):Serializable {
    /**
     * DTO for {@link org.aleks616.shrendar.entities.Bands}
     */

}
data class BandDto(val id:Int?=null,val name:String?=null):Serializable

data class AlbumDataDto(
    val id:Int?=null,
    val band:BandDto?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val importance:Int?=null,
    val genre:String?=null,
    val artworkUrl:String?=null,
)
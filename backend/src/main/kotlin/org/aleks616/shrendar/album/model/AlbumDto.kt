package org.aleks616.shrendar.album.model

import org.aleks616.shrendar.genre.model.Genre
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
    val genre:Genre?=null,
    val artworkUrl:String?=null,
):Serializable {
    /**
     * DTO for {@link org.aleks616.shrendar.entities.Bands}
     */

}
data class BandDto(val id:Int?=null,val name:String?=null):Serializable

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
    val importance:Int?=null,
)

data class AlbumDataDto(
    val id:Int?=null,
    val band:BandDto?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val importance:Int?=null,
    val genre:Genre?=null,
    val artworkUrl:String?=null,
)
package org.aleks616.shrendar.album.model

import org.aleks616.shrendar.genre.model.Genre
import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.entities.Albums}
 */
data class AlbumByDateDto(
    val id:Long?=null,
    val band:BandDto?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val importance:Byte?=null,
    val yearsSince:Int?=null,
    val genre:Genre?=null,
    val artworkUrl:String?=null,
):Serializable {
    /**
     * DTO for {@link org.aleks616.shrendar.entities.Bands}
     */

}
data class BandDto(val id:Int?=null,val name:String?=null):Serializable
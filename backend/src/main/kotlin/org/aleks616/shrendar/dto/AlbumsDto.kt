package org.aleks616.shrendar.dto

import org.aleks616.shrendar.entities.AlbumType
import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.entities.Albums}
 */
data class AlbumsByDateDto(
    val id:Int?=null,
    val bands:BandsDto?=null,
    val title:String?=null,
    val releaseDate:LocalDate?=null,
    val type:AlbumType?=null,
    val importance:Int?=null,
    val yearsSince:Int?=null
):Serializable {
    /**
     * DTO for {@link org.aleks616.shrendar.entities.Bands}
     */
    data class BandsDto(val id:Int?=null,val name:String?=null):Serializable
}
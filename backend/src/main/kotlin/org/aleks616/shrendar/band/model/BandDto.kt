package org.aleks616.shrendar.band.model

import org.aleks616.shrendar.genre.model.GenreDto
import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.Band}
 */
data class BandDto(
    val id:Int?=null,
    val name:String?=null,
    val formedYear:Int?=null,
    val disbandedYear:Int?=null,
    val status:Status?=null,
    val country:CountryDto?=null,
    val description:String?=null
):Serializable

data class CountryDto(var id:Int?=null,var name:String?=null)

data class BandWikiDto(
    val name:String?=null,
    val formedYear:Int?=null,
    val disbandedYear:Int?=null,
    val status:Status?=null,
    val country:CountryDto?=null,
    val description:String?=null,
    val imageUrl:String?=null,
    val computedGenres:List<GenreDto>?=null
):Serializable


data class BandGenreDto(
    var id:Int?=null,
    var name:String?=null,
    var formedYear:Int?=null,
    var country:CountryDto?=null,
    var similarity:Double?=null
)
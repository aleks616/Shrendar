package org.aleks616.shrendar.band.model

import org.aleks616.shrendar.common.model.CountryDto
import org.aleks616.shrendar.genre.model.GenreDto
import java.io.Serializable

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
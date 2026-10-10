package org.aleks616.shrendar.band.model

import org.aleks616.shrendar.genre.model.SimpleGenreDto
import java.io.Serializable

data class BandWikiDto(
    val name:String?=null,
    val formedYear:Int?=null,
    val disbandedYear:Int?=null,
    val status:String?=null,
    val country:String?=null,
    val description:String?=null,
    val imageUrl:String?=null,
    val computedGenres:List<SimpleGenreDto>?=null,
    val bandMembers:List<BandsMembersWikiDto>?=null,
    val albums:List<AlbumDto>?=null,
    val similar:List<BandGenreDto>?=null,
    val isFavorite:Boolean?=null
):Serializable
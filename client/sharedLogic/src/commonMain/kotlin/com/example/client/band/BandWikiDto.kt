package com.example.client.band

import com.example.client.common.AlbumDto
import com.example.client.common.SimpleGenreDto
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
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
)
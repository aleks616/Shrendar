package com.example.client.profile

import com.example.client.common.FavoriteArtistDto
import com.example.client.common.FavoriteBandDto
import com.example.client.common.FavoriteGenreDto
import com.example.client.common.ContributionDto
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class UserProfileDto(
    val login:String="",
    val username:String="",
    val rankId:Int=1,
    val rankName:String?=null,
    val xp:Int=1,
    val bio:String?=null,
    val accountAge:String?=null,
    val lastLogin:String?=null,

    val favoriteBands:List<FavoriteBandDto>?=null,
    val favoriteArtists:List<FavoriteArtistDto>?=null,
    val favoriteGenres:List<FavoriteGenreDto>?=null,

    val contributions:List<ContributionDto>?=null,
    val user:Boolean=false
)

package org.aleks616.shrendar.user.model

import org.aleks616.shrendar.artist.model.FavoriteArtistDto
import org.aleks616.shrendar.band.model.FavoriteBandDto
import org.aleks616.shrendar.contribution.model.ContributionDto
import org.aleks616.shrendar.genre.model.FavoriteGenreDto
import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.user.model.User}
 */
data class UserProfileDto(
    val login:String?=null,
    val username:String?=null,
    val rankId:Int?=null,
    val rankName:String?=null,
    val bio:String?=null,
    val accountAge:String?=null,
    val lastLogin:String?=null,

    val favoriteBands:List<FavoriteBandDto>?=null,
    val favoriteArtists:List<FavoriteArtistDto>?=null,
    val favoriteGenres:List<FavoriteGenreDto>?=null,

    val contributions:List<ContributionDto>?=null,
    ):Serializable
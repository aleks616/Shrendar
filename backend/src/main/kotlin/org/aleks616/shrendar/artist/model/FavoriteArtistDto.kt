package org.aleks616.shrendar.artist.model

import org.aleks616.shrendar.band.model.ArtistBandsStatusDto
import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.artist.model.Artist}
 */
data class FavoriteArtistDto(
    val id:Long?=null,
    val name:String?=null,
    val bands:List<ArtistBandsStatusDto>?=null
):Serializable
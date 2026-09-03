package org.aleks616.shrendar.homepage.model

import org.aleks616.shrendar.album.model.AlbumByDateDto
import org.aleks616.shrendar.artist.model.ArtistAnniversaryDto
import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.Band}
 */
data class HomePageMainDto(
    val favoriteAlbums:List<AlbumByDateDto>?=null,
    val favoriteArtistsBirthdays:List<ArtistAnniversaryDto>?=null,
    val favoriteArtistsDeathdays:List<ArtistAnniversaryDto>?=null,
    val otherBandMembersBirthdays:List<ArtistAnniversaryDto>?=null,
    val otherBandMembersDeathAnniversaries:List<ArtistAnniversaryDto>?=null,

    val recommendedArtistBirthdays:List<ArtistAnniversaryDto>?=null,
    val recommendedArtistDeathAnniversaries:List<ArtistAnniversaryDto>?=null,
    val recommendedAlbumsAnniversaries:List<AlbumByDateDto>?=null,
):Serializable
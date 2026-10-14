package org.aleks616.shrendar.homepage.model

import org.aleks616.shrendar.album.model.AlbumAnniversaryDto
import org.aleks616.shrendar.artist.model.ArtistAnniversaryDto
import java.io.Serializable


data class UpcomingAnniversariesDto(
    val artistBirthdays:List<ArtistAnniversaryDto>?=null,
    val artistDeathAnns:List<ArtistAnniversaryDto>?=null,
    val albums:List<AlbumAnniversaryDto>?=null
):Serializable
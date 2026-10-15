package com.example.client.homepage.dto

import com.example.client.common.EventWikiDto
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@ExperimentalJsExport
@JsExport
@Serializable
data class HomepageMainDto(
    val favoriteAlbums:List<AlbumByDateDto>?=null,
    val favoriteArtistsBirthdays:List<ArtistAnniversaryDto>?=null,
    val favoriteArtistsDeathAnniversaries:List<ArtistAnniversaryDto>?=null,
    val otherBandMembersBirthdays:List<ArtistAnniversaryDto>?=null,
    val otherBandMembersDeathAnniversaries:List<ArtistAnniversaryDto>?=null,
    val recommendedArtistBirthdays:List<ArtistAnniversaryDto>?=null,
    val recommendedArtistDeathAnniversaries:List<ArtistAnniversaryDto>?=null,
    val recommendedAlbumsAnniversaries:List<AlbumByDateDto>?=null,
    val eventAnniversaries:List<EventWikiDto>?=null,
)
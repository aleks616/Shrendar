package org.aleks616.shrendar.band.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.ArtistBandsDto}
 */
data class ArtistBandsStatusDto(
    val artistId:Long?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val current:Boolean?=null,
):Serializable
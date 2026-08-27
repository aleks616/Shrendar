package org.aleks616.shrendar.band.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.BandsMembers}
 */
data class ArtistBandsDto(
    val id:Int?=null,
    val artistId:Long?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val role:String?=null,
    val joinedYear:Int?=null,
    val leftYear:Int?=null,
    val nickname:String?=null
):Serializable
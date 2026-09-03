package org.aleks616.shrendar.artist.model

import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.artist.model.ArtistBirthdayDeathDateDto}
 */
data class ArtistAnniversaryDto(
    val id:Long?=null,
    val name:String?=null,
    val anniversaryDate:LocalDate?=null,
    val daysTillAnniversary:Int?=null,
    val yearsSince:Int?=null,
    val country:String?=null
):Serializable
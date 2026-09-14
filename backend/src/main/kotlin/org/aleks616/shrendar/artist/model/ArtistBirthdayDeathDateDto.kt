package org.aleks616.shrendar.artist.model

import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.artist.model.ArtistWikiDto}
 */
data class ArtistBirthdayDeathDateDto(
    val id:Long?=null,
    val name:String?=null,
    val birthDate:LocalDate?=null,
    val daysTillBirthday:Int?=null,
    val deathDate:LocalDate?=null,
    val daysTillDeathAnniversary:Int?=null,
    val age:Int?=null,
    val ageAtDeath:Int?=null,
    val country:String?=null
):Serializable
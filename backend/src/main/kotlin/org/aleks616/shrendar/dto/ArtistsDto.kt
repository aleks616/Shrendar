package org.aleks616.shrendar.dto

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.entities.Artists}
 */
data class ArtistsBirthDayDto(
    val id:Int?=null,
    val name:String?=null,
    val birthDay:Int?=null,
    val birthMonth:Int?=null,
    val birthYear:Int?=null,
    val age:Int?=null
    ):Serializable
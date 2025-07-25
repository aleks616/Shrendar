package org.aleks616.shrendar.dto

import java.io.Serializable
import java.time.LocalDate

/**
 * DTO for {@link org.aleks616.shrendar.entities.Event}
 */
data class EventBandDto(
    val id:Int?=null,
    val band:BandsDto?=null,
    val date:LocalDate?=null,
    val name:String?=null,
    val description:String?=null,
    val yearsSince:Int?=null
):Serializable{
    /**
     * DTO for {@link org.aleks616.shrendar.entities.Bands}
     */
    data class BandsDto(
        val id:Int?=null,
        val name:String?=null
    ):Serializable
}
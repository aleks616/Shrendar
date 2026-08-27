package org.aleks616.shrendar.band.model

import org.aleks616.shrendar.common.model.CountryDto
import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.Band}
 */
data class BandDto(
    val id:Int?=null,
    val name:String?=null,
    val formedYear:Int?=null,
    val disbandedYear:Int?=null,
    val status:Status?=null,
    val country:CountryDto?=null,
    val description:String?=null
):Serializable
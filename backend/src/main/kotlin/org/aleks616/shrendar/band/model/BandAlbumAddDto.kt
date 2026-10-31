package org.aleks616.shrendar.band.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.Band}
 */
data class BandAlbumAddDto(
    val id:Int=0,
    val name:String?=null,
    val formedYear:Int?=null,
    val disbandedYear:Int?=null,
    val country:String?=null
):Serializable
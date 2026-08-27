package org.aleks616.shrendar.band.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.Band}
 * COUNTRY IS STRING NOT NAME
 */
data class FavoriteBandDto(val id:Int?=null,val name:String?=null,val country:String?=null):Serializable
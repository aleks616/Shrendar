package org.aleks616.shrendar.band.model

import java.io.Serializable

data class ArtistBandsHistoryDto(
    val memberId:Long?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val nickname:String?=null,
    val yearRole:MutableList<String>?=null
):Serializable
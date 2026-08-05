package org.aleks616.shrendar.band.model

import java.io.Serializable

data class BandsMembersDto(
    val id:Int?=null,
    val artistId:Int?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val nickname:String?=null,
    var yearRole:MutableList<String>?=null,
):Serializable

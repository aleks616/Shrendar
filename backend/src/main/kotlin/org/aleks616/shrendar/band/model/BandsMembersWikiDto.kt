package org.aleks616.shrendar.band.model

import java.io.Serializable

data class BandsMembersWikiDto(
    val id:Long?=null,
    val artistId:Long?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val nickname:String?=null,
    var yearRole:MutableList<String>?=null,
):Serializable
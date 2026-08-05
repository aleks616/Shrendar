package org.aleks616.shrendar.band.model

import java.io.Serializable

data class BandsMembersDataExtendedDto(
    val id:Int?=null,
    val artistId:Int?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    var role:String?=null,
    var joinedYear:Int?=null,
    var leftYear:Int?=null,
    val nickname:String?=null,
    var yearRole:MutableList<String>?=mutableListOf(),
):Serializable

package org.aleks616.shrendar.band.model

import java.io.Serializable


data class ArtistBandsExtendedDto(
    val id:Int?=null,
    val artistId:Int?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val role:String?=null,
    val joinedYear:Int?=null,
    val leftYear:Int?=null,
    val nickname:String?=null,
    val yearRole:MutableList<String>?=null
):Serializable
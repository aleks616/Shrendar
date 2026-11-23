package org.aleks616.shrendar.artist.model

import java.io.Serializable

data class ArtistMemberAddDto(
    val id:Long=0,
    val name:String?=null,
    val birthYear:Int?=null,
    val deathYear:Int?=null,
    val country:String?=null
):Serializable
package org.aleks616.shrendar.band.model

import java.io.Serializable

data class ArtistBandAddDto(
    var id:Long?=null,
    var bandId:Int?=null,
    var artistId:Long?=null,
    var nickname:String?=null,
    var role:String?=null,
    var joinedYear:Int?=null,
    var leftYear:Int?=null
):Serializable

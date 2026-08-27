package org.aleks616.shrendar.band.model

import java.io.Serializable

/**
 * DTO for {@link org.aleks616.shrendar.band.model.BandsMembers}
 */
data class BandsMembersDataDto(
    val id:Long?=null,
    val artistId:Long?=null,
    val artistName:String?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    var role:String?=null,
    var joinedYear:Int?=null,
    var leftYear:Int?=null,
    val nickname:String?=null,
):Serializable



package org.aleks616.shrendar.user.model

import java.io.Serializable

data class UserDto(
    val login:String?=null,
    val username:String?=null,
    val rankId:Int?=null,
    val xp:Int?=null,
):Serializable
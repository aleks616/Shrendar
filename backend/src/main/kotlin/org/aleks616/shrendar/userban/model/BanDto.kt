package org.aleks616.shrendar.userban.model

import java.io.Serializable

data class BanDto(
    val userId:Int?=null,
    val duration:Int?=null,
    val description:String?=null,
):Serializable
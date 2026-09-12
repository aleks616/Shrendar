package org.aleks616.shrendar.user.model

import java.io.Serializable

data class ReportRequestDto(
    val reportedUserId:Int=0,
    val reason:String?=null
):Serializable
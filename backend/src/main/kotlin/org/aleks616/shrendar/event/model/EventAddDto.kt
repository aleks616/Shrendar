package org.aleks616.shrendar.event.model

import java.io.Serializable
import java.time.LocalDate

data class EventAddDto(
    val id:Int?=null,
    val bandId:Int?=null,
    val date:LocalDate?=null,
    val name:String?=null,
    val description:String?=null
):Serializable

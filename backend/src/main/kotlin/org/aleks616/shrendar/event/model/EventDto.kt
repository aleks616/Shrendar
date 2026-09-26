package org.aleks616.shrendar.event.model

import java.io.Serializable
import java.time.LocalDate

data class EventDto(
    val id:Int?=null,
    val bandId:Int?=null,
    val bandName:String?=null,
    val date:LocalDate?=null,
    val name:String?=null,
    val description:String?=null,
    val yearsSince:Int?=null
):Serializable
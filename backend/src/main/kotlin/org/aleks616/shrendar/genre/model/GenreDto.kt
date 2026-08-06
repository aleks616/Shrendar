package org.aleks616.shrendar.genre.model

import java.io.Serializable

data class GenreDto(
    val id:Int?=null,
    val name:String?=null,
    val value:Byte?=null
):Serializable

data class GenreDto1(
    val id:Int?=null,
    val name:String?=null,
    var value:Long?=null
):Serializable
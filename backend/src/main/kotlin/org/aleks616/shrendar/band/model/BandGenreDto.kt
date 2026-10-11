package org.aleks616.shrendar.band.model

data class BandGenreDto(
    var id:Int?=null,
    var name:String?=null,
    var formedYear:Int?=null,
    var country:String?=null,
    var similarity:Double?=null
)
package org.aleks616.shrendar.band.model

import org.aleks616.shrendar.common.model.CountryDto

data class BandGenreDto(
    var id:Int?=null,
    var name:String?=null,
    var formedYear:Int?=null,
    var country:CountryDto?=null,
    var similarity:Double?=null
)
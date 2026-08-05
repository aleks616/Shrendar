package org.aleks616.shrendar.band.model

import java.io.Serializable


data class BandAddDto(
    var id:Int?=null,
    var name:String?=null,
    var formedYear:Int?=null,
    var status:Status?=null,
    var disbandedYear:Int?=null,
    var country:Int?=null,
    var description:String?=null,
    var imageUrl:String?
):Serializable
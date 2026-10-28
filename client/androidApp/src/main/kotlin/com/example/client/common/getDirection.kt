package com.example.client.common

import com.ramcosta.composedestinations.generated.destinations.*
import com.ramcosta.composedestinations.spec.Direction

fun getDirection(url:String?):Direction? {
    if(url==null) return null
    val itemType=url.split("/")[1]
    val itemId=url.split("/")[2]
    return when(itemType) {
        "album"->AlbumDataViewDestination(itemId.toLong())
        "artist"->ArtistDataViewDestination(itemId.toLong())
        "band"->BandDataViewDestination(itemId.toInt())
        "event"->EventDataViewDestination(itemId.toInt())
        "u"->ProfilePageViewDestination(itemId)
        else->null
    }
}

fun getDirection(type:String?,id:String?):Direction? {
    if(type.isNullOrEmpty()||id.isNullOrEmpty()) return null
    return when(type) {
        "album"->AlbumDataViewDestination(id.toLong())
        "artist"->ArtistDataViewDestination(id.toLong())
        "band"->BandDataViewDestination(id.toInt())
        "event"->EventDataViewDestination(id.toInt())
        "u"->ProfilePageViewDestination(id)
        else->null
    }
}
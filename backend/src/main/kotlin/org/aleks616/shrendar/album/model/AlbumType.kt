package org.aleks616.shrendar.album.model

enum class AlbumType {
    STUDIO,
    EP,
    COMPILATION,
    CONCERT,
    DEMO,
    SINGLE,
    OTHER;

    fun isNullOrEmpty():Boolean {
        return this.toString().isEmpty()
    }
}

fun AlbumType?.isNullOrEmpty():Boolean {
    return this?.isNullOrEmpty()?:true
}


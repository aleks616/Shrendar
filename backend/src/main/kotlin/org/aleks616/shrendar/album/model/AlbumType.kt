package org.aleks616.shrendar.album.model

enum class AlbumType {
    studio,
    EP,
    compilation,
    concert,
    demo,
    single,
    other;

    fun isNullOrEmpty():Boolean {
        return this.toString().isEmpty()
    }
}

fun AlbumType?.isNullOrEmpty():Boolean {
    return this?.isNullOrEmpty()?:true
}


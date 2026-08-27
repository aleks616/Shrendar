package org.aleks616.shrendar.user.repository

import org.aleks616.shrendar.artist.model.Artist
import org.aleks616.shrendar.user.model.User
import org.aleks616.shrendar.user.model.UsersArtists
import org.springframework.data.jpa.repository.JpaRepository

interface
UserArtistRepository:JpaRepository<UsersArtists,Long> {
    fun findByArtistAndUser(artist:Artist,user:User):UsersArtists?
    fun findByUser(user:User):MutableList<UsersArtists>
}
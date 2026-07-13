package org.aleks616.shrendar.album.repository

import org.aleks616.shrendar.album.model.Albums
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AlbumRepository :JpaRepository<Albums,Int>
package com.example.client.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.client.LocalText
import com.example.client.common.FavoritesTable
import com.example.client.common.FavoriteArtistDto
import dev.icerock.moko.resources.compose.stringResource
import kotlin.collections.contains
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.mapIndexedNotNull
import kotlin.collections.orEmpty
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun FavoriteArtistsTable(
    favoriteArtists:List<FavoriteArtistDto>?,
    currentUser:Boolean,
    favoriteIds:Set<Long>,
    onToggle:(Long)->Unit,
) {
    val rows=favoriteArtists.orEmpty()
    val headers=buildList {
        if(currentUser) add(stringResource(LocalText().getStringResource("toggle")))
        add(stringResource(LocalText().getStringResource("artist_name")))
        add(stringResource(LocalText().getStringResource("bands")))
    }
    val tableRows=rows.map {item->
        val bands=item.bands.orEmpty()
        val currentBands=bands.filter {it.current==true}
        val pastBands=bands.filter {it.current==false}
        val bandsText=buildString {
            append(currentBands.joinToString {it.bandName.orEmpty()}.ifBlank {"-"})
            if(pastBands.isNotEmpty()) {
                append("\n")
                append(stringResource(LocalText().getStringResource("past")))
                append(": ")
                append(pastBands.joinToString {it.bandName.orEmpty()})
            }
        }
        buildList {
            if(currentUser) add("")
            add(item.name.orEmpty())
            add(bandsText)
        }
    }
    FavoritesTable(
        headers=headers,
        rows=tableRows,
        maxHeight=480.dp,
        emptyText=stringResource(LocalText().getStringResource("no_favorite_artists")),
        favoriteEnabled=currentUser,
        favoriteRows=rows.mapIndexedNotNull {index,item->
            if(item.id in favoriteIds) index else null
        }.toSet(),
        onFavoriteClick={index-> rows[index].id?.let(onToggle)}
    )
}

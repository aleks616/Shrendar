package com.example.client.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.client.LocalText
import com.example.client.common.FavoritesTable
import com.example.client.common.FavoriteGenreDto
import dev.icerock.moko.resources.compose.stringResource
import kotlin.collections.contains
import kotlin.collections.map
import kotlin.collections.mapIndexedNotNull
import kotlin.collections.orEmpty
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun FavoriteGenresTable(
    favoriteGenres:List<FavoriteGenreDto>?,
    currentUser:Boolean=false,
    favoriteIds:Set<Int>,
    onToggle:(Int)->Unit,
) {
    val rows=favoriteGenres.orEmpty()
    val headers=buildList {
        if(currentUser) add(stringResource(LocalText().getStringResource("toggle")))
        add(stringResource(LocalText().getStringResource("genre")))
    }
    val tableRows=rows.map {item->
        buildList {
            if(currentUser) add("")
            add(item.name.orEmpty())
        }
    }
    FavoritesTable(
        headers=headers,
        rows=tableRows,
        maxHeight=480.dp,
        emptyText=stringResource(LocalText().getStringResource("no_favorite_genres")),
        favoriteEnabled=currentUser,
        favoriteRows=rows.mapIndexedNotNull {index,item->
            if(item.id in favoriteIds) index else null
        }.toSet(),
        onFavoriteClick={index-> rows[index].id?.let(onToggle)}
    )
}
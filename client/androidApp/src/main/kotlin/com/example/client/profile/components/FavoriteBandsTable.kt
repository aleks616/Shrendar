package com.example.client.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.client.LocalText
import com.example.client.common.DataTable
import com.example.client.common.FavoriteBandDto
import dev.icerock.moko.resources.compose.stringResource
import kotlin.collections.contains
import kotlin.collections.map
import kotlin.collections.mapIndexedNotNull
import kotlin.collections.orEmpty
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun FavoriteBandsTable(
    favoriteBands:List<FavoriteBandDto>?,
    currentUser:Boolean=false,
    favoriteIds:Set<Int>,
    onToggle:(Int)->Unit,
) {
    val rows=favoriteBands.orEmpty()
    val headers=buildList {
        if(currentUser) add(stringResource(LocalText().getStringResource("toggle")))
        add(stringResource(LocalText().getStringResource("band_name")))
        add(stringResource(LocalText().getStringResource("country")))
        add(stringResource(LocalText().getStringResource("active")))
    }
    val tableRows=rows.map {item->
        buildList {
            if(currentUser) add("")
            add(item.name.orEmpty())
            add(item.country?.let {stringResource(LocalText().getStringResource(it))}?:"-")
            add(item.activeYears?:"-")
        }
    }
    DataTable(
        headers=headers,
        rows=tableRows,
        maxHeight=480.dp,
        emptyText=stringResource(LocalText().getStringResource("no_favorite_bands")),
        favoriteEnabled=currentUser,
        favoriteRows=rows.mapIndexedNotNull {index,item->
            if(item.id in favoriteIds) index else null
        }.toSet(),
        onFavoriteClick={index-> rows[index].id?.let(onToggle)}
    )
}
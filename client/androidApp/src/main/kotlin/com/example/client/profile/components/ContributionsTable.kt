package com.example.client.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.client.LocalText
import com.example.client.common.DataTable
import com.example.client.profile.UserProfileDto
import dev.icerock.moko.resources.compose.stringResource
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun ContributionsTable(user:UserProfileDto) {
    val rows=user.contributions.orEmpty()
    val headers=listOf(
        stringResource(LocalText().getStringResource("action")),
        stringResource(LocalText().getStringResource("date")),
        stringResource(LocalText().getStringResource("table")),
        stringResource(LocalText().getStringResource("column")),
        stringResource(LocalText().getStringResource("confirmed")),
        stringResource(LocalText().getStringResource("before")),
        stringResource(LocalText().getStringResource("after"))
    )
    val tableRows=rows.map {item->
        listOf(
            item.action?.name?.lowercase()?.replaceFirstChar {it.uppercase()}?:"-",
            item.changedAt?:"-",
            item.changedTable?:"-",
            item.changedColumn?:"-",
            if(item.confirmed==true) "✓" else "✕",
            item.oldValue?:"-",
            item.newValue?:"-"
        )
    }
    DataTable(
        headers=headers,
        rows=tableRows,
        maxHeight=480.dp,
        minColumnWidth=120.dp,
        cellPadding=8.dp,
        emptyText=stringResource(LocalText().getStringResource("no_contributions"))
    )
}
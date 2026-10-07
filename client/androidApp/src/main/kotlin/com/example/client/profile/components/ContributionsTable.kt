package com.example.client.profile.components

import androidx.compose.runtime.Composable
import com.example.client.LocalText
import com.example.client.common.ContributionDto
import com.example.client.common.Table
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun ContributionsTable(contributions:List<ContributionDto>?) {
    Table(
        columns=listOf(
            Pair("action",ContributionDto::action),
            Pair("date",ContributionDto::changedAt),
            Pair("table",ContributionDto::changedTable),
            Pair("column",ContributionDto::changedColumn),
            Pair("confirmed",ContributionDto::confirmed),
            Pair("before",ContributionDto::oldValue),
            Pair("after",ContributionDto::newValue),
        ),
        data=contributions.orEmpty(),
        emptyText=stringResource(LocalText().getStringResource("no_contributions")),
    )
}
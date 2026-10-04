package com.example.client.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun DataTableCell(
    width:Dp,
    cellPadding:Dp,
    content:@Composable ()->Unit,
) {
    Box(
        modifier=Modifier
            .width(width)
            .padding(horizontal=cellPadding),
        contentAlignment=Alignment.CenterStart
    ) {
        content()
    }
}
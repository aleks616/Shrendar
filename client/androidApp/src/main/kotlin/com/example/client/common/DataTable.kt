package com.example.client.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.client.MR
import com.example.client.star
import com.example.client.star_filled
import com.example.client.tray
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun DataTable(
    headers:List<String>,
    rows:List<List<String>>,
    maxHeight:Dp?=null,
    minColumnWidth:Dp=80.dp,
    cellPadding:Dp=4.dp,
    emptyText:String,
    favoriteEnabled:Boolean=false,
    favoriteRows:Set<Int> =emptySet(),
    onFavoriteClick:(Int)->Unit={},
) {
    val textMeasurer=rememberTextMeasurer()
    val textStyle=LocalTextStyle.current
    val density=LocalDensity.current
    val columnWidths=remember(headers,rows,textStyle,density) {
        headers.indices.map {columnIndex->
            val values=buildList {
                add(headers[columnIndex])
                rows.forEach {row-> add(row.getOrElse(columnIndex) {""})}
            }
            val width=values.maxOf {value->
                textMeasurer.measure(AnnotatedString(value),style=textStyle).size.width
            }
            with(density) {
                maxOf(width.toDp()+(cellPadding*2),minColumnWidth)
            }
        }
    }
    val measuredTableWidth=columnWidths.fold(0.dp) {total,width-> total+width}
    val horizontalScrollState=rememberScrollState()
    val verticalScrollState=rememberScrollState()

    Card(
        colors=CardDefaults.cardColors(containerColor=Color.Transparent),
        modifier=Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(modifier=Modifier.fillMaxWidth()) {
            val extraWidth=if(measuredTableWidth<maxWidth) {
                (maxWidth-measuredTableWidth)/columnWidths.size
            }
            else {
                0.dp
            }
            val tableWidths=columnWidths.map {it+extraWidth}
            val tableWidth=tableWidths.fold(0.dp) {total,width-> total+width}

            Column(
                modifier=Modifier.width(tableWidth)
            ) {
                Row(
                    modifier=Modifier
                        .width(tableWidth)
                        .horizontalScroll(horizontalScrollState)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical=8.dp),
                    verticalAlignment=Alignment.CenterVertically
                ) {
                    headers.forEachIndexed {index,title->
                        DataTableCell(tableWidths[index],cellPadding) {
                            Text(
                                title,
                                color=MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Divider()
                Column(
                    modifier=Modifier
                        .width(tableWidth)
                        .then(
                            maxHeight?.let {
                                Modifier
                                    .heightIn(max=it)
                                    .verticalScroll(verticalScrollState)
                            }?:Modifier
                        )
                        .horizontalScroll(horizontalScrollState)
                ) {
                    if(rows.isEmpty()) {
                        Column(
                            modifier=Modifier
                                .width(tableWidth)
                                .padding(vertical=24.dp),
                            horizontalAlignment=Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter=painterResource(MR.images.tray),
                                contentDescription="no data",
                                modifier=Modifier.size(28.dp),
                                tint=MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                emptyText,
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign=TextAlign.Center
                            )
                        }
                    }
                    rows.forEachIndexed {rowIndex,row->
                        DataTableRow(tableWidth) {
                            row.forEachIndexed {columnIndex,value->
                                DataTableCell(tableWidths[columnIndex],cellPadding) {
                                    if(favoriteEnabled&&columnIndex==0&&rowIndex in favoriteRows) {
                                        IconButton(onClick={onFavoriteClick(rowIndex)}) {
                                            Icon(
                                                painter=painterResource(MR.images.star_filled),
                                                contentDescription="favorite",
                                                modifier=Modifier.size(24.dp),
                                                tint=MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    else if(favoriteEnabled&&columnIndex==0) {
                                        IconButton(onClick={onFavoriteClick(rowIndex)}) {
                                            Icon(
                                                painter=painterResource(MR.images.star),
                                                contentDescription="favorite",
                                                modifier=Modifier.size(24.dp),
                                                tint=MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    else {
                                        Text(value,color=MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
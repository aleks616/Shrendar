package com.example.client.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.client.LocalText
import com.example.client.MR
import com.example.client.tray
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlin.reflect.KProperty1

private fun Any?.tableText():String = when(this) {
    null->"-"
    is Boolean->if(this) "✓" else "✕"
    else->toString()
}

@Composable
fun <T> Table(
    columns:List<Pair<String,KProperty1<T,*>>>,
    data:List<T>,
    emptyText:String,
) {
    val textMeasurer=rememberTextMeasurer()
    val bodyTextStyle=LocalTextStyle.current
    val headerTextStyle=MaterialTheme.typography.labelLarge
    val density=LocalDensity.current
    val horizontalScrollState=rememberScrollState()
    val verticalScrollState=rememberScrollState()
    val headers=columns.map {column->
        stringResource(LocalText().getStringResource(column.first))
    }

    val columnWidths=remember(
        columns,
        headers,
        data,
        bodyTextStyle,
        headerTextStyle,
        density,
    ) {
        headers.mapIndexed {index,header->
            val headerWidth=textMeasurer.measure(
                AnnotatedString(header),
                style=headerTextStyle,
            ).size.width

            val dataWidth=data.maxOfOrNull {item->
                textMeasurer.measure(
                    AnnotatedString(columns[index].second.get(item).tableText()),
                    style=bodyTextStyle,
                ).size.width
            }?:0

            with(density) {
                maxOf(headerWidth,dataWidth).toDp()+16.dp
            }
        }
    }

    val tableWidth=columnWidths.fold(0.dp) {total,width->
        total+width
    }

    Box(
        modifier=Modifier.fillMaxWidth(),
        contentAlignment=Alignment.Center,
    ) {
        Surface(
            modifier=Modifier.fillMaxWidth(),
            shape=RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier=Modifier
                    .width(tableWidth)
                    .horizontalScroll(horizontalScrollState),
            ) {
                Row(
                    modifier=Modifier
                        .width(tableWidth)
                        .height(IntrinsicSize.Min)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                ) {
                    columns.forEachIndexed {index,_->
                        Box(
                            modifier=Modifier
                                .width(columnWidths[index])
                                .height(IntrinsicSize.Min),
                        ) {
                            Text(
                                modifier=Modifier.padding(horizontal=8.dp,vertical=12.dp),
                                text=headers[index],
                                color=MaterialTheme.colorScheme.onSecondary,
                                style=headerTextStyle,
                            )
                            if(index<columns.lastIndex) {
                                Divider(
                                    modifier=Modifier
                                        .align(Alignment.CenterEnd)
                                        .fillMaxHeight()
                                        .padding(vertical=8.dp)
                                        .width(1.dp),
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier=Modifier
                        .width(tableWidth)
                        .heightIn(max=500.dp)
                        .verticalScroll(verticalScrollState),
                ) {
                    if(data.isEmpty()) {
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
                    data.forEach {item->
                        Row(
                            modifier=Modifier.width(tableWidth),
                            verticalAlignment=Alignment.CenterVertically,
                        ) {
                            columns.forEachIndexed {index,column->
                                Box(
                                    modifier=Modifier
                                        .width(columnWidths[index])
                                        .padding(horizontal=8.dp,vertical=12.dp),
                                ) {
                                    Text(
                                        text=column.second.get(item).tableText(),
                                    )
                                }
                            }
                        }

                        Divider()
                    }
                }
            }
        }
    }
}
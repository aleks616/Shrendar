package com.example.client.common

import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Tabs(
    labels:List<String>,
    selectedIndex:Int,
    onSelected:(Int)->Unit,
    modifier:Modifier=Modifier,
    content:@Composable (Int)->Unit,
) {
    if(labels.isEmpty()) return

    val currentIndex=selectedIndex.coerceIn(0,labels.lastIndex)

    Column(
        modifier=modifier,
        verticalArrangement=Arrangement.spacedBy(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex=currentIndex,
            edgePadding=0.dp,
            containerColor=Color.Transparent,
            divider={
                Divider(color=MaterialTheme.colorScheme.outlineVariant)
            },
            indicator={tabPositions ->
                TabRowDefaults.Indicator(
                    modifier=Modifier.tabIndicatorOffset(tabPositions[currentIndex]),
                    color=MaterialTheme.colorScheme.primary
                )
            }
        ) {
            labels.forEachIndexed {index,label->
                Tab(
                    selected=index==currentIndex,
                    onClick={onSelected(index)},
                    text={
                        Text(
                            label,
                            color=if(index==currentIndex) {
                                MaterialTheme.colorScheme.onSurface
                            }
                            else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )
            }
        }
        content(currentIndex)
    }
}

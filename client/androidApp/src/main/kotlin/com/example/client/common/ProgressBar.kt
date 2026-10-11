package com.example.client.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressBar(
    progress:Float,
    startText:String,
    endText:String,
    modifier:Modifier=Modifier,
) {
    Column(
        modifier=modifier,
        verticalArrangement=Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.SpaceBetween
        ) {
            Text(startText)
            Text(endText)
        }
        LinearProgressIndicator(
            progress=progress,
            modifier=Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
    }
}

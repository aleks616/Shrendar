package com.example.client

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource


@Composable
@Preview
fun BackButton(
    onBack:()->Unit={},
){
    IconButton(onClick=onBack) {
        Icon(
            painter=painterResource(MR.images.arrow_left),
            contentDescription="back",
            modifier=Modifier.size(24.dp),
            tint=MaterialTheme.colorScheme.primary
        )
    }
}
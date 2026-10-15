package com.example.client.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import com.example.client.common.LoadImage
import com.example.client.homepage.dto.AlbumByDateDto
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun AlbumCard(album:AlbumByDateDto) {
    AppTheme {
        Surface(
            modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            color=MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f),
        ) {
            Column(
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement=Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier=Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(text="${album.yearsSince} 🎂",fontSize=20.sp,fontWeight=FontWeight.Bold)
                    Text(text="${album.band?.name} - ${album.title}",fontSize=18.sp,fontWeight=FontWeight.Bold)
                }
                Column(modifier=Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text(text="${stringResource(MR.strings.release_date)}: ${album.releaseDate.toString()}")
                    Text(text="${stringResource(MR.strings.album_type)}: ${album.type.toString()}")
                    if(album.genre!=null){
                        Text(text="${stringResource(MR.strings.main_genre)}: ${album.genre?.name}")
                    }
                    LoadImage(
                        imageUrl=album.artworkUrl,
                        contentDescription=album.title.orEmpty(),
                        width=288.dp,
                    )
                }
            }
        }
    }
}
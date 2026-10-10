package com.example.client.album.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.client.AppTheme
import com.example.client.LocalText
import com.example.client.album.AlbumClient
import com.example.client.album.AlbumWikiDto
import com.example.client.common.LoadImage
import com.example.client.common.TranslatedDescription
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun AlbumDataView(
    albumId:Long=255,
) {
    var album by remember(albumId) {mutableStateOf<AlbumWikiDto?>(null)}
    var isLoading by remember(albumId) {mutableStateOf(true)}

    LaunchedEffect(albumId) {
        isLoading=true
        album=try {
            AlbumClient.getAlbumWikiPageData(albumId)
        }
        catch(_:Exception) {
            null
        }
        finally {
            isLoading=false
        }
    }

    AppTheme {
        Surface(
            modifier=Modifier.fillMaxSize(),
            color=MaterialTheme.colorScheme.background,
        ) {
            when {
                isLoading->Box(
                    modifier=Modifier.fillMaxSize(),
                    contentAlignment=Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                album!=null-> {
                    val albumData=album!!

                    Column(
                        modifier=Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start=12.dp,end=12.dp,top=16.dp,bottom=8.dp),
                        verticalArrangement=Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text=albumData.albumName.orEmpty(),
                            style=MaterialTheme.typography.headlineLarge,
                            fontWeight=FontWeight.Bold,
                        )
                        Row(
                            modifier=Modifier.fillMaxWidth(),
                            horizontalArrangement=Arrangement.spacedBy(16.dp),
                            verticalAlignment=Alignment.Top,
                        ) {
                            Column(
                                modifier=Modifier.fillMaxWidth(),
                                verticalArrangement=Arrangement.spacedBy(8.dp),
                            ) {
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("band"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=albumData.band?.name?:"-",
                                    )
                                }
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("release_date"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=albumData.releaseDate?.toString()?:"-",
                                    )
                                    Text(
                                        text=" "+stringResource(
                                            LocalText().getStringResource("anniversary_in")
                                        ),
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=albumData.daysTillAnniversary?.toString()?:"-",
                                    )
                                    Text(
                                        text=" "+stringResource(LocalText().getStringResource("days")),
                                    )
                                }
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("years_since"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=albumData.albumAge?.toString()?:"-",
                                    )
                                }
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("album_type"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=albumData.type?:"-",
                                    )
                                }
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("genre"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=albumData.genre?.name?:"-",
                                    )
                                }
                            }
                        }

                        LoadImage(
                            imageUrl=albumData.artworkUrl,
                            contentDescription=albumData.albumName.orEmpty(),
                        )

                        TranslatedDescription(description=albumData.description.orEmpty())
                    }
                }
            }
        }
    }
}

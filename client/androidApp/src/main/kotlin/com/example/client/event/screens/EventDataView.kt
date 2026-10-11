package com.example.client.event.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.client.AppTheme
import com.example.client.LocalText
import com.example.client.common.TranslatedDescription
import com.example.client.event.EventClient
import com.example.client.event.EventWikiDto
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun EventDataView(
    eventId:Int=2,
) {
    var event by remember(eventId) {mutableStateOf<EventWikiDto?>(null)}
    var isLoading by remember(eventId) {mutableStateOf(true)}

    LaunchedEffect(eventId) {
        isLoading=true
        event=try {
            EventClient.getEventData(eventId)
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

                event!=null-> {
                    val eventData=event!!

                    Column(
                        modifier=Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start=12.dp,end=12.dp,top=16.dp,bottom=8.dp),
                        verticalArrangement=Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text=eventData.name.orEmpty(),
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
                                        text=eventData.bandName?:"-",
                                    )
                                }
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("date"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=eventData.date?.toString()?:"-",
                                    )
                                    Text(
                                        text=" "+stringResource(
                                            LocalText().getStringResource("anniversary_in")
                                        )+" ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=eventData.daysTillAnniversary?.toString()?:"-",
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
                                        text=eventData.yearsSince?.toString()?:"-",
                                    )
                                }
                            }
                        }

                        TranslatedDescription(description=eventData.description.orEmpty())
                    }
                }
            }
        }
    }
}
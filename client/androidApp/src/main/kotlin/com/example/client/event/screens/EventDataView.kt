package com.example.client.event.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.client.AppTheme
import com.example.client.BackButton
import com.example.client.LocalText
import com.example.client.common.EventWikiDto
import com.example.client.common.TranslatedDescription
import com.example.client.event.EventClient
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.BandDataViewDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class,ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>
fun EventDataView(
    navigator:DestinationsNavigator,
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
            Column(modifier=Modifier.fillMaxSize()) {

                if(isLoading) {
                    Box(
                        modifier=Modifier.fillMaxSize(),
                        contentAlignment=Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                if(event!=null) {
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
                                    TextButton(
                                        modifier=Modifier.padding(0.dp),
                                        onClick={navigator.navigate(BandDataViewDestination(eventData.bandId!!))},
                                    ) {
                                        Text(
                                            text="${stringResource(LocalText().getStringResource("band"))}: ",
                                            color=MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text=eventData.bandName?:"-",
                                        )
                                    }
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
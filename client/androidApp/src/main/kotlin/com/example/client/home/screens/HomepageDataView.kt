package com.example.client.home.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import com.example.client.home.components.*
import com.example.client.homepage.HomepageClient
import com.example.client.homepage.dto.HomepageMainDto
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class,ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>(start=true)
fun HomepageDataView(
    navigator:DestinationsNavigator
) {
    var data by remember {mutableStateOf<HomepageMainDto?>(null)}
    var isLoading by remember {mutableStateOf(true)}
    val context=LocalContext.current

    LaunchedEffect(Unit) {
        isLoading=true
        val token=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            .getString("authToken",null)
        data=try {
            HomepageClient.getTodayAnniversaries(token)
        }
        catch(_:Exception) {
            null
        }
        finally {
            isLoading=false
        }
    }

    val albumCount=(data?.favoriteAlbums?.size?:0)+(data?.recommendedAlbumsAnniversaries?.size?:0)
    val birthdayCount=(data?.favoriteArtistsBirthdays?.size?:0)+(data?.otherBandMembersBirthdays?.size?:0)+(data?.recommendedArtistBirthdays?.size?:0)
    val deathAnnCount=(data?.favoriteArtistsDeathAnniversaries?.size?:0)+(data?.otherBandMembersDeathAnniversaries?.size?:0)+(data?.recommendedArtistDeathAnniversaries?.size?:0)
    val eventCount=data?.eventAnniversaries?.size?:0

    AppTheme {
        Surface(
            modifier=Modifier.fillMaxSize(),
        ) {
            Column(modifier=Modifier.fillMaxSize()) {
                TopAppBar(
                    title={},
                    navigationIcon={
                        BackButton {navigator.popBackStack()}
                    },
                )
                Column(
                    modifier=Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(all=8.dp),
                    verticalArrangement=Arrangement.spacedBy(4.dp),
                ) {
                    if(isLoading) {
                        Box(
                            modifier=Modifier.fillMaxSize(),
                            contentAlignment=Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    if(data!=null){
                        if(albumCount>0){
                            Text(text=stringResource(MR.strings.album_anniversaries),fontSize=26.sp,fontWeight=FontWeight.Bold)
                            data!!.favoriteAlbums?.forEach{album->
                                AlbumCard(album,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                            data!!.recommendedAlbumsAnniversaries?.forEach{album->
                                AlbumCard(album,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                        }
                        Spacer(modifier=Modifier.height(16.dp))

                        if(birthdayCount>0){
                            Text(text=stringResource(MR.strings.birthdays_today),fontSize=26.sp,fontWeight=FontWeight.Bold)
                            data!!.favoriteArtistsBirthdays?.forEach {artist->
                                ArtistBirthdayCard(artist,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                            data!!.otherBandMembersBirthdays?.forEach {artist->
                                ArtistBirthdayCard(artist,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                            data!!.recommendedArtistBirthdays?.forEach {artist->
                                ArtistBirthdayCard(artist,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                        }

                        if(deathAnnCount>0){
                            Text(text=stringResource(MR.strings.died_today),fontSize=26.sp,fontWeight=FontWeight.Bold)
                            data!!.favoriteArtistsDeathAnniversaries?.forEach { artist->
                                ArtistDeathAnniversaryCard(artist,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                            data!!.otherBandMembersDeathAnniversaries?.forEach { artist->
                                ArtistDeathAnniversaryCard(artist,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                            data!!.recommendedArtistDeathAnniversaries?.forEach { artist->
                                ArtistDeathAnniversaryCard(artist,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                        }

                        if(eventCount>0){
                            Text(text=stringResource(MR.strings.events_today),fontSize=26.sp,fontWeight=FontWeight.Bold)
                            data!!.eventAnniversaries?.forEach { event->
                                EventCard(event,navigator)
                                Spacer(modifier=Modifier.height(8.dp))
                            }
                        }
                    }
                    UpcomingAnniversariesView(navigator)
                    CommonBandsView(navigator)
                }
            }
        }
    }
}
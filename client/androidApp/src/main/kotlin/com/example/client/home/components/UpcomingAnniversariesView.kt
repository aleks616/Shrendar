package com.example.client.home.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import com.example.client.homepage.HomepageClient
import com.example.client.homepage.dto.UpcomingAnniversariesDto
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun UpcomingAnniversariesView() {
    var data by remember {mutableStateOf<UpcomingAnniversariesDto?>(null)}
    var isLoading by remember {mutableStateOf(true)}
    val context=LocalContext.current

    LaunchedEffect(Unit) {
        isLoading=true
        val token=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            .getString("authToken",null)
        data=try {
            HomepageClient.getUpcoming(token)
        }
        catch(_:Exception) {
            null
        }
        finally {
            isLoading=false
        }
    }

    AppTheme {
        Surface {
            if(!isLoading&&data!=null) {
                Column(
                    modifier=Modifier.fillMaxWidth(),
                    verticalArrangement=Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text=stringResource(MR.strings.upcoming_album_anniversaries),
                        fontSize=26.sp,
                        fontWeight=FontWeight.Bold,
                        modifier=Modifier.padding(bottom=12.dp)
                    )
                    data?.albums?.forEach {album->
                        Text(text="${album.bandName} - ${album.title}",fontWeight=FontWeight.Bold)
                        Text(
                            text="${album.daysTillAnniversary} "+"${stringResource(MR.strings.time_dp)} "+
                                 "${stringResource(MR.strings.to)} "+stringResource(MR.strings.anniversary)
                        )
                        Text(text="${album.age+1} ${stringResource(MR.strings.time_yp)}")
                        Text(text=album.releaseDate.toString())
                    }
                    Spacer(modifier=Modifier.padding(bottom=8.dp))

                    Text(
                        text=stringResource(MR.strings.upcoming_birthdays),
                        fontSize=26.sp,
                        fontWeight=FontWeight.Bold,
                        modifier=Modifier.padding(bottom=12.dp)
                    )
                    data?.artistBirthdays?.forEach {artist->
                        Text(text=artist.name!!,fontWeight=FontWeight.Bold)
                        Text(
                            text="${artist.daysTillAnniversary} "+"${stringResource(MR.strings.time_dp)} "+
                                 "${stringResource(MR.strings.to)} "+stringResource(MR.strings.birthday)
                        )
                        Text(text="${artist.yearsSince} ${stringResource(MR.strings.time_yp)}")
                        Text(text=artist.anniversaryDate.toString())
                    }
                    Spacer(modifier=Modifier.padding(bottom=8.dp))

                    Text(
                        text=stringResource(MR.strings.upcoming_death_anniversaries),
                        fontSize=26.sp,
                        fontWeight=FontWeight.Bold,
                        modifier=Modifier.padding(bottom=12.dp)
                    )
                    data?.artistDeathAnns?.forEach {artist->
                        Text(text=artist.name!!,fontWeight=FontWeight.Bold)
                        Text(
                            text="${artist.daysTillAnniversary} "+"${stringResource(MR.strings.time_dp)} "+
                                 "${stringResource(MR.strings.to)} "+stringResource(MR.strings.death_anniversary)
                        )
                        Text(text="${artist.yearsSince} ${stringResource(MR.strings.time_yp)}")
                        Text(text=artist.anniversaryDate.toString())
                    }
                }
            }
        }
    }
}
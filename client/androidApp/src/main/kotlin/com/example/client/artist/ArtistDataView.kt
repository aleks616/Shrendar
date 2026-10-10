package com.example.client.artist

import android.content.Context
import android.util.Log
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
import com.example.client.*
import com.example.client.common.LoadImage
import com.example.client.common.Table
import com.example.client.common.TranslatedDescription
import com.example.client.profile.ProfileClient
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import kotlin.collections.orEmpty
import kotlin.js.ExperimentalJsExport

private data class BandTableRow(
    val band:String,
    val yearRole:String
)

@OptIn(ExperimentalJsExport::class)
@Composable
fun ArtistDataView(
    artistId:Long=144
) {
    val context=LocalContext.current
    var artist by remember(artistId) {mutableStateOf<ArtistWikiDto?>(null)}
    var isLoading by remember(artistId) {mutableStateOf(true)}
    var isFavorite by remember {mutableStateOf(false)}
    val token=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
        .getString("authToken",null)
    val scope=rememberCoroutineScope()

    fun toggleArtist(id:Long) {
        scope.launch {
            try {
                ProfileClient.toggleFavoriteArtist(id,token)
                isFavorite=!isFavorite
            }
            catch(e:Exception) {
                Log.e("artist favorite",e.localizedMessage?:"")
            }
        }
    }

    LaunchedEffect(artistId) {
        isLoading=true
        try {
            artist=ArtistClient.getArtistWikiPageDataById(artistId,token)
            isFavorite=artist?.favorite==true
        }
        catch(_:Exception) {

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
            if(isLoading) {
                Box(
                    modifier=Modifier.fillMaxSize(),
                    contentAlignment=Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            val artistData=artist?:ArtistWikiDto()
            val bands=artistData.bands.orEmpty()
            val isDead=artistData.deathDate!=null
            val isLoggedIn=
                context.getSharedPreferences("authToken",Context.MODE_PRIVATE).getString("authToken",null)!=null

            Column(
                modifier=Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start=12.dp,end=12.dp,top=16.dp,bottom=8.dp),
                verticalArrangement=Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.spacedBy(4.dp),
                    verticalAlignment=Alignment.CenterVertically,
                ) {
                    Text(
                        text=artistData.name.orEmpty(),
                        style=MaterialTheme.typography.headlineLarge,
                        fontWeight=FontWeight.Bold,
                    )
                    IconButton(
                        enabled=isLoggedIn,
                        onClick={toggleArtist(artistId)}
                    ) {
                        Icon(
                            painter=if(artistData.favorite) {
                                painterResource(MR.images.star_filled)
                            }
                            else {
                                painterResource(MR.images.star)
                            },
                            contentDescription="favorite",
                            modifier=Modifier.size(28.dp),
                            tint=MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.spacedBy(16.dp),
                    verticalAlignment=Alignment.Top,
                ) {
                    Column(
                        modifier=Modifier.weight(1.1f),
                        verticalArrangement=Arrangement.spacedBy(8.dp),
                    ) {
                        Row {
                            Text(
                                text="${stringResource(LocalText().getStringResource("country"))}: ",
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text=artistData.country?.let {
                                    stringResource(LocalText().getStringResource(it))
                                }?:"-",
                            )
                        }
                        Row {
                            Text(
                                text="${stringResource(LocalText().getStringResource("age"))}: ",
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text=if(artistData.age!=null) artistData.age.toString() else "-",
                            )
                        }
                        Row {
                            Text(
                                text="${stringResource(LocalText().getStringResource("birthday"))}: ",
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text=if(artistData.birthDate!=null) artistData.birthDate.toString() else "-",
                            )
                            Text(
                                text=" "+stringResource(LocalText().getStringResource("next_in")),
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text=if(artistData.daysTillBirthday!=null) artistData.daysTillBirthday.toString() else "-",
                            )
                            Text(
                                text=" "+stringResource(LocalText().getStringResource("days"))
                            )
                        }
                        if(isDead) {
                            Row {
                                Text(
                                    text="${stringResource(LocalText().getStringResource("death_anniversary"))}: ",
                                    color=MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text=artistData.deathDate.toString(),
                                )
                                Text(
                                    text=" "+stringResource(LocalText().getStringResource("next_in")),
                                    color=MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text=artistData.daysTillDeathAnniversary.toString(),
                                )
                                Text(
                                    text=" "+stringResource(LocalText().getStringResource("days"))
                                )
                            }
                        }
                        Row {
                            Text(
                                text="${stringResource(LocalText().getStringResource("gender"))}: ",
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text=artistData.gender?.let {
                                    stringResource(LocalText().getStringResource(it.lowercase()))
                                }?:"-",
                            )
                        }
                        Row {
                            Text(
                                text="${stringResource(LocalText().getStringResource("zodiac_sign"))}: ",
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text=artistData.zodiacSign?.let {
                                    stringResource(LocalText().getStringResource("zodiac_"+it.toString().lowercase()))
                                }?:"-",
                            )
                        }
                        Row {
                            Text(
                                text="${stringResource(LocalText().getStringResource("chinese_zodiac_sign"))}: ",
                                color=MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text=artistData.chineseZodiacSign?.let {
                                    stringResource(LocalText().getStringResource("chinese_zodiac_"+it.toString().lowercase()))
                                }?:"-",
                            )
                        }
                    }
                }
                LoadImage(
                    imageUrl=artistData.artistImageUrl,
                    contentDescription=artistData.name.orEmpty(),
                )

                TranslatedDescription(description=artistData.description.orEmpty())

                Row(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.Center,
                ) {
                    Text(
                        text=stringResource(LocalText().getStringResource("bands")),
                        style=MaterialTheme.typography.headlineSmall
                    )
                }
                Row(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.spacedBy(8.dp),
                ){
                    val bandsTableData=bands.filter{it.bandName!=null}.map{record->
                        BandTableRow(
                            band=record.bandName!!,
                            yearRole=record.yearRole.orEmpty().joinToString("\n") {role->
                                role.replace("guitar",context.getString(LocalText().getStringResource("guitar").resourceId))
                                    .replace("bass",context.getString(LocalText().getStringResource("bass").resourceId))
                                    .replace("drums",context.getString(LocalText().getStringResource("drums").resourceId))
                                    .replace("backing vocals",context.getString(LocalText().getStringResource("backing_vocals").resourceId))
                                    .replace("vocals",context.getString(LocalText().getStringResource("vocals").resourceId))
                            },
                        )
                    }
                    Table(
                        columns=listOf(
                            Pair("bands",BandTableRow::band),
                            Pair("role",BandTableRow::yearRole),
                        ),
                        data=bandsTableData,
                        emptyText=stringResource(
                            LocalText().getStringResource("no_bands")
                        ),
                    )
                }
            }

        }
    }

}
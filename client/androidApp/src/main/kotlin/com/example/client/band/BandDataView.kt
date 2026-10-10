package com.example.client.band

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.client.*
import com.example.client.common.Table
import com.example.client.common.Tabs
import com.example.client.common.TranslatedDescription
import com.example.client.profile.ProfileClient
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.js.ExperimentalJsExport

private data class MemberTableRow(
    val personName:String?,
    val yearRole:String,
)

private data class AlbumTableRow(
    val title:String,
    val releaseDate:String?,
    val albumType:String?,
    val mainGenre:String,
)

private data class SimilarBandTableRow(
    val bandName:String?,
    val formedYear:Int?,
    val country:String?,
)

@Composable
private fun LoadImage(
    imageUrl:String?,
    contentDescription:String,
) {
    if(imageUrl.isNullOrBlank()) return

    var bitmap by remember(imageUrl) {mutableStateOf<Bitmap?>(null)}
    var isLoading by remember(imageUrl) {mutableStateOf(true)}

    LaunchedEffect(imageUrl) {
        bitmap=withContext(Dispatchers.IO) {
            var connection:HttpURLConnection?=null
            try {
                connection=URL(imageUrl).openConnection() as HttpURLConnection
                connection.connectTimeout=10_000
                connection.readTimeout=10_000
                connection.doInput=true
                connection.connect()
                connection.inputStream.use {input->
                    BitmapFactory.decodeStream(input)
                }
            }
            catch(_:IOException) {
                null
            }
            finally {
                connection?.disconnect()
            }
        }
        isLoading=false
    }

    Box(
        modifier=Modifier.fillMaxWidth(),
        contentAlignment=Alignment.Center,
    ) {
        when {
            isLoading->CircularProgressIndicator(modifier=Modifier.size(48.dp))
            bitmap!=null->Image(
                bitmap=bitmap!!.asImageBitmap(),
                contentDescription=contentDescription,
                contentScale=ContentScale.Fit,
                modifier=Modifier
                    .fillMaxWidth()
                    .widthIn(max=300.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )

            else->Box(modifier=Modifier.size(180.dp))
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@Composable
fun BandDataView(
    bandId:Int=21,
) {
    val context=LocalContext.current
    var band by remember(bandId) {mutableStateOf<BandWikiDto?>(null)}
    var isLoading by remember(bandId) {mutableStateOf(true)}
    var selectedTab by remember {mutableStateOf(0)}
    var selectedMemberFilter by remember {mutableStateOf(0)}
    var selectedAlbumFilter by remember {mutableStateOf(0)}
    var showingGenreInformation by remember {mutableStateOf(false)}
    var isFavorite by remember {mutableStateOf(false)}
    val token=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
        .getString("authToken",null)
    val scope=rememberCoroutineScope()

    fun toggleBand(id:Int) {
        scope.launch {
            try {
                ProfileClient.toggleFavoriteBand(id,token)
                isFavorite=!isFavorite
            }
            catch(e:Exception) {
                Log.e("band favorite",e.localizedMessage?:"")
            }
        }
    }

    fun toggleArtistFavoriteAll(bandId:Int) {
        scope.launch {
            try {
                ProfileClient.toggleFavoriteArtistAll(bandId,token)
            }
            catch(e:Exception) {
                Log.e("band favorite all",e.localizedMessage?:"")
            }
        }
    }

    LaunchedEffect(bandId) {
        isLoading=true
        val token=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            .getString("authToken",null)
        band=try {
            BandClient.getBandWikiPageDataById(bandId,token)
        }
        catch(_:Exception) {
            null
        }
        finally {
            isLoading=false
            isFavorite=band?.favorite==true
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

                band!=null-> {
                    val bandData=band!!
                    val members=bandData.bandMembers.orEmpty()
                    val currentMembers=members.filter {
                        it.yearRole.orEmpty().any {role-> role.contains("-)")}
                    }
                    val pastMembers=members.filter {
                        it.yearRole.orEmpty().none {role-> role.contains("-)")}
                    }
                    val showingMembers=when(selectedMemberFilter) {
                        1->currentMembers
                        2->pastMembers
                        else->members
                    }.map {member->
                        MemberTableRow(
                            personName=member.artistName,
                            yearRole=member.yearRole.orEmpty().joinToString("\n") {role->
                                role
                                    .replace(
                                        "guitar",
                                        context.getString(
                                            LocalText().getStringResource("guitar").resourceId
                                        )
                                    )
                                    .replace(
                                        "bass",
                                        context.getString(
                                            LocalText().getStringResource("bass").resourceId
                                        )
                                    )
                                    .replace(
                                        "drums",
                                        context.getString(
                                            LocalText().getStringResource("drums").resourceId
                                        )
                                    )
                                    .replace(
                                        "backing vocals",
                                        context.getString(
                                            LocalText().getStringResource("backing_vocals").resourceId
                                        ),
                                    )
                                    .replace(
                                        "vocals",
                                        context.getString(
                                            LocalText().getStringResource("vocals").resourceId
                                        )
                                    )
                            },
                        )
                    }
                    val albums=bandData.albums.orEmpty()
                    val showingAlbums=(if(selectedAlbumFilter==1) {
                        albums.filter {it.type=="Studio"}
                    }
                    else {
                        albums
                    }).map {album->
                        AlbumTableRow(
                            title=album.title,
                            releaseDate=album.releaseDate?.toString(),
                            albumType=album.type,
                            mainGenre=album.genreName,
                        )
                    }
                    val similarBands=bandData.similar.orEmpty().map {similarBand->
                        SimilarBandTableRow(
                            bandName=similarBand.name,
                            formedYear=similarBand.formedYear,
                            country=similarBand.country?.let {
                                context.getString(LocalText().getStringResource(it).resourceId)
                            },
                        )
                    }
                    val statusColor=when(bandData.status) {
                        "Active"->Color.Green
                        "Disbanded"->MaterialTheme.colorScheme.error
                        else->Color.Yellow
                    }
                    val isLoggedIn=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
                        .getString("authToken",null)!=null

                    Column(
                        modifier=Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal=12.dp,vertical=16.dp),
                        verticalArrangement=Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            modifier=Modifier.fillMaxWidth(),
                            horizontalArrangement=Arrangement.spacedBy(4.dp),
                            verticalAlignment=Alignment.CenterVertically,
                        ) {
                            Text(
                                text=bandData.name.orEmpty(),
                                style=MaterialTheme.typography.headlineLarge,
                                fontWeight=FontWeight.Bold,
                            )
                            IconButton(
                                enabled=isLoggedIn,
                                onClick={toggleBand(bandId)}
                            ) {
                                Icon(
                                    painter=if(bandData.favorite==true) {
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
                                        text=bandData.country?.let {
                                            stringResource(LocalText().getStringResource(it))
                                        }?:"-",
                                    )
                                }
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("status"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text=stringResource(
                                            LocalText().getStringResource(
                                                bandData.status?.lowercase()?:"-"
                                            )
                                        ),
                                        color=statusColor,
                                    )
                                }
                                Row {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("years_active"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text="${bandData.formedYear?:"-"} - ${
                                            bandData.disbandedYear
                                            ?:stringResource(LocalText().getStringResource("present"))
                                        }",
                                    )
                                }
                            }

                            Column(
                                modifier=Modifier.weight(0.9f),
                                verticalArrangement=Arrangement.spacedBy(8.dp),
                            ) {
                                Row(verticalAlignment=Alignment.CenterVertically) {
                                    Text(
                                        text="${stringResource(LocalText().getStringResource("top_genres"))}: ",
                                        color=MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    IconButton(
                                        onClick={showingGenreInformation=true},
                                        modifier=Modifier.size(28.dp),
                                    ) {
                                        Icon(
                                            imageVector=Icons.Outlined.Info,
                                            contentDescription="More information",
                                            tint=MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier=Modifier.size(18.dp),
                                        )
                                    }
                                }
                                bandData.computedGenres.orEmpty().forEach {genre->
                                    Text("-${genre.name?:"-"}")
                                }
                            }
                        }

                        LoadImage(
                            imageUrl=bandData.imageUrl,
                            contentDescription=bandData.name.orEmpty(),
                        )

                        TranslatedDescription(description=bandData.description.orEmpty())

                        Tabs(
                            labels=listOf("members","albums","similar_bands").map {
                                stringResource(LocalText().getStringResource(it))
                            },
                            selectedIndex=selectedTab,
                            onSelected={selectedTab=it},
                        ) {tab->
                            when(tab) {
                                0-> {
                                    Row(
                                        modifier=Modifier.fillMaxWidth(),
                                        horizontalArrangement=Arrangement.SpaceBetween,
                                        verticalAlignment=Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            modifier=Modifier
                                                .horizontalScroll(rememberScrollState())
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(12.dp),
                                                )
                                                .padding(4.dp),
                                        ) {
                                            listOf("all_members","current_members","past_members")
                                                .forEachIndexed {index,label->
                                                    Box(
                                                        modifier=Modifier
                                                            .height(32.dp)
                                                            .background(
                                                                if(selectedMemberFilter==index) {
                                                                    MaterialTheme.colorScheme.surface
                                                                }
                                                                else {
                                                                    Color.Transparent
                                                                },
                                                                RoundedCornerShape(12.dp),
                                                            )
                                                            .clickable(
                                                                interactionSource=remember {MutableInteractionSource()},
                                                                indication=LocalIndication.current,
                                                                onClick={selectedMemberFilter=index},
                                                            )
                                                            .semantics {
                                                                role=Role.RadioButton
                                                                selected=selectedMemberFilter==index
                                                            },
                                                        contentAlignment=Alignment.Center,
                                                    ) {
                                                        Text(
                                                            text=stringResource(
                                                                LocalText().getStringResource(label)
                                                            ),
                                                            color=MaterialTheme.colorScheme.onSurface,
                                                            modifier=Modifier.padding(horizontal=12.dp),
                                                        )
                                                    }
                                                }
                                        }
                                        Button(
                                            onClick={toggleArtistFavoriteAll(bandId)},
                                            enabled=isLoggedIn,
                                        ) {
                                            Text(
                                                text=stringResource(
                                                    LocalText().getStringResource("favorite_all")
                                                ),modifier=Modifier.padding(0.dp)
                                            )
                                        }
                                    }


                                    Spacer(modifier=Modifier.size(4.dp))
                                    Table(
                                        columns=listOf(
                                            Pair("person_name",MemberTableRow::personName),
                                            Pair("role",MemberTableRow::yearRole),
                                        ),
                                        data=showingMembers,
                                        emptyText=stringResource(
                                            LocalText().getStringResource("no_band_members")
                                        ),
                                    )
                                }

                                1-> {
                                    Row(
                                        modifier=Modifier
                                            .horizontalScroll(rememberScrollState())
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(12.dp),
                                            )
                                            .padding(4.dp),
                                    ) {
                                        listOf("all","studio").forEachIndexed {index,label->
                                            Box(
                                                modifier=Modifier
                                                    .height(32.dp)
                                                    .background(
                                                        if(selectedAlbumFilter==index) {
                                                            MaterialTheme.colorScheme.surface
                                                        }
                                                        else {
                                                            Color.Transparent
                                                        },
                                                        RoundedCornerShape(12.dp),
                                                    )
                                                    .clickable(
                                                        interactionSource=remember {MutableInteractionSource()},
                                                        indication=LocalIndication.current,
                                                        onClick={selectedAlbumFilter=index},
                                                    )
                                                    .semantics {
                                                        role=Role.RadioButton
                                                        selected=selectedAlbumFilter==index
                                                    },
                                                contentAlignment=Alignment.Center,
                                            ) {
                                                Text(
                                                    text=stringResource(
                                                        LocalText().getStringResource(label)
                                                    ),
                                                    color=MaterialTheme.colorScheme.onSurface,
                                                    modifier=Modifier.padding(horizontal=12.dp),
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier=Modifier.size(4.dp))
                                    Table(
                                        columns=listOf(
                                            Pair("title",AlbumTableRow::title),
                                            Pair("release_date",AlbumTableRow::releaseDate),
                                            Pair("album_type",AlbumTableRow::albumType),
                                            Pair("main_genre",AlbumTableRow::mainGenre),
                                        ),
                                        data=showingAlbums,
                                        emptyText=stringResource(
                                            LocalText().getStringResource("no_albums")
                                        ),
                                    )
                                }

                                else->Table(
                                    columns=listOf(
                                        Pair("thing_name",SimilarBandTableRow::bandName),
                                        Pair("formed_year",SimilarBandTableRow::formedYear),
                                        Pair("country",SimilarBandTableRow::country),
                                    ),
                                    data=similarBands,
                                    emptyText=stringResource(
                                        LocalText().getStringResource("no_similar_bands")
                                    ),
                                )
                            }
                        }
                    }

                    if(showingGenreInformation) {
                        AlertDialog(
                            onDismissRequest={showingGenreInformation=false},
                            containerColor=MaterialTheme.colorScheme.surface,
                            titleContentColor=MaterialTheme.colorScheme.onSurface,
                            textContentColor=MaterialTheme.colorScheme.onSurface,
                            tonalElevation=0.dp,
                            title={
                                Text(stringResource(LocalText().getStringResource("top_genres")))
                            },
                            text={
                                Text(
                                    stringResource(LocalText().getStringResource("band_genre_info"))
                                )
                            },
                            confirmButton={
                                TextButton(
                                    onClick={showingGenreInformation=false},
                                    colors=ButtonDefaults.textButtonColors(
                                        contentColor=MaterialTheme.colorScheme.onSurface,
                                    ),
                                ) {
                                    Text(stringResource(LocalText().getStringResource("ok")))
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

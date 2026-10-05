package com.example.client.profile.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.example.client.AppTheme
import com.example.client.LocalText
import com.example.client.common.ProgressBar
import com.example.client.common.Tabs
import com.example.client.common.translatedDate
import com.example.client.profile.ProfileClient
import com.example.client.profile.UserProfileDto
import com.example.client.profile.components.ContributionsTable
import com.example.client.profile.components.FavoriteArtistsTable
import com.example.client.profile.components.FavoriteBandsTable
import com.example.client.profile.components.FavoriteGenresTable
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class,ExperimentalJsExport::class)
fun ProfilePageView(
    login:String="aleks",
) {
    var user by remember {mutableStateOf<UserProfileDto?>(null)}
    var isLoading by remember {mutableStateOf(login.isNotBlank())}

    val scope=rememberCoroutineScope()
    val context=LocalContext.current
    var selectedTab by remember {mutableStateOf(0)}
    var showLastOnline by remember {mutableStateOf(false)}
    val isOnline=false
    val popupOffset=with(LocalDensity.current) {8.dp.roundToPx()}
    val popupPositionProvider=remember(popupOffset) {
        CenteredBelowPopupPositionProvider(popupOffset)
    }
    var favoriteBandIds by remember(user) {
        mutableStateOf(user?.favoriteBands.orEmpty().mapNotNull {it.id}.toSet())
    }
    var favoriteArtistIds by remember(user) {
        mutableStateOf(user?.favoriteArtists.orEmpty().mapNotNull {it.id}.toSet())
    }
    var favoriteGenreIds by remember(user) {
        mutableStateOf(user?.favoriteGenres.orEmpty().mapNotNull {it.id}.toSet())
    }

    val ranks=listOf(1,15,40,120,270,520,820,1200,1700,2400,3500,5500,8000,11000,16000,21000,182500,400000)
    val rankProgress=if(user?.rankId in 1..17) {
        val currentMinXp=ranks[user?.rankId?.minus(1) ?: 0]
        val nextMinXp=ranks[user?.rankId ?: 1]
        ((user?.xp?.minus(currentMinXp))?.toFloat()?.div((nextMinXp-currentMinXp)))?.coerceIn(0f,1f)
    }
    else {
        null
    }

    fun token():String?=
        context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            .getString("authToken",null)

    fun toggleBand(id:Int) {
        scope.launch {
            try {
                if(ProfileClient.toggleFavoriteBand(id,token())=="band_toggled") {
                    favoriteBandIds=if(id in favoriteBandIds) favoriteBandIds-id else favoriteBandIds+id
                }
            }
            catch(e:Exception) {
                Log.e("profile favorite band",e.localizedMessage?:"")
            }
        }
    }

    fun toggleArtist(id:Long) {
        scope.launch {
            try {
                if(ProfileClient.toggleFavoriteArtist(id,token())=="artist_toggled") {
                    favoriteArtistIds=
                        if(id in favoriteArtistIds) favoriteArtistIds-id else favoriteArtistIds+id
                }
            }
            catch(e:Exception) {
                Log.e("profile favorite artist",e.localizedMessage?:"")
            }
        }
    }

    fun toggleGenre(id:Int) {
        scope.launch {
            try {
                if(ProfileClient.toggleFavoriteGenre(id,token())=="genre_toggled") {
                    favoriteGenreIds=if(id in favoriteGenreIds) favoriteGenreIds-id else favoriteGenreIds+id
                }
            }
            catch(e:Exception) {
                Log.e("profile favorite genre",e.localizedMessage?:"")
            }
        }
    }

    LaunchedEffect(login) {
        if(login.isBlank()) {
            user=UserProfileDto(
                login="Preview user",
                username="preview",
                rankId=1,
                xp=1,
                bio="",
                user=true,
            )
            isLoading=false
            return@LaunchedEffect
        }

        isLoading=true
        user=null
        try {
            val token=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
                .getString("authToken",null)
            user=ProfileClient.getUserProfile(login,token)
        }
        catch(e:Exception) {
            Log.e("profile",e.localizedMessage?:"")
            user=null
        }
        isLoading=false
    }

    AppTheme {
        Surface(modifier=Modifier.fillMaxSize()) {
            when {
                isLoading->Box(
                    contentAlignment=Alignment.Center,
                    modifier=Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }

                else->
                    user?.let{ userData->
                        Column(
                            modifier=Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal=12.dp,vertical=16.dp),
                            verticalArrangement=Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment=Alignment.CenterVertically,
                                horizontalArrangement=Arrangement.spacedBy(14.dp)
                            ) {
                                Box(modifier=Modifier.size(80.dp)) {
                                    Surface(
                                        color=MaterialTheme.colorScheme.primaryContainer,
                                        shape=MaterialTheme.shapes.large,
                                        modifier=Modifier.fillMaxSize()
                                    ) {
                                        Box(contentAlignment=Alignment.Center) {
                                            Icon(
                                                imageVector=Icons.Default.Person,
                                                contentDescription="profile",
                                                modifier=Modifier.size(52.dp),
                                                tint=MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Box(
                                        modifier=Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(26.dp)
                                            .offset(x=6.dp,y=6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if(isOnline) Color.Green
                                                else MaterialTheme.colorScheme.outline
                                            )
                                            .clickable(
                                                interactionSource=remember {MutableInteractionSource()},
                                                indication=LocalIndication.current,
                                                onClick={showLastOnline=!showLastOnline}
                                            )
                                    ) {
                                        if(showLastOnline) Popup(
                                            popupPositionProvider=popupPositionProvider,
                                            onDismissRequest={showLastOnline=false}
                                        ) {
                                            Surface(
                                                color=MaterialTheme.colorScheme.surface,
                                                shape=MaterialTheme.shapes.small,
                                                tonalElevation=0.dp,
                                                shadowElevation=4.dp
                                            ) {
                                                Text(
                                                    "${stringResource(LocalText().getStringResource("last_online"))} "
                                                    +translatedDate(userData.lastLogin),
                                                    color=MaterialTheme.colorScheme.onSurface,
                                                    modifier=Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Column {
                                    Text(userData.username,fontSize=24.sp,fontWeight=FontWeight.Bold)
                                    Text(
                                        "@${userData.login}",
                                        fontSize=18.sp,
                                        color=MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${stringResource(LocalText().getStringResource("member_since"))} "
                                        +translatedDate(userData.accountAge),
                                        fontSize=14.sp
                                    )
                                }
                            }

                            rankProgress?.let {progress->
                                ProgressBar(
                                    progress=progress,
                                    startText="${stringResource(LocalText().getStringResource("level"))} ${userData.rankId}: "
                                        +stringResource(LocalText().getStringResource("rank${userData.rankId}")),
                                    endText="${stringResource(LocalText().getStringResource("level"))} ${userData.rankId+1}: "
                                        +stringResource(LocalText().getStringResource("rank${userData.rankId+1}"))
                                )
                            }

                            Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                Text(
                                    stringResource(LocalText().getStringResource("bio")),
                                    fontWeight=FontWeight.Bold
                                )
                                Surface(
                                    modifier=Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    shape=MaterialTheme.shapes.medium,
                                    border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),
                                    color=MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text=userData.bio?.takeIf {it.isNotBlank()}?:"",
                                        modifier=Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(10.dp)
                                    )
                                }
                            }

                            Tabs(
                                labels=listOf(
                                    "favorite_bands",
                                    "favorite_artists",
                                    "favorite_genres"
                                ).map {title->
                                    stringResource(LocalText().getStringResource(title))
                                },
                                selectedIndex=selectedTab,
                                onSelected={selectedTab=it}
                            ) {index->
                                when(index) {
                                    0->FavoriteBandsTable(
                                        favoriteBands=userData.favoriteBands,
                                        currentUser=userData.user,
                                        favoriteIds=favoriteBandIds,
                                        onToggle=::toggleBand
                                    )

                                    1->FavoriteArtistsTable(
                                        favoriteArtists=userData.favoriteArtists,
                                        currentUser=userData.user,
                                        favoriteIds=favoriteArtistIds,
                                        onToggle=::toggleArtist
                                    )

                                    2->FavoriteGenresTable(
                                        favoriteGenres=userData.favoriteGenres,
                                        currentUser=userData.user,
                                        favoriteIds=favoriteGenreIds,
                                        onToggle=::toggleGenre
                                    )
                                }
                            }

                            Spacer(modifier=Modifier.height(16.dp))
                            Text(
                                stringResource(LocalText().getStringResource("contributions")),
                                fontWeight=FontWeight.Bold
                            )
                            ContributionsTable(userData.contributions)
                            Spacer(modifier=Modifier.height(4.dp))
                        }
                    }
            }
        }
    }
}

class CenteredBelowPopupPositionProvider(
    val verticalOffset:Int,
):PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds:IntRect,
        windowSize:IntSize,
        layoutDirection:LayoutDirection,
        popupContentSize:IntSize,
    ):IntOffset {
        return IntOffset(
            anchorBounds.left+(anchorBounds.width-popupContentSize.width)/2,
            anchorBounds.bottom+verticalOffset
        )
    }
}
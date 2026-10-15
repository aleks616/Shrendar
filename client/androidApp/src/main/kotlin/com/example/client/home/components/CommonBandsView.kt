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
import com.example.client.AppTheme
import com.example.client.MR
import com.example.client.bands_to_check_out
import com.example.client.homepage.HomepageClient
import com.example.client.homepage.dto.CommonBandsDto
import com.example.client.member_in_favorites
import dev.icerock.moko.resources.compose.stringResource
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
@Composable
fun CommonBandsView() {
    var bands by remember {mutableStateOf<CommonBandsDto?>(null)}
    var isLoading by remember {mutableStateOf(true)}
    val context=LocalContext.current

    LaunchedEffect(Unit) {
        isLoading=true
        val token=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            .getString("authToken",null)
        if(token!=null) {
            bands=try {
                HomepageClient.getCommonBands(token)
            }
            catch(_:Exception) {
                null
            }
            finally {
                isLoading=false
            }
        }
    }

    AppTheme {
        Surface(
            modifier=Modifier.fillMaxSize(),
        ) {
            if(!isLoading&&bands!=null) {
                Column(
                    modifier=Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text=stringResource(MR.strings.bands_to_check_out),
                        fontSize=26.sp,
                        fontWeight=FontWeight.Bold,
                        modifier=Modifier.padding(bottom=12.dp)
                    )
                    bands?.bands?.forEach {band->
                        band.bandName?.let {Text(text=it,fontWeight=FontWeight.Bold)}
                        Text(text=stringResource(MR.strings.member_in_favorites))
                        Text(text=band.nickname?:band.artistName?:"unknown")
                        Spacer(modifier=Modifier.size(8.dp))
                    }
                }
            }
        }
    }
}
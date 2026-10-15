package com.example.client.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.client.MR
import com.example.client.no_image
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun LoadImage(
    imageUrl:String?,
    contentDescription:String,
    width:Dp=300.dp,
) {
    if(imageUrl.isNullOrBlank()) return defaultIcon(width)

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
        modifier=Modifier.size(width),
        contentAlignment=Alignment.Center,
    ) {
        if(bitmap!=null) {
            Image(
                bitmap=bitmap!!.asImageBitmap(),
                contentDescription=contentDescription,
                contentScale=ContentScale.FillBounds,
                modifier=Modifier
                    .fillMaxSize()
            )
        }
        else {
            defaultIcon(width)
        }
    }
}

@Composable
fun defaultIcon(width:Dp=300.dp) {
    Icon(
        painter=painterResource(MR.images.no_image),
        modifier=Modifier.size(width),
        contentDescription="no image",
        tint=Color.Unspecified
    )
}
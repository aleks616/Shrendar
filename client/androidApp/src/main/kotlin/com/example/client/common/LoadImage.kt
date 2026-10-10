package com.example.client.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun LoadImage(
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
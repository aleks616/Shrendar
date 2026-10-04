package com.example.client.common

import androidx.compose.runtime.Composable
import com.example.client.LocalText
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun translatedDate(timePassed:String?):String {
    if(timePassed.isNullOrBlank()) return "-"
    if(timePassed=="today") return stringResource(LocalText().getStringResource("today"))
    val parts=timePassed.split(" ")
    if(parts.size<2) return timePassed
    return "${parts[0]} ${stringResource(LocalText().getStringResource(parts[1]))} ${
        stringResource(
            LocalText().getStringResource(
                "time_ago"
            )
        )
    }"
}
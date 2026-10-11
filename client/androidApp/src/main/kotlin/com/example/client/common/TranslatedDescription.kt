package com.example.client.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun TranslatedDescription(description:String) {
    var displayedDescription by remember(description) {mutableStateOf(description)}
    var isLoading by remember {mutableStateOf(false)}
    val scope=rememberCoroutineScope()

    Surface(
        modifier=Modifier
            .fillMaxWidth()
            .padding(top=4.dp),
        shape=RoundedCornerShape(12.dp),
        border=BorderStroke(1.dp,MaterialTheme.colorScheme.primary),
        color=MaterialTheme.colorScheme.surface,
        tonalElevation=0.dp,
    ) {
        Box {
            Text(
                text=displayedDescription,
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(end=44.dp),
            )

            IconButton(
                onClick={
                    if(!isLoading) {
                        isLoading=true
                        scope.launch {
                            try {
                                displayedDescription=LlmClient.translate(
                                    TranslationRequestDto(
                                        text=description,
                                        targetLanguage=Locale.getDefault().language.ifBlank {"en"},
                                    )
                                )
                            }
                            finally {
                                isLoading=false
                            }
                        }
                    }
                },
                enabled=!isLoading,
                modifier=Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .semantics {contentDescription="Translate description"},
            ) {
                if(isLoading) {
                    CircularProgressIndicator()
                }
                else {
                    Icon(
                        imageVector=translate,
                        contentDescription=null,
                    )
                }
            }
        }
    }
}

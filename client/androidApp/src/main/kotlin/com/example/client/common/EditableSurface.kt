package com.example.client.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun EditableSurface(
    value:String,
    modifier:Modifier=Modifier,
    onSave:((String)->Unit)?=null,
) {
    var draft by remember {mutableStateOf(value)}
    var isEditing by remember {mutableStateOf(false)}
    val focusRequester=remember {FocusRequester()}
    val focusManager=LocalFocusManager.current

    LaunchedEffect(value) {
        if(!isEditing) {
            draft=value
        }
    }

    LaunchedEffect(isEditing) {
        if(isEditing) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier=modifier
            .fillMaxWidth()
            .height(208.dp),
        shape=RoundedCornerShape(12.dp),
        border=BorderStroke(1.dp,MaterialTheme.colorScheme.primary),
        color=MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box {
            if(isEditing) {
                BasicTextField(
                    value=draft,
                    onValueChange={draft=it},
                    modifier=Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp)
                        .padding(end=32.dp),
                    textStyle=MaterialTheme.typography.bodyLarge.copy(
                        color=MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions=KeyboardOptions(imeAction=ImeAction.Default),
                )
            }
            else {
                Text(
                    text=draft,
                    modifier=Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                        .padding(end=32.dp),
                    color=MaterialTheme.colorScheme.onSurface,
                )
            }

            IconButton(
                onClick={
                    if(isEditing) {
                        isEditing=false
                        focusManager.clearFocus()
                        onSave?.invoke(draft)
                    }
                    else {
                        isEditing=true
                    }
                },
                modifier=Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector=if(isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription=if(isEditing) "Save" else "Edit",
                    tint=MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

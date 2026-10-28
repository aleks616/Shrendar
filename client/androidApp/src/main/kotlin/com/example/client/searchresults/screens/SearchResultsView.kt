package com.example.client.searchresults.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import com.example.client.common.LoadImage
import com.example.client.common.getDirection
import com.example.client.searching.SearchClient
import com.example.client.searching.SearchResults
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport


@Composable
@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class,ExperimentalJsExport::class)
fun SearchResultsView(
    navigator:DestinationsNavigator
) {
    var data by remember {mutableStateOf<SearchResults?>(null)}
    var isLoading by remember {mutableStateOf(false)}
    val filterOptions=listOf("all","band","album","artist","user","event")
    var filter by remember {mutableStateOf("all")}
    var showingItems by remember {mutableStateOf<SearchResults?>(null)}
    var searchQuery by remember {mutableStateOf("")}
    val scope=rememberCoroutineScope()
    val focusManager=LocalFocusManager.current

    suspend fun loadData() {
        isLoading=true
        try {
            data=SearchClient.search(searchQuery)
            showingItems=data
            filter="all"
        }
        catch(_:Exception) {
            data=null
        }
        finally {
            isLoading=false
        }
    }

    fun submitSearch() {
        scope.launch {loadData()}
        focusManager.clearFocus()
    }

    AppTheme {
        Surface(
            modifier=Modifier.fillMaxSize()
        ) {
            Column(modifier=Modifier.fillMaxSize()) {

                TextField(
                    value=searchQuery,
                    placeholder={Text(text=stringResource(MR.strings.searchbar))},
                    onValueChange={searchQuery=it},
                    leadingIcon={
                        Icon(
                            imageVector=Icons.Filled.Search,
                            tint=MaterialTheme.colorScheme.primary,
                            contentDescription="Search"
                        )
                    },
                    keyboardOptions=KeyboardOptions(imeAction=ImeAction.Done),
                    keyboardActions=KeyboardActions(
                        onDone={submitSearch()}
                    ),
                    modifier=Modifier
                        .onPreviewKeyEvent {event->
                            if(event.type==KeyEventType.KeyDown && event.key==Key.Enter) {
                                submitSearch()
                                true
                            }
                            else false
                        }
                        .fillMaxWidth()
                        .padding(12.dp)
                )
                if(data!=null) {
                    FlowRow(
                        modifier=Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        maxItemsInEachRow=3,
                        horizontalArrangement=Arrangement.spacedBy(12.dp)
                    ) {
                        filterOptions.forEach {option->
                            Button(
                                onClick={
                                    filter=option
                                    showingItems=if(option=="all") {
                                        data
                                    }
                                    else {
                                        data?.copy(items=data?.items?.filter {it.type==option})
                                    }
                                },colors=ButtonDefaults.buttonColors(
                                    containerColor=if(filter==option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(text=stringResource(LocalText().getStringResource("filter_$option")))
                            }
                        }
                    }
                }
                Column(
                    modifier=Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(all=8.dp),
                    verticalArrangement=Arrangement.spacedBy(8.dp),
                ) {
                    if(isLoading) {
                        Box(
                            modifier=Modifier.fillMaxSize(),
                            contentAlignment=Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else if(data!=null) {
                        if(showingItems?.items?.size==0) {
                            Row(modifier=Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) {
                                Text(text=stringResource(MR.strings.no_results),fontSize=28.sp)
                            }
                        }
                        showingItems?.items?.forEach {item->
                            Row(
                                modifier=Modifier
                                    .fillMaxWidth()
                                    .heightIn(min=120.dp)
                                    .padding(4.dp)
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                verticalAlignment=Alignment.CenterVertically,
                                horizontalArrangement=Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier=Modifier.padding(horizontal=8.dp)) {
                                    LoadImage(
                                        imageUrl=item.imageUrl,
                                        contentDescription=item.name,
                                        width=64.dp,
                                        round=true
                                    )
                                }
                                Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                                    val direction=getDirection(item.link)
                                    if(direction!=null) {
                                        TextButton(
                                            modifier=Modifier.padding(0.dp),
                                            onClick={navigator.navigate(direction)}
                                        ) {
                                            Text(
                                                text=item.name,
                                                fontSize=22.sp,
                                                color=MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    else {
                                        Text(text=item.name,fontSize=22.sp,color=MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text(text=item.info?:"-",fontSize=16.sp)
                                    Text(
                                        text=item.description,fontSize=12.sp,
                                        color=MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
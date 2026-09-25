package com.example.client.account.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import dev.icerock.moko.resources.compose.stringResource

@Composable
@Preview
fun WelcomeView(
    registerScreen:()->Unit={},
    signInScreen:()->Unit={},
) {
    AppTheme {
        Surface {
            Box(modifier=Modifier.fillMaxSize()) {
                Box(
                    modifier=Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors=listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha=0.8f),Color.Unspecified
                                )
                            )
                        )
                )
                Column(
                    horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.SpaceBetween,
                    modifier=Modifier
                        .fillMaxSize()
                        .padding(top=25.dp)
                ) {
                    Column(
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.spacedBy(16.dp),
                        modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp)
                    ) {
                        Text(text=stringResource(MR.strings.welcome),fontSize=28.sp,fontWeight=FontWeight.Bold)
                        Text(text=stringResource(MR.strings.sign_in_up),fontSize=20.sp,fontWeight=FontWeight.Bold)
                    }
                    Column(
                        horizontalAlignment=Alignment.CenterHorizontally,
                        modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp)
                    ) {
                        Button(onClick=registerScreen,modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp)) {
                            Text(text=stringResource(MR.strings.create_account))
                        }
                        Button(
                            onClick=signInScreen,
                            modifier=Modifier
                                .fillMaxWidth()
                                .padding(horizontal=20.dp),
                            colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF717171))

                        ) {
                            Text(text=stringResource(MR.strings.account_already))
                        }
                        Row(horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                            Text(text=stringResource(MR.strings.continue_as))
                            TextButton(onClick={},modifier=Modifier.padding(0.dp)) {
                                Text(text=stringResource(MR.strings.guest_question),modifier=Modifier.padding(0.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
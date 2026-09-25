package com.example.client.account.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.AppTheme
import com.example.client.MR
import com.example.client.account_already
import com.example.client.continue_as
import com.example.client.create_account
import com.example.client.guest_question
import com.example.client.sign_in_up
import com.example.client.welcome
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
                            colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)

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
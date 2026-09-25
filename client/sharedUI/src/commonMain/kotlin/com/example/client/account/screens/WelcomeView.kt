package com.example.client.account.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.client.continue_as
import com.example.client.guest_question
import com.example.client.sign_in_up
import com.example.client.welcome
import dev.icerock.moko.resources.compose.stringResource

@Composable
@Preview
fun WelcomeView() {
    AppTheme {
        Column(
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(15.dp),
            modifier=Modifier
                .padding(top=25.dp)
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors=listOf(MaterialTheme.colorScheme.primary.copy(alpha=0.8f),Color.Transparent)))
                .height(200.dp),
        ){
            Text(text=stringResource(MR.strings.welcome),fontSize=28.sp,fontWeight=FontWeight.Bold)
            Text(text=stringResource(MR.strings.sign_in_up),fontSize=20.sp)
            Spacer(modifier=Modifier.height(20.dp))


            Row{
                Text(text=stringResource(MR.strings.continue_as))
                Button(onClick={}){
                    Text(text=stringResource(MR.strings.guest_question))
                }
            }
        }
    }
}
package com.ft.ltd

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ft.ltd.service.ServiceHelper
import com.ft.ltd.service.StopWatchService
import com.ft.ltd.service.StopWatchState
import com.ft.ltd.util.Constants.ACTION_SERVICE_CANCEL
import com.ft.ltd.util.Constants.ACTION_SERVICE_START
import com.ft.ltd.util.Constants.ACTION_SERVICE_STOP

@ExperimentalAnimationApi
@Composable
fun MainScreen(stopWatchService: StopWatchService) {
    val context = LocalContext.current
    val hourS by stopWatchService.hourS
    val minuteS by stopWatchService.minuteS
    val secondS by stopWatchService.secondS
    val currentState by stopWatchService.currentState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(weight = 9f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(targetState = hourS, transitionSpec = { animation() }) {
                Text(
                    text = hourS,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.h1.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = if (hourS == "00") Color.White else Color.Blue
                    )
                )
            }

            AnimatedContent(targetState = minuteS, transitionSpec = { animation() }) {
                Text(
                    text = minuteS,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.h1.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = if (minuteS == "00") Color.White else Color.Blue
                    )
                )
            }

            AnimatedContent(targetState = secondS, transitionSpec = { animation() }) {
                Text(
                    text = secondS,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.h1.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = if (secondS == "00") Color.White else Color.Blue
                    )
                )
            }
        }

        Row(modifier = Modifier.weight(weight = 1f)) {
            Button(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(0.8f),
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = if (currentState == StopWatchState.Started) ACTION_SERVICE_STOP else ACTION_SERVICE_START
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (currentState == StopWatchState.Started) Color.Red else Color.Blue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = when (currentState) {
                        StopWatchState.Started -> "Stop"
                        StopWatchState.Stopped -> "Resume"
                        else -> "Start"
                    }
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            Button(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(0.8f),
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_CANCEL
                    )
                },
                enabled = secondS != "00" && currentState != StopWatchState.Started,
                colors = ButtonDefaults.buttonColors(disabledBackgroundColor = Color.LightGray)
            )
            {
                Text(
                    text = "Cancel"
                )
            }
        }
    }
}

@ExperimentalAnimationApi
fun animation(duration: Int = 500): ContentTransform {
    return slideInVertically(animationSpec = tween(durationMillis = duration)) { height -> height } +
            fadeIn(animationSpec = tween(durationMillis = duration)) with
            slideOutVertically(animationSpec = tween(durationMillis = duration)) { height -> height } +
            fadeOut(animationSpec = tween(durationMillis = duration))
}
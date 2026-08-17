package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallSession
import com.example.data.model.CallStatus

@Composable
fun CallScreen(
    callSession: CallSession,
    onEndCall: () -> Unit,
    onAcceptCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    val formattedDuration = remember(callSession.durationSeconds) {
        val minutes = callSession.durationSeconds / 60
        val seconds = callSession.durationSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("❤️", fontSize = 48.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = callSession.callerName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (callSession.status) {
                        CallStatus.RINGING -> if (callSession.isVideo) "Lik Video Calling..." else "Lik Calling..."
                        CallStatus.CONNECTED -> formattedDuration
                        CallStatus.ENDED -> "Call Ended"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.LightGray
                )
            }

            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(56.dp)
                        .background(if (isMuted) Color.White else Color.DarkGray, CircleShape)
                        .testTag("call_mute_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (isMuted) Color.Black else Color.White
                    )
                }

                // End Call Button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.Red, CircleShape)
                        .testTag("call_end_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = { isSpeakerOn = !isSpeakerOn },
                    modifier = Modifier
                        .size(56.dp)
                        .background(if (isSpeakerOn) Color.White else Color.DarkGray, CircleShape)
                        .testTag("call_speaker_btn")
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = if (isSpeakerOn) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

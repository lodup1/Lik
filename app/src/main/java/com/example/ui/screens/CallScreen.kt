package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ActiveCall
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LikRosePrimary
import com.example.ui.theme.LikRoseSecondary
import com.example.ui.viewmodel.CallViewModel

@Composable
fun CallScreen(callViewModel: CallViewModel) {
    val activeCall by callViewModel.activeCall.collectAsState()

    val call = activeCall ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (call.callType == CallType.VIDEO && call.status == CallStatus.CONNECTED && !call.isVideoOff) {
                // Partner Video Feed
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800",
                    contentDescription = "Partner Video",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Self Video PIP Preview (Bottom Right)
                Box(
                    modifier = Modifier
                        .padding(bottom = 120.dp, end = 20.dp)
                        .size(110.dp, 160.dp)
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, LikRosePrimary, RoundedCornerShape(16.dp))
                        .background(Color.DarkGray)
                ) {
                    if (call.isFrontCamera) {
                        Text(
                            "You",
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "Rear Cam",
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Voice Call / Video Off Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    LikRosePrimary.copy(alpha = 0.3f),
                                    DarkBackground
                                )
                            )
                        )
                )
            }

            // Call Content Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Info Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .scale(if (call.status == CallStatus.RINGING || call.status == CallStatus.INCOMING) pulseScale else 1f)
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(LikRosePrimary, LikRoseSecondary))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(call.callerAvatar, fontSize = 52.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = call.callerName,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val statusText = when (call.status) {
                        CallStatus.DIALING -> "Dialing..."
                        CallStatus.RINGING -> "Ringing..."
                        CallStatus.INCOMING -> if (call.callType == CallType.VIDEO) "Incoming Video Call..." else "Incoming Voice Call..."
                        CallStatus.CONNECTED -> formatCallDuration(call.durationSeconds)
                        CallStatus.REJECTED -> "Call Declined"
                        CallStatus.ENDED -> "Call Ended"
                        CallStatus.IDLE -> ""
                    }

                    Text(
                        text = statusText,
                        color = LikRoseSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Voice Equalizer Wave Animation
                if (call.callType == CallType.VOICE && call.status == CallStatus.CONNECTED) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(60.dp)
                    ) {
                        repeat(7) { index ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height((20..50).random().dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(LikRosePrimary)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Control Action Buttons
                when (call.status) {
                    CallStatus.INCOMING -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Decline Button
                            FloatingActionButton(
                                onClick = { callViewModel.rejectCall() },
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("reject_call_button")
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = "Decline Call", modifier = Modifier.size(32.dp))
                            }

                            // Accept Button
                            FloatingActionButton(
                                onClick = { callViewModel.acceptCall() },
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("accept_call_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Accept Call", modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    else -> {
                        // Active / Dialing Control Bar
                        Card(
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mute Button
                                IconButton(
                                    onClick = { callViewModel.toggleMute() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (call.isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = "Mute",
                                        tint = if (call.isMuted) Color.Black else Color.White
                                    )
                                }

                                // Camera Toggle Button
                                if (call.callType == CallType.VIDEO) {
                                    IconButton(
                                        onClick = { callViewModel.toggleVideo() },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(if (call.isVideoOff) Color.White else Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = if (call.isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                            contentDescription = "Video",
                                            tint = if (call.isVideoOff) Color.Black else Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { callViewModel.toggleFrontCamera() },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            Icons.Default.Cameraswitch,
                                            contentDescription = "Switch Camera",
                                            tint = Color.White
                                        )
                                    }
                                }

                                // Speaker Button
                                IconButton(
                                    onClick = { callViewModel.toggleSpeaker() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (call.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Speaker",
                                        tint = if (call.isSpeakerOn) Color.Black else Color.White
                                    )
                                }

                                // End Call Button
                                FloatingActionButton(
                                    onClick = { callViewModel.endCall() },
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .testTag("end_call_button")
                                ) {
                                    Icon(Icons.Default.CallEnd, contentDescription = "End Call")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCallDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

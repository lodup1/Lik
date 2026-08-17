package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.data.model.User
import com.example.ui.theme.OnlineGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    currentUser: User?,
    lastMessage: Message?,
    onOpenChat: () -> Unit,
    onStartCall: (isVideo: Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lik", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Lik",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onStartCall(false) },
                        modifier = Modifier.testTag("call_audio_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call")
                    }
                    IconButton(
                        onClick = { onStartCall(true) },
                        modifier = Modifier.testTag("call_video_btn")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                PartnerChatRow(
                    partnerName = if (currentUser?.pairedPartnerId != null) "Partner (${currentUser.pairedPartnerId})" else "Connected Partner",
                    lastMessage = lastMessage?.text ?: "Tap to send your first message ❤️",
                    time = "Now",
                    avatar = currentUser?.avatarEmoji ?: "❤️",
                    onClick = onOpenChat
                )
            }
        }
    }
}

@Composable
private fun PartnerChatRow(
    partnerName: String,
    lastMessage: String,
    time: String,
    avatar: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("partner_chat_item"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(avatar, fontSize = 24.sp)
                }
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(OnlineGreen, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

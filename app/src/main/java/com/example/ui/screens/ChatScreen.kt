package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.User
import com.example.ui.components.MediaAttachmentDialog
import com.example.ui.components.WallpaperPickerDialog
import com.example.ui.components.availableWallpapers
import com.example.ui.theme.SentBubbleColor
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUser: User?,
    chatViewModel: ChatViewModel,
    themeViewModel: ThemeViewModel,
    onBack: () -> Unit,
    onStartCall: (isVideo: Boolean) -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()
    val chatWallpaper by themeViewModel.chatWallpaper.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val wallpaperColor = remember(chatWallpaper) {
        availableWallpapers.firstOrNull { it.name == chatWallpaper }?.color ?: Color(0xFFFCE4EC)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Partner ❤️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Mood: ${currentUser?.currentMood ?: "Happy"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showWallpaperDialog = true }, modifier = Modifier.testTag("wallpaper_btn")) {
                        Icon(Icons.Default.Palette, contentDescription = "Change Wallpaper")
                    }
                    IconButton(onClick = { onStartCall(false) }, modifier = Modifier.testTag("chat_call_voice_btn")) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { onStartCall(true) }, modifier = Modifier.testTag("chat_call_video_btn")) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(wallpaperColor)
        ) {
            // Chat message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No messages yet. Send a lovely hello! 💕",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderId == "current_user"
                    MessageBubble(
                        message = msg,
                        isMine = isMine,
                        onReaction = { reaction ->
                            chatViewModel.addReaction(msg.id, reaction)
                        }
                    )
                }
            }

            // Quick simulation response buttons for test / rich interactivity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { chatViewModel.simulatePartnerReply("I love you so much! ❤️") },
                    label = { Text("Partner: ❤️", fontSize = 11.sp) },
                    modifier = Modifier.testTag("quick_reply_1")
                )
                AssistChip(
                    onClick = { chatViewModel.simulatePartnerReply("Thinking of you! Miss you 🥰") },
                    label = { Text("Partner: 🥰", fontSize = 11.sp) },
                    modifier = Modifier.testTag("quick_reply_2")
                )
            }

            // Message input bar
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachmentDialog = true },
                        modifier = Modifier.testTag("chat_attach_btn")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Attach", tint = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message partner...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                chatViewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .testTag("chat_send_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (showAttachmentDialog) {
            MediaAttachmentDialog(
                onDismiss = { showAttachmentDialog = false },
                onAttachImage = {
                    chatViewModel.sendMessage("[Sent Photo 📷]", type = MessageType.IMAGE)
                },
                onAttachVoice = {
                    chatViewModel.sendMessage("[Sent Voice Note 🎙️ (0:15)]", type = MessageType.VOICE)
                },
                onAttachLocation = {
                    chatViewModel.sendMessage("📍 Shared Location: Home Sweet Home", type = MessageType.LOCATION)
                },
                onAttachSticker = {
                    chatViewModel.sendMessage("🧸 Couple Hugs Sticker", type = MessageType.STICKER)
                }
            )
        }

        if (showWallpaperDialog) {
            WallpaperPickerDialog(
                currentWallpaper = chatWallpaper,
                onSelectWallpaper = { themeViewModel.setWallpaper(it) },
                onDismiss = { showWallpaperDialog = false }
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isMine: Boolean,
    onReaction: (String) -> Unit
) {
    val bubbleColor = if (isMine) SentBubbleColor else Color.White
    val textColor = if (isMine) Color.White else Color.Black
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 2.dp,
                bottomEnd = if (isMine) 2.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        fontSize = 10.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        if (message.reaction != null) {
            Text(
                text = message.reaction,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

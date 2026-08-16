package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SleekIndigoLight
import com.example.ui.theme.SleekIndigoPrimary
import com.example.ui.theme.SleekIndigoSecondary

data class ChatMessageItem(
    val id: String,
    val senderName: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String = "Just now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    title: String = "Lik Chat",
    initialMessages: List<ChatMessageItem> = listOf(
        ChatMessageItem("1", "Sarah", "Hey babe! Just got home. Can't wait for our trip this weekend ✨", isFromMe = false, timestamp = "18:42"),
        ChatMessageItem("2", "Me", "Welcome home! Me too. I started packing already lol", isFromMe = true, timestamp = "18:45")
    ),
    onSendMessage: (String) -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessageItem>().apply { addAll(initialMessages) } }
    val listState = rememberLazyListState()

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.asPaddingValues(density).calculateBottomPadding()

    LaunchedEffect(messages.size, imeBottom) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                modifier = Modifier.height(80.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(SleekIndigoPrimary, SleekIndigoSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌸", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Online",
                                fontSize = 11.sp,
                                color = OnlineGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach",
                            tint = SleekIndigoSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = if (isDark) DarkSurfaceVariant else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            textStyle = TextStyle(
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 15.sp
                            ),
                            placeholder = {
                                Text(
                                    "Message...",
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("message_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = SleekIndigoSecondary,
                                focusedPlaceholderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                unfocusedPlaceholderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            ),
                            maxLines = 4
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Crossfade(targetState = messageText.isNotBlank(), label = "btn_transition") { hasText ->
                        if (hasText) {
                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        val newMsg = ChatMessageItem(
                                            id = System.currentTimeMillis().toString(),
                                            senderName = "Me",
                                            text = messageText.trim(),
                                            isFromMe = true
                                        )
                                        messages.add(newMsg)
                                        onSendMessage(messageText.trim())
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(SleekIndigoPrimary, SleekIndigoSecondary)
                                        )
                                    )
                                    .testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) DarkSurfaceVariant else SleekIndigoLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mic",
                                    tint = SleekIndigoSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("message_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageRow(message = msg, isDark = isDark)
                }
            }
        }
    }
}

@Composable
fun ChatMessageRow(message: ChatMessageItem, isDark: Boolean = true) {
    val bubbleColor = if (message.isFromMe) SleekIndigoPrimary else if (isDark) DarkSurface else Color.White
    val textColor = if (message.isFromMe) Color.White else if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val timeColor = if (message.isFromMe) Color.White.copy(alpha = 0.75f) else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromMe) 18.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (message.isFromMe) 2.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 14.5.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = message.timestamp,
                    color = timeColor,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

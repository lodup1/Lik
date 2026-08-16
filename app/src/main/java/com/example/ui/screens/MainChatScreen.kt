package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus
import com.example.data.model.PairingInfo
import com.example.data.model.UserAccount
import com.example.ui.components.MediaAttachmentDialog
import com.example.ui.components.WallpaperPickerDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SleekIndigoLight
import com.example.ui.theme.SleekIndigoPrimary
import com.example.ui.theme.SleekIndigoSecondary
import com.example.ui.viewmodel.CallViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ThemeViewModel
import com.example.ui.viewmodel.WallpaperConfig
import com.example.ui.viewmodel.WallpaperType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatScreen(
    currentUser: UserAccount?,
    pairingInfo: PairingInfo,
    chatViewModel: ChatViewModel,
    callViewModel: CallViewModel,
    themeViewModel: ThemeViewModel? = null,
    onOpenProfile: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wallpaperConfig = themeViewModel?.wallpaperConfig?.collectAsState()?.value ?: WallpaperConfig()
    val messages by chatViewModel.messages.collectAsState()
    val isPartnerTyping by chatViewModel.isPartnerTyping.collectAsState()
    val isSearchActive by chatViewModel.isSearchActive.collectAsState()
    val searchQuery by chatViewModel.searchQuery.collectAsState()
    val replyMessage by chatViewModel.selectedReplyMessage.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showMediaDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Contextual message actions state
    var selectedMessageForMenu by remember { mutableStateOf<ChatMessage?>(null) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    var messageToForward by remember { mutableStateOf<ChatMessage?>(null) }
    var highlightedMessageId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // Detect keyboard / IME height changes
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val imeBottom = imeInsets.asPaddingValues(density).calculateBottomPadding()

    // Auto scroll to bottom when new messages arrive or keyboard opens
    LaunchedEffect(messages.size, imeBottom) {
        if (messages.isNotEmpty() && highlightedMessageId == null) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showMediaDialog) {
        MediaAttachmentDialog(
            onDismiss = { showMediaDialog = false },
            onSendPhoto = { url -> chatViewModel.sendPhotoAttachment(url) },
            onSendVideo = { url -> chatViewModel.sendVideoAttachment(url) }
        )
    }

    if (showWallpaperDialog && themeViewModel != null) {
        WallpaperPickerDialog(
            wallpaperConfig = wallpaperConfig,
            themeViewModel = themeViewModel,
            onDismiss = { showWallpaperDialog = false }
        )
    }

    // Message Action Modal Sheet / Menu
    if (selectedMessageForMenu != null) {
        val activeMsg = selectedMessageForMenu!!

        ModalBottomSheet(
            onDismissRequest = { selectedMessageForMenu = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.testTag("message_action_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Header preview
                val previewSnippet = when (activeMsg.mediaType) {
                    MediaType.IMAGE -> if (activeMsg.text.isNotBlank()) "📷 ${activeMsg.text}" else "📷 Photo Attachment"
                    MediaType.VIDEO -> if (activeMsg.text.isNotBlank()) "🎥 ${activeMsg.text}" else "🎥 Video Attachment"
                    MediaType.VOICE -> "🎤 Voice Message"
                    else -> activeMsg.text.ifBlank { "Message" }
                }

                Text(
                    text = previewSnippet,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Reply
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val msg = activeMsg
                            selectedMessageForMenu = null
                            chatViewModel.setReplyMessage(msg)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("action_reply")
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = "Reply",
                        tint = SleekIndigoSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Reply",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 2. Copy (only if text is present)
                if (activeMsg.text.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val textToCopy = activeMsg.text
                                selectedMessageForMenu = null
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Chat Message", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .testTag("action_copy")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = SleekIndigoSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Copy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 3. Forward
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val msg = activeMsg
                            selectedMessageForMenu = null
                            messageToForward = msg
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("action_forward")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Forward,
                        contentDescription = "Forward",
                        tint = SleekIndigoSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Forward",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 4. Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val msg = activeMsg
                            selectedMessageForMenu = null
                            messageToDelete = msg
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("action_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Delete",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (messageToDelete != null) {
        val targetMsg = messageToDelete!!
        val isMyMsg = targetMsg.senderId == (currentUser?.id ?: "me")

        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Delete message?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isMyMsg) {
                        "Do you want to delete this message for everyone or only for yourself?"
                    } else {
                        "This message will be removed from your chat on this device."
                    },
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                if (isMyMsg) {
                    TextButton(
                        onClick = {
                            val id = targetMsg.id
                            messageToDelete = null
                            chatViewModel.deleteMessageForEveryone(id)
                            Toast.makeText(context, "Deleted for everyone", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Delete for everyone", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    TextButton(
                        onClick = {
                            val id = targetMsg.id
                            messageToDelete = null
                            chatViewModel.deleteMessageForMe(id)
                            Toast.makeText(context, "Deleted for me", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Delete for me", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                Row {
                    if (isMyMsg) {
                        TextButton(
                            onClick = {
                                val id = targetMsg.id
                                messageToDelete = null
                                chatViewModel.deleteMessageForMe(id)
                                Toast.makeText(context, "Deleted for me", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Delete for me")
                        }
                    }
                    TextButton(onClick = { messageToDelete = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Forward Confirmation Dialog
    if (messageToForward != null) {
        val targetMsg = messageToForward!!
        val partnerName = (pairingInfo.partnerDisplayName?.takeIf { it.isNotBlank() }
            ?: pairingInfo.partnerUsername?.takeIf { it.isNotBlank() }
            ?: "").removePrefix("@")

        AlertDialog(
            onDismissRequest = { messageToForward = null },
            title = { Text("Forward message", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Forward message to:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(SleekIndigoPrimary, SleekIndigoSecondary))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!pairingInfo.partnerAvatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = pairingInfo.partnerAvatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(pairingInfo.partnerAvatarEmoji ?: "🌸", fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(partnerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Connected Room", fontSize = 12.sp, color = OnlineGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val previewTxt = when (targetMsg.mediaType) {
                        MediaType.IMAGE -> if (targetMsg.text.isNotBlank()) "📷 ${targetMsg.text}" else "📷 Photo Attachment"
                        MediaType.VIDEO -> if (targetMsg.text.isNotBlank()) "🎥 ${targetMsg.text}" else "🎥 Video Attachment"
                        MediaType.VOICE -> "🎤 Voice Note"
                        else -> targetMsg.text.ifBlank { "Message" }
                    }
                    Text(
                        text = "Message: \"$previewTxt\"",
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = targetMsg
                        messageToForward = null
                        chatViewModel.forwardMessage(msg)
                        Toast.makeText(context, "Message forwarded", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekIndigoPrimary)
                ) {
                    Text("Forward")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToForward = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        windowInsets = WindowInsets.statusBars,
                        modifier = Modifier.height(80.dp),
                        navigationIcon = {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("back_to_chats_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Chats",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenProfile() }
                                    .padding(vertical = 4.dp)
                            ) {
                                // Profile Avatar with Online Dot
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                ) {
                                    if (!pairingInfo.partnerAvatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = pairingInfo.partnerAvatarUrl,
                                            contentDescription = "Partner Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(SleekIndigoPrimary, SleekIndigoSecondary)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = pairingInfo.partnerAvatarEmoji ?: "🌸",
                                                fontSize = 20.sp,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                    // Online Status Dot
                                    Box(
                                        modifier = Modifier
                                            .size(11.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .align(Alignment.BottomEnd)
                                            .padding(1.5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(OnlineGreen)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val chatHeaderTitle = (pairingInfo.partnerDisplayName?.takeIf { it.isNotBlank() }
                                        ?: pairingInfo.partnerUsername?.takeIf { it.isNotBlank() }
                                        ?: "Chat").removePrefix("@")
                                    Text(
                                        text = chatHeaderTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val statusSubtext = when {
                                        isPartnerTyping -> "typing..."
                                        !pairingInfo.partnerStatusMood.isNullOrBlank() -> "Online • ${pairingInfo.partnerStatusMood}"
                                        else -> "Online"
                                    }
                                    Text(
                                        text = statusSubtext,
                                        fontSize = 12.sp,
                                        fontWeight = if (isPartnerTyping) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isPartnerTyping) SleekIndigoSecondary else OnlineGreen,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { chatViewModel.toggleSearch() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("search_chat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { callViewModel.startVoiceCall() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("voice_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Voice Call",
                                    tint = SleekIndigoSecondary
                                )
                            }

                            IconButton(
                                onClick = { callViewModel.startVideoCall() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("video_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Video Call",
                                    tint = SleekIndigoSecondary
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showMoreMenu = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("chat_more_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Partner Profile") },
                                        onClick = {
                                            showMoreMenu = false
                                            onOpenProfile()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = SleekIndigoSecondary)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Chat Wallpaper") },
                                        onClick = {
                                            showMoreMenu = false
                                            showWallpaperDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Wallpaper, contentDescription = null, tint = SleekIndigoSecondary)
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // In-Chat Live Search Bar
                    AnimatedVisibility(visible = isSearchActive) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { chatViewModel.updateSearchQuery(it) },
                                placeholder = { Text("Search messages...", fontSize = 14.sp) },
                                singleLine = true,
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { chatViewModel.updateSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .testTag("search_chat_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Anchor Composer in bottomBar with IME padding
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Modern Reply Banner with smooth entrance
                    AnimatedVisibility(
                        visible = replyMessage != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.95f),
                        exit = fadeOut() + scaleOut(targetScale = 0.95f)
                    ) {
                        replyMessage?.let { reply ->
                            val isMyReply = reply.senderId == (currentUser?.id ?: "me")
                            val replyAuthor = if (isMyReply) "You" else (pairingInfo.partnerDisplayName ?: pairingInfo.partnerUsername ?: "Partner")
                            val previewText = when (reply.mediaType) {
                                MediaType.IMAGE -> if (reply.text.isNotBlank()) "📷 ${reply.text}" else "📷 Photo"
                                MediaType.VIDEO -> if (reply.text.isNotBlank()) "🎥 ${reply.text}" else "🎥 Video"
                                MediaType.VOICE -> "🎤 Voice message"
                                else -> reply.text.ifBlank { "Message" }
                            }

                            Surface(
                                color = if (isDarkTheme) DarkSurfaceVariant else Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.5.dp)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(SleekIndigoPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Replying to $replyAuthor",
                                            fontSize = 12.sp,
                                            color = SleekIndigoPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            previewText,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { chatViewModel.setReplyMessage(null) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Cancel Reply",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Modern Composer Surface
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Attachment Icon Button
                            IconButton(
                                onClick = { showMediaDialog = true },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("attach_media_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Attach Media",
                                    tint = SleekIndigoSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Message Text Input Field
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = if (isDarkTheme) DarkSurfaceVariant else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                TextField(
                                    value = textInput,
                                    onValueChange = {
                                        textInput = it
                                        chatViewModel.onUserTyping(it)
                                    },
                                    textStyle = TextStyle(
                                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp
                                    ),
                                    placeholder = {
                                        Text(
                                            "Message...",
                                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            fontSize = 15.sp
                                        )
                                    },
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chat_input_field"),
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                        unfocusedTextColor = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = SleekIndigoSecondary,
                                        focusedPlaceholderColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        unfocusedPlaceholderColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Dynamic Send / Mic Action Button with smooth transition
                            Crossfade(
                                targetState = textInput.isNotBlank(),
                                label = "composer_button_transition"
                            ) { hasText ->
                                if (hasText) {
                                    IconButton(
                                        onClick = {
                                            if (textInput.isNotBlank()) {
                                                chatViewModel.sendMessage(textInput.trim())
                                                textInput = ""
                                                scope.launch {
                                                    if (messages.isNotEmpty()) {
                                                        listState.animateScrollToItem(messages.size - 1)
                                                    }
                                                }
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
                                            .testTag("send_message_button")
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
                                        onClick = { chatViewModel.sendVoiceNoteAttachment() },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDarkTheme) DarkSurfaceVariant else SleekIndigoLight
                                            )
                                            .testTag("voice_note_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice Note",
                                            tint = SleekIndigoSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val backgroundGradient = if (isDarkTheme) {
            Brush.verticalGradient(listOf(DarkBackground, DarkSurface))
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFF1F5F9),
                    Color(0xFFEEF2FF)
                )
            )
        }

        // Chat Body Area (Exclusively between top header and bottom composer)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundGradient)
        ) {
            // Background Wallpaper Rendering (Behind messages only)
            when (wallpaperConfig.type) {
                WallpaperType.SOLID -> {
                    val color = try {
                        Color(android.graphics.Color.parseColor(wallpaperConfig.value))
                    } catch (e: Exception) {
                        Color.Transparent
                    }
                    Box(modifier = Modifier.fillMaxSize().background(color))
                }
                WallpaperType.GALLERY -> {
                    if (wallpaperConfig.value.isNotBlank() && File(wallpaperConfig.value).exists()) {
                        AsyncImage(
                            model = File(wallpaperConfig.value),
                            contentDescription = "Chat Wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Subtle dimming overlay so text stays readable
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                    }
                }
                WallpaperType.BUILTIN -> {
                    val brush = when (wallpaperConfig.value) {
                        "ROMANTIC_SUNSET" -> Brush.verticalGradient(listOf(Color(0xFF2E1065), Color(0xFF701A75), Color(0xFF9D174D)))
                        "STARRY_NIGHT" -> Brush.verticalGradient(listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B)))
                        "PASTEL_LAVENDER" -> Brush.verticalGradient(listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF6366F1)))
                        "COZY_ROSE" -> Brush.verticalGradient(listOf(Color(0xFF4C0519), Color(0xFF881337), Color(0xFFBE123C)))
                        "FOREST_EMERALD" -> Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669)))
                        "DEEP_SPACE" -> Brush.verticalGradient(listOf(Color(0xFF0B0F19), Color(0xFF1E1B4B), Color(0xFF311042)))
                        else -> Brush.verticalGradient(listOf(Color(0xFF090D16), Color(0xFF111827)))
                    }
                    Box(modifier = Modifier.fillMaxSize().background(brush))
                }
                WallpaperType.DEFAULT -> {
                    // Default gradient
                }
            }

            if (messages.isEmpty()) {
                // Empty Chat State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            SleekIndigoPrimary.copy(alpha = 0.2f),
                                            SleekIndigoSecondary.copy(alpha = 0.35f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pairingInfo.partnerAvatarEmoji ?: "💕",
                                fontSize = 32.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No messages yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Say hi to ${pairingInfo.partnerDisplayName ?: "your partner"}! ✨\nMessages are end-to-end encrypted.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Subtle E2E Encryption indicator
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = if (isDarkTheme) DarkSurfaceVariant.copy(alpha = 0.6f) else Color(0xFFE2E8F0).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted",
                                        tint = if (isDarkTheme) SleekIndigoSecondary else SleekIndigoPrimary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Messages are end-to-end encrypted",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(messages, key = { _, msg -> msg.id }) { index, msg ->
                        // Date Separator if previous message was on a different day
                        val showDateSeparator = if (index == 0) {
                            true
                        } else {
                            val prevMsg = messages[index - 1]
                            !isSameDay(prevMsg.timestamp, msg.timestamp)
                        }

                        if (showDateSeparator) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = if (isDarkTheme) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFE2E8F0).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(10.dp),
                                    shadowElevation = 1.dp
                                ) {
                                    Text(
                                        text = formatDateSeparator(msg.timestamp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        val isMe = msg.senderId == (currentUser?.id ?: "me")
                        val isHighlighted = (msg.id == highlightedMessageId) || (msg.id == selectedMessageForMenu?.id)

                        ChatMessageBubble(
                            message = msg,
                            isMe = isMe,
                            isHighlighted = isHighlighted,
                            partnerEmoji = pairingInfo.partnerAvatarEmoji ?: "🌸",
                            partnerAvatarUrl = pairingInfo.partnerAvatarUrl,
                            onLongPress = { selectedMessageForMenu = msg },
                            onReply = { chatViewModel.setReplyMessage(msg) },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Chat Message", msg.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { messageToDelete = msg },
                            onForward = { messageToForward = msg },
                            onRetry = { chatViewModel.retryUpload(msg.id) },
                            onReplyQuoteClick = { replyId ->
                                val targetIndex = messages.indexOfFirst { it.id == replyId }
                                if (targetIndex != -1) {
                                    scope.launch {
                                        listState.animateScrollToItem(targetIndex)
                                        highlightedMessageId = replyId
                                        delay(1500)
                                        if (highlightedMessageId == replyId) {
                                            highlightedMessageId = null
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // Partner Typing Indicator Bubble
                    if (isPartnerTyping) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(SleekIndigoLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pairingInfo.partnerAvatarEmoji ?: "🌸", fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isDarkTheme) DarkSurface else Color(0xFFEEF2FF),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SleekIndigoPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SleekIndigoSecondary)
                                        )
                                        Spacer(modifier = Modifier.width(7.dp))
                                        Text(
                                            "${(pairingInfo.partnerDisplayName ?: "Partner").uppercase()} IS TYPING",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = SleekIndigoSecondary
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    isHighlighted: Boolean = false,
    partnerEmoji: String,
    partnerAvatarUrl: String? = null,
    onLongPress: () -> Unit = {},
    onReply: () -> Unit = {},
    onCopy: () -> Unit = {},
    onDelete: () -> Unit = {},
    onForward: () -> Unit = {},
    onRetry: () -> Unit = {},
    onReplyQuoteClick: (replyId: String) -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground

    val bubbleColor = if (isMe) {
        SleekIndigoPrimary
    } else {
        if (isDark) DarkSurface else Color.White
    }

    val textColor = if (isMe) {
        Color.White
    } else {
        if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    }

    val metaColor = if (isMe) {
        Color.White.copy(alpha = 0.75f)
    } else {
        if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(SleekIndigoPrimary, SleekIndigoSecondary))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!partnerAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = partnerAvatarUrl,
                        contentDescription = "Partner Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(partnerEmoji, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp
                ),
                color = bubbleColor,
                shadowElevation = if (isHighlighted) 6.dp else if (isMe) 2.dp else 1.dp,
                border = if (isHighlighted) BorderStroke(2.dp, SleekIndigoSecondary) else null,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { },
                        onLongClick = onLongPress
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Reply Indicator Header (Clickable to scroll to original message)
                    message.replyToText?.let { replyTxt ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isMe) Color.Black.copy(alpha = 0.18f) else if (isDark) DarkSurfaceVariant else Color(0xFFEEF2FF)
                                )
                                .clickable {
                                    message.replyToId?.let { onReplyQuoteClick(it) }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = message.replyToSenderName ?: "Reply",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color(0xFFE0E7FF) else SleekIndigoPrimary
                                )
                                Text(
                                    text = replyTxt,
                                    color = if (isMe) Color.White.copy(alpha = 0.9f) else if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Media Content
                    when (message.mediaType) {
                        MediaType.IMAGE -> {
                            if (!message.mediaUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "Photo Attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                        MediaType.VIDEO -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                message.mediaSizeFormatted?.let { size ->
                                    Text(
                                        text = size,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        MediaType.VOICE -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isMe) Color.Black.copy(alpha = 0.15f) else if (isDark) DarkSurfaceVariant else Color(0xFFEEF2FF)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Voice Note",
                                    tint = if (isMe) Color.White else SleekIndigoPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Voice Note (0:15)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        else -> { }
                    }

                    // Text Content
                    if (message.isDeleted) {
                        Text(
                            text = "🚫 This message was deleted",
                            color = metaColor,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic
                        )
                    } else if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }

                    // Upload Progress Indicator
                    if (message.uploadProgress < 1.0f && message.status != MessageStatus.FAILED) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { message.uploadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isMe) Color.White else SleekIndigoPrimary,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }

                    // Metadata: Timestamp + Status Indicators
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime(message.timestamp),
                            color = metaColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Normal
                        )

                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            when (message.status) {
                                MessageStatus.SENDING -> {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(metaColor)
                                    )
                                }
                                MessageStatus.SENT -> {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Sent",
                                        tint = metaColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                MessageStatus.DELIVERED -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Delivered",
                                        tint = metaColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                MessageStatus.READ -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Read",
                                        tint = Color(0xFF67E8F9), // Bright cyan for read
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                MessageStatus.FAILED -> {
                                    IconButton(
                                        onClick = onRetry,
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Retry",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(14.dp)
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
}

private fun formatMessageTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateSeparator(timestamp: Long): String {
    val messageCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    val todayCalendar = Calendar.getInstance()
    val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        isSameDay(messageCalendar, todayCalendar) -> "Today"
        isSameDay(messageCalendar, yesterdayCalendar) -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return isSameDay(c1, c2)
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

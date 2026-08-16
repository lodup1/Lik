package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaType
import com.example.data.model.PairingInfo
import com.example.data.model.UserAccount
import com.example.ui.theme.LikRosePrimary
import com.example.ui.theme.LikRoseSecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SleekIndigoDark
import com.example.ui.theme.SleekIndigoLight
import com.example.ui.theme.SleekIndigoPrimary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CallViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.PairingViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

val MOOD_PRESETS = listOf(
    "💖 In Love",
    "😊 Happy",
    "🥺 Missing You",
    "😴 Sleepy",
    "☕ Busy Working",
    "🍕 Eating",
    "🎮 Gaming",
    "🎧 Vibing",
    "✨ Excited",
    "🌧️ Need a Hug",
    "📚 Studying",
    "💪 Working Out"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatListScreen(
    currentUser: UserAccount?,
    pairingInfo: PairingInfo,
    chatViewModel: ChatViewModel,
    pairingViewModel: PairingViewModel,
    authViewModel: AuthViewModel? = null,
    callViewModel: CallViewModel? = null,
    onOpenChat: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPairing: () -> Unit,
    onOpenOurSpace: () -> Unit = {}
) {
    val context = LocalContext.current
    var showMoodSelectorDialog by remember { mutableStateOf(false) }
    var showPartnerMoodDialog by remember { mutableStateOf(false) }

    val messages by chatViewModel.messages.collectAsState()
    val lastMessage = messages.lastOrNull()

    // Dynamic calculations for Our Space
    val now = System.currentTimeMillis()
    val connectedSinceMs = pairingInfo.connectedSinceTimestamp
    val diffMillis = (now - connectedSinceMs).coerceAtLeast(0L)
    val connectedDays = (TimeUnit.MILLISECONDS.toDays(diffMillis) + 1).toInt()

    val (anniversaryDaysLeft, nextAnniversaryDateStr) = remember(pairingInfo.anniversaryDateMs, now) {
        calculateNextAnniversary(pairingInfo.anniversaryDateMs, now)
    }

    val togetherSinceDateFormatted = remember(connectedSinceMs) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(connectedSinceMs))
    }

    val lastChatFormatted = remember(lastMessage?.timestamp) {
        if (lastMessage != null) {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            sdf.format(Date(lastMessage.timestamp))
        } else {
            "No messages yet"
        }
    }

    // Subtle gentle pulse animation for central partner avatar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dp")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lik",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            color = SleekIndigoLight,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (pairingInfo.isPaired) OnlineGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (pairingInfo.isPaired) "UsSpace Connected" else "Not Paired",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SleekIndigoPrimary
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Profile Button displaying current user's DP
                    IconButton(
                        onClick = onOpenProfile,
                        modifier = Modifier.testTag("open_profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SleekIndigoLight)
                                .border(1.5.dp, SleekIndigoPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUser?.customAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentUser?.customAvatarUrl,
                                    contentDescription = "My Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = currentUser?.avatarEmoji ?: "👤",
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!pairingInfo.isPaired) {
                FloatingActionButton(
                    onClick = onOpenPairing,
                    containerColor = SleekIndigoPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("new_pairing_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Pair Partner")
                }
            }
        }
    ) { innerPadding ->
        if (pairingInfo.isPaired) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(36.dp))

                    // ==========================================
                    // 1. PARTNER MOOD BUBBLE (ABOVE DP)
                    // ==========================================
                    val partnerMoodText = pairingInfo.partnerStatusMood?.takeIf { it.isNotBlank() }
                        ?: "💭 Tap to chat with me ✨"

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SleekIndigoLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekIndigoPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showPartnerMoodDialog = true }
                            .testTag("partner_mood_bubble")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = partnerMoodText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekIndigoPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Partner Mood",
                                tint = SleekIndigoPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ==========================================
                    // 2. CENTRAL PARTNER DP WITH GREEN ONLINE INDICATOR
                    // Tapping DP opens the 1-to-1 chat
                    // ==========================================
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .clickable { onOpenChat() }
                            .testTag("central_partner_dp_widget")
                    ) {
                        // Outer decorative gradient glowing ring
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(
                                            SleekIndigoPrimary,
                                            LikRosePrimary,
                                            SleekIndigoLight,
                                            LikRoseSecondary,
                                            SleekIndigoPrimary
                                        )
                                    )
                                )
                                .padding(4.dp)
                        ) {
                            // Inner container for Partner's DP
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!pairingInfo.partnerAvatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = pairingInfo.partnerAvatarUrl,
                                        contentDescription = "Partner Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(
                                                        SleekIndigoLight,
                                                        MaterialTheme.colorScheme.surface
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pairingInfo.partnerAvatarEmoji ?: "🌸",
                                            fontSize = 68.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Online indicator badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 8.dp, end = 8.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(OnlineGreen)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ==========================================
                    // 3. PARTNER'S USERNAME (WITHOUT @ SYMBOL)
                    // ==========================================
                    val rawUsername = pairingInfo.partnerUsername?.takeIf { it.isNotBlank() }
                        ?: pairingInfo.partnerDisplayName?.takeIf { it.isNotBlank() }
                        ?: ""
                    val cleanUsername = rawUsername.removePrefix("@")

                    Text(
                        text = cleanUsername,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 4. COMPACT "MY MOOD" ROW
                // Show "My Mood" in one small row with current mood and pencil/edit button
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showMoodSelectorDialog = true }
                        .testTag("my_mood_compact_row")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SleekIndigoLight,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.SentimentSatisfied,
                                        contentDescription = null,
                                        tint = SleekIndigoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "My Mood:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val myCurrentMood = currentUser?.statusMood?.takeIf { it.isNotBlank() } ?: "None set"
                            Text(
                                text = myCurrentMood,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser?.statusMood.isNullOrBlank()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                } else {
                                    SleekIndigoPrimary
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { showMoodSelectorDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SleekIndigoLight)
                                .testTag("edit_my_mood_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Mood",
                                tint = SleekIndigoPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 5. OUR SPACE CARD (DIRECTLY BELOW MY MOOD)
                // ==========================================
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                SleekIndigoPrimary.copy(alpha = 0.45f),
                                LikRosePrimary.copy(alpha = 0.35f),
                                SleekIndigoPrimary.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("home_our_space_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "💜",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Our Space",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "✨",
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid / Rows
                        // Row 1: Connected for & Last chat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔥 Connected for",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$connectedDays days",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekIndigoPrimary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "💬 Last chat",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lastChatFormatted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row 2: Together since & Anniversary Left
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📅 Together since",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = togetherSinceDateFormatted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "❤️ Anniversary Left",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$anniversaryDaysLeft days left",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LikRosePrimary
                                )
                                Text(
                                    text = nextAnniversaryDateStr,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // View Our Space Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenOurSpace() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View Our Space",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekIndigoPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Our Space",
                                tint = SleekIndigoPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Unpaired empty state
            EmptyChatsView(
                message = "You are not paired yet. Connect with your partner to share moods and start chatting privately!",
                showActionButton = true,
                actionText = "Pair with Partner",
                onAction = onOpenPairing
            )
        }
    }

    // ==========================================
    // DIALOG: MOOD SELECTOR (PRESETS + CUSTOM)
    // ==========================================
    if (showMoodSelectorDialog) {
        var customMoodInput by remember { mutableStateOf(currentUser?.statusMood ?: "") }

        AlertDialog(
            onDismissRequest = { showMoodSelectorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SentimentSatisfied, contentDescription = null, tint = SleekIndigoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set My Mood", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Presets:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MOOD_PRESETS.forEach { mood ->
                            val isSelected = currentUser?.statusMood == mood
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) SleekIndigoPrimary else SleekIndigoLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (isSelected) {
                                            authViewModel?.updateStatusMood(null)
                                            Toast.makeText(context, "Mood removed", Toast.LENGTH_SHORT).show()
                                        } else {
                                            authViewModel?.updateStatusMood(mood)
                                            Toast.makeText(context, "Mood updated to $mood", Toast.LENGTH_SHORT).show()
                                        }
                                        showMoodSelectorDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mood,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SleekIndigoPrimary
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Or write custom status:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = customMoodInput,
                        onValueChange = { customMoodInput = it },
                        placeholder = { Text("e.g. Cooking dinner 🍳, Thinking of you 💕") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick emoji add row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("💖", "😊", "🥺", "😴", "☕", "🍕", "🎮", "✨").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { customMoodInput = "$emoji $customMoodInput".trim() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customMoodInput.trim()
                        if (trimmed.isNotBlank()) {
                            authViewModel?.updateStatusMood(trimmed)
                            Toast.makeText(context, "Mood updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel?.updateStatusMood(null)
                        }
                        showMoodSelectorDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Mood")
                }
            },
            dismissButton = {
                Row {
                    if (!currentUser?.statusMood.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                authViewModel?.updateStatusMood(null)
                                Toast.makeText(context, "Mood cleared", Toast.LENGTH_SHORT).show()
                                showMoodSelectorDialog = false
                            }
                        ) {
                            Text("Clear Mood", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showMoodSelectorDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // ==========================================
    // DIALOG: EDIT PARTNER MOOD (FOR TESTING / QUICK UPDATE)
    // ==========================================
    if (showPartnerMoodDialog) {
        var partnerMoodInput by remember { mutableStateOf(pairingInfo.partnerStatusMood ?: "") }
        val partnerNameClean = (pairingInfo.partnerDisplayName?.takeIf { it.isNotBlank() }
            ?: pairingInfo.partnerUsername?.takeIf { it.isNotBlank() }
            ?: "").removePrefix("@")

        AlertDialog(
            onDismissRequest = { showPartnerMoodDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = LikRosePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (partnerNameClean.isNotBlank()) "$partnerNameClean's Mood" else "Status / Mood", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (partnerNameClean.isNotBlank()) "Update $partnerNameClean's mood bubble:" else "Update mood bubble:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = partnerMoodInput,
                        onValueChange = { partnerMoodInput = it },
                        placeholder = { Text("e.g. Missing You 💕, In Love 💖") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("💖 In Love", "🥺 Missing You", "😊 Happy", "✨ Thinking of you", "😴 Sleepy").forEach { mood ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SleekIndigoLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { partnerMoodInput = mood }
                            ) {
                                Text(
                                    text = mood,
                                    fontSize = 11.sp,
                                    color = SleekIndigoPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = partnerMoodInput.trim()
                        pairingViewModel.updatePartnerStatusMood(if (trimmed.isNotBlank()) trimmed else null)
                        Toast.makeText(context, "Partner status updated!", Toast.LENGTH_SHORT).show()
                        showPartnerMoodDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                if (!pairingInfo.partnerStatusMood.isNullOrBlank()) {
                    TextButton(
                        onClick = {
                            pairingViewModel.updatePartnerStatusMood(null)
                            Toast.makeText(context, "Partner mood cleared", Toast.LENGTH_SHORT).show()
                            showPartnerMoodDialog = false
                        }
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = { showPartnerMoodDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun EmptyChatsView(
    message: String,
    showActionButton: Boolean = true,
    actionText: String = "Start a Chat",
    onAction: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "No Chats",
                modifier = Modifier.size(72.dp),
                tint = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Partner Connected",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (showActionButton) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekIndigoPrimary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("empty_start_chat_button")
                ) {
                    Text(text = actionText, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}

private fun calculateNextAnniversary(originalAnniversaryMs: Long, nowMs: Long): Pair<Int, String> {
    val origCal = Calendar.getInstance().apply { timeInMillis = originalAnniversaryMs }
    val nextAnnivCal = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.MONTH, origCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, origCal.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val todayStart = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (nextAnnivCal.before(todayStart)) {
        nextAnnivCal.add(Calendar.YEAR, 1)
    }

    val diffMs = nextAnnivCal.timeInMillis - todayStart.timeInMillis
    val daysLeft = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()

    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val dateStr = sdf.format(nextAnnivCal.time)

    return Pair(daysLeft, dateStr)
}

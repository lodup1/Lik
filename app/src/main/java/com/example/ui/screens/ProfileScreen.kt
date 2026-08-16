package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PairingInfo
import com.example.data.model.UserAccount
import com.example.ui.components.WallpaperPickerDialog
import com.example.ui.theme.LikRosePrimary
import com.example.ui.theme.LikRoseSecondary
import com.example.ui.theme.SleekIndigoLight
import com.example.ui.theme.SleekIndigoPrimary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.PairingViewModel
import com.example.ui.viewmodel.ThemeViewModel
import com.example.ui.viewmodel.WallpaperConfig
import com.example.ui.viewmodel.WallpaperType
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserAccount?,
    pairingInfo: PairingInfo,
    authViewModel: AuthViewModel,
    pairingViewModel: PairingViewModel,
    themeViewModel: ThemeViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onOpenPairing: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val wallpaperConfig by themeViewModel.wallpaperConfig.collectAsState()

    var showMyDpDialog by remember { mutableStateOf(false) }
    var showPartnerDpDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showMyMoodDialog by remember { mutableStateOf(false) }
    var showPartnerMoodDialog by remember { mutableStateOf(false) }

    var selectedEmoji by remember { mutableStateOf(currentUser?.avatarEmoji ?: "❤️") }
    var showLastSeen by remember { mutableStateOf(true) }

    val emojis = listOf("❤️", "💖", "🌸", "✨", "🌹", "🧸", "🦋", "👑")

    // Activity Launchers for User DP
    val myGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = saveUriToInternalStorage(context, it, "user_dp")
            if (localPath != null) {
                authViewModel.updateProfile(
                    displayName = currentUser?.displayName ?: "",
                    avatarEmoji = selectedEmoji,
                    avatarUrl = localPath
                )
                Toast.makeText(context, "Profile Picture updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val myCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val localPath = saveBitmapToInternalStorage(context, it, "user_dp")
            if (localPath != null) {
                authViewModel.updateProfile(
                    displayName = currentUser?.displayName ?: "",
                    avatarEmoji = selectedEmoji,
                    avatarUrl = localPath
                )
                Toast.makeText(context, "Photo captured & DP updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Activity Launchers for Partner DP
    val partnerGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = saveUriToInternalStorage(context, it, "partner_dp")
            if (localPath != null) {
                pairingViewModel.updatePartnerDp(localPath)
                Toast.makeText(context, "Partner Profile Picture updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val partnerCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val localPath = saveBitmapToInternalStorage(context, it, "partner_dp")
            if (localPath != null) {
                pairingViewModel.updatePartnerDp(localPath)
                Toast.makeText(context, "Partner photo captured!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Activity Launcher for Chat Wallpaper from Gallery
    val wallpaperGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            themeViewModel.setGalleryWallpaper(it)
            Toast.makeText(context, "Chat Wallpaper updated!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // USER AVATAR & DP BOX
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(SleekIndigoPrimary, SleekIndigoLight))
                        )
                        .border(3.dp, SleekIndigoPrimary, CircleShape)
                        .clickable { showMyDpDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser?.customAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser?.customAvatarUrl,
                            contentDescription = "My Profile DP",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(selectedEmoji, fontSize = 52.sp)
                    }
                }

                // Camera Badge Button on DP
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SleekIndigoPrimary)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { showMyDpDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change DP",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentUser?.displayName ?: "User",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "@${currentUser?.username ?: "username"}",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mood / Status Badge
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showMyMoodDialog = true },
                color = SleekIndigoLight,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentUser?.statusMood.takeIf { !it.isNullOrBlank() } ?: "💭 Set Status / Mood",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekIndigoPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Edit, contentDescription = null, tint = SleekIndigoPrimary, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Change / Remove DP Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showMyDpDialog = true },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Change DP", fontSize = 13.sp)
                }

                if (!currentUser?.customAvatarUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            deleteInternalStorageFiles(context, "user_dp")
                            authViewModel.updateProfile(
                                displayName = currentUser?.displayName ?: "",
                                avatarEmoji = selectedEmoji,
                                avatarUrl = null
                            )
                            Toast.makeText(context, "DP Removed", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove DP", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MY STATUS & MOOD CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MY STATUS & MOOD", style = MaterialTheme.typography.labelMedium.copy(color = SleekIndigoPrimary, fontWeight = FontWeight.Bold))
                        TextButton(onClick = { showMyMoodDialog = true }) {
                            Text(if (currentUser?.statusMood.isNullOrBlank()) "Set Status" else "Edit Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekIndigoPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showMyMoodDialog = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekIndigoLight,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💭", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.statusMood.takeIf { !it.isNullOrBlank() } ?: "No status set yet",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (!currentUser?.statusMood.isNullOrBlank()) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!currentUser?.statusMood.isNullOrBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "Appears in private chat header and profile",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = SleekIndigoPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji Selection Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CHOOSE AVATAR EMOJI", style = MaterialTheme.typography.labelMedium.copy(color = SleekIndigoPrimary, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        emojis.take(4).forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedEmoji == emoji) SleekIndigoPrimary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        selectedEmoji = emoji
                                        authViewModel.updateProfile(
                                            displayName = currentUser?.displayName ?: "",
                                            avatarEmoji = emoji,
                                            avatarUrl = currentUser?.customAvatarUrl
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Partner Info Card with Partner DP & Status customization
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONNECTED PARTNER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SleekIndigoPrimary))
                        Row {
                            TextButton(onClick = { showPartnerDpDialog = true }) {
                                Text("DP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekIndigoPrimary)
                            }
                            TextButton(onClick = { showPartnerMoodDialog = true }) {
                                Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekIndigoPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SleekIndigoLight)
                                    .border(2.dp, SleekIndigoPrimary, CircleShape)
                                    .clickable { showPartnerDpDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!pairingInfo.partnerAvatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = pairingInfo.partnerAvatarUrl,
                                        contentDescription = "Partner DP",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(pairingInfo.partnerAvatarEmoji ?: "🌸", fontSize = 28.sp)
                                }
                            }

                            // Camera badge on partner DP
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(SleekIndigoPrimary)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .clickable { showPartnerDpDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Partner DP",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val partnerDisplay = (pairingInfo.partnerDisplayName?.takeIf { it.isNotBlank() }
                                ?: pairingInfo.partnerUsername?.takeIf { it.isNotBlank() }
                                ?: "").removePrefix("@")
                            Text(partnerDisplay, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            if (!pairingInfo.partnerStatusMood.isNullOrBlank()) {
                                Text(
                                    text = pairingInfo.partnerStatusMood,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SleekIndigoPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            } else {
                                Text("No status set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPartnerDpDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Partner DP", fontSize = 12.sp)
                        }

                        if (!pairingInfo.partnerAvatarUrl.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    deleteInternalStorageFiles(context, "partner_dp")
                                    pairingViewModel.updatePartnerDp(null)
                                    Toast.makeText(context, "Partner DP Removed", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CHAT CODE & 1-TO-1 PAIRING CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = SleekIndigoPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Chat Code & 1-on-1 Pairing",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your private 1-on-1 connection identifier. Share with your friend to connect.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // My Chat Code Display
                    val myCode = currentUser?.pairCode ?: "LIK-XXXX"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SleekIndigoLight.copy(alpha = 0.6f))
                            .border(1.dp, SleekIndigoPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MY CHAT CODE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = SleekIndigoPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = myCode,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = SleekIndigoPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons: Copy & Share & Enter New Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(myCode))
                                Toast.makeText(context, "Chat Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekIndigoLight,
                                contentColor = SleekIndigoPrimary
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Connect on Lik")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Connect with me on Lik using my Chat Code: $myCode"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Chat Code"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekIndigoPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Enter Code or Switch Pairing Button
                    OutlinedButton(
                        onClick = {
                            onOpenPairing()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reconnect_pair_code_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SleekIndigoPrimary
                        )
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pairingInfo.isPaired) "Enter New Chat Code / Re-Pair" else "Enter Friend's Chat Code",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CHAT WALLPAPER SETTINGS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wallpaper, contentDescription = null, tint = SleekIndigoPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Personal Chat Wallpaper", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Customize background wallpaper for your 1-on-1 private chat feed.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wallpaper Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Gallery Button
                        Button(
                            onClick = { wallpaperGalleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekIndigoLight, contentColor = SleekIndigoPrimary)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Options / Preset Button
                        Button(
                            onClick = { showWallpaperDialog = true },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekIndigoPrimary, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Solid / Built-in", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (wallpaperConfig.type != WallpaperType.DEFAULT) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = {
                                themeViewModel.resetWallpaperToDefault()
                                Toast.makeText(context, "Wallpaper reset to default", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset to Default", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences & Privacy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PREFERENCES & PRIVACY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SleekIndigoPrimary))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = SleekIndigoPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Dark Theme Canvas", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { themeViewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = SleekIndigoPrimary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenPrivacyPolicy)
                            .padding(vertical = 6.dp)
                            .testTag("privacy_policy_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SleekIndigoPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("Read our data handling practices", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Privacy Policy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = {
                    authViewModel.logout()
                    onLoggedOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Session", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // DIALOG: MY PROFILE DP OPTIONS
    if (showMyDpDialog) {
        AlertDialog(
            onDismissRequest = { showMyDpDialog = false },
            title = { Text("My Profile DP", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose how you would like to set your profile picture:")

                    Button(
                        onClick = {
                            showMyDpDialog = false
                            myGalleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }

                    Button(
                        onClick = {
                            showMyDpDialog = false
                            myCameraLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo with Camera")
                    }

                    if (!currentUser?.customAvatarUrl.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                showMyDpDialog = false
                                deleteInternalStorageFiles(context, "user_dp")
                                authViewModel.updateProfile(
                                    displayName = currentUser?.displayName ?: "",
                                    avatarEmoji = selectedEmoji,
                                    avatarUrl = null
                                )
                                Toast.makeText(context, "DP Removed", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remove DP")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMyDpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG: PARTNER DP OPTIONS
    if (showPartnerDpDialog) {
        val partnerNameClean = (pairingInfo.partnerDisplayName?.takeIf { it.isNotBlank() }
            ?: pairingInfo.partnerUsername?.takeIf { it.isNotBlank() }
            ?: "").removePrefix("@")
        AlertDialog(
            onDismissRequest = { showPartnerDpDialog = false },
            title = { Text(if (partnerNameClean.isNotBlank()) "$partnerNameClean's DP" else "Profile DP", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (partnerNameClean.isNotBlank()) "Set or change profile picture for $partnerNameClean:" else "Set or change profile picture:")

                    Button(
                        onClick = {
                            showPartnerDpDialog = false
                            partnerGalleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }

                    Button(
                        onClick = {
                            showPartnerDpDialog = false
                            partnerCameraLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo with Camera")
                    }

                    if (!pairingInfo.partnerAvatarUrl.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                showPartnerDpDialog = false
                                deleteInternalStorageFiles(context, "partner_dp")
                                pairingViewModel.updatePartnerDp(null)
                                Toast.makeText(context, "Partner DP Removed", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remove Partner DP")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPartnerDpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG: CHAT WALLPAPER PICKER (GALLERY, BUILTIN, SOLID, RESET)
    if (showWallpaperDialog) {
        WallpaperPickerDialog(
            wallpaperConfig = wallpaperConfig,
            themeViewModel = themeViewModel,
            onDismiss = { showWallpaperDialog = false }
        )
    }

    // DIALOG: SET MY MOOD / STATUS
    if (showMyMoodDialog) {
        SetMoodDialog(
            title = "Set My Status / Mood",
            currentStatus = currentUser?.statusMood,
            onDismiss = { showMyMoodDialog = false },
            onSaveStatus = { newStatus ->
                authViewModel.updateStatusMood(newStatus)
                Toast.makeText(context, if (newStatus != null) "Status updated!" else "Status cleared!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIALOG: SET PARTNER MOOD / STATUS
    if (showPartnerMoodDialog) {
        val partnerNameClean = (pairingInfo.partnerDisplayName?.takeIf { it.isNotBlank() }
            ?: pairingInfo.partnerUsername?.takeIf { it.isNotBlank() }
            ?: "").removePrefix("@")
        SetMoodDialog(
            title = if (partnerNameClean.isNotBlank()) "Set $partnerNameClean's Status" else "Set Status / Mood",
            currentStatus = pairingInfo.partnerStatusMood,
            onDismiss = { showPartnerMoodDialog = false },
            onSaveStatus = { newStatus ->
                pairingViewModel.updatePartnerStatusMood(newStatus)
                Toast.makeText(context, if (newStatus != null) "Status updated!" else "Status cleared!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SetMoodDialog(
    title: String,
    currentStatus: String?,
    onDismiss: () -> Unit,
    onSaveStatus: (String?) -> Unit
) {
    var customText by remember { mutableStateOf(currentStatus ?: "") }

    val presetMoods = listOf(
        "🏋️ At the gym",
        "💻 Working",
        "🎧 Listening to music",
        "😴 Sleeping",
        "☕ Having coffee",
        "🚗 Driving",
        "🎮 Gaming",
        "💖 Thinking of you",
        "📚 Studying",
        "✈️ Travelling",
        "🎬 Watching movies",
        "🍲 Eating"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    label = { Text("Status or mood text") },
                    placeholder = { Text("e.g. At the gym 🏋️") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Text("Quick Preset Moods", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetMoods.chunked(2).forEach { rowMoods ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMoods.forEach { mood ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { customText = mood },
                                    color = if (customText == mood) SleekIndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (customText == mood) Color.White else MaterialTheme.colorScheme.onSurface
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(mood, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            if (rowMoods.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveStatus(customText.trim().ifEmpty { null })
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekIndigoPrimary)
            ) {
                Text("Save Status")
            }
        },
        dismissButton = {
            Row {
                if (!currentStatus.isNullOrBlank()) {
                    TextButton(onClick = {
                        onSaveStatus(null)
                        onDismiss()
                    }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

private fun saveUriToInternalStorage(context: Context, uri: Uri, prefix: String): String? {
    return try {
        deleteInternalStorageFiles(context, prefix)
        val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String): String? {
    return try {
        deleteInternalStorageFiles(context, prefix)
        val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        outputStream.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun deleteInternalStorageFiles(context: Context, prefix: String) {
    try {
        context.filesDir.listFiles()?.filter { it.name.startsWith(prefix) }?.forEach { it.delete() }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

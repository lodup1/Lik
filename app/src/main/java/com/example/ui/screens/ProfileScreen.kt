package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.User
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.PairingViewModel
import com.example.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User?,
    authViewModel: AuthViewModel,
    pairingViewModel: PairingViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateToPrivacy: () -> Unit
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var editStatus by remember { mutableStateOf(currentUser?.statusMessage ?: "") }
    var editAvatar by remember { mutableStateOf(currentUser?.avatarEmoji ?: "❤️") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(currentUser?.avatarEmoji ?: "❤️", fontSize = 36.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentUser?.displayName ?: "You",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${currentUser?.username ?: "user"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${currentUser?.statusMessage ?: "Connected on Lik"}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                editName = currentUser?.displayName ?: ""
                                editStatus = currentUser?.statusMessage ?: ""
                                editAvatar = currentUser?.avatarEmoji ?: "❤️"
                                showEditProfileDialog = true
                            },
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile")
                        }
                    }
                }
            }

            // Connection Details
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Connection Details", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your Lik Code: ${currentUser?.pairCode ?: "LIK-XXXX"}")
                        Text("Partner Status: ${if (currentUser?.pairedPartnerId != null) "Connected (${currentUser.pairedPartnerId})" else "Not Paired"}")
                        if (currentUser?.pairedPartnerId != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { pairingViewModel.unpair() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("unpair_btn")
                            ) {
                                Text("Unpair Current Partner")
                            }
                        }
                    }
                }
            }

            // Preferences
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Preferences", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dark Theme")
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { themeViewModel.toggleDarkMode(it) },
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onNavigateToPrivacy,
                            modifier = Modifier.fillMaxWidth().testTag("privacy_policy_nav_btn")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Privacy Policy & Terms")
                                Icon(Icons.Default.ChevronRight, contentDescription = "Open")
                            }
                        }
                    }
                }
            }

            // Logout
            item {
                Button(
                    onClick = { authViewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_btn")
                ) {
                    Text("Log Out")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("Edit Profile") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Display Name") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editStatus,
                            onValueChange = { editStatus = it },
                            label = { Text("Status Message") },
                            singleLine = true
                        )
                        Text("Pick Avatar Emoji:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("❤️", "💖", "🌸", "🧸", "✨", "🐱").forEach { emoji ->
                                FilterChip(
                                    selected = editAvatar == emoji,
                                    onClick = { editAvatar = emoji },
                                    label = { Text(emoji, fontSize = 16.sp) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.updateProfile(
                                displayName = editName,
                                avatarEmoji = editAvatar,
                                status = editStatus,
                                mood = currentUser?.currentMood ?: "Happy"
                            )
                            showEditProfileDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

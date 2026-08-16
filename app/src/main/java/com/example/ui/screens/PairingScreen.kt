package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.ui.theme.SleekIndigoDark
import com.example.ui.theme.SleekIndigoLight
import com.example.ui.theme.SleekIndigoPrimary
import com.example.ui.theme.SleekIndigoSecondary
import com.example.ui.viewmodel.PairingUiState
import com.example.ui.viewmodel.PairingViewModel

@Composable
fun PairingScreen(
    currentUser: UserAccount?,
    pairingViewModel: PairingViewModel,
    onPairingComplete: () -> Unit
) {
    val pairingState by pairingViewModel.uiState.collectAsState()
    var partnerCodeInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Connect with Code, 1: My Chat Code
    var copiedToClipboard by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val myCode = currentUser?.pairCode ?: "LIK-8921"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SleekIndigoLight.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Branding: Lik Logo & Encryption Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(SleekIndigoPrimary, SleekIndigoSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lik",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Encrypted Private Space Badge
                    Surface(
                        color = SleekIndigoLight,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekIndigoPrimary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Private & Encrypted",
                                tint = SleekIndigoPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Private & Encrypted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekIndigoPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Security Shield Visual / Main Icon
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SleekIndigoPrimary, SleekIndigoDark)
                            )
                        )
                        .border(
                            4.dp,
                            SleekIndigoLight,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Secure Chat",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Text(
                    text = "Start a Private Chat",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Connect directly with your friend using a unique Chat Code. Pure 1-on-1 private space.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Custom Segmented Selector (Connect with Code vs My Chat Code)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    color = SleekIndigoLight.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 0: Enter Code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(21.dp))
                                .background(
                                    if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .clickable { selectedTab = 0 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Enter Chat Code",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) SleekIndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Tab 1: My Chat Code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(21.dp))
                                .background(
                                    if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .clickable { selectedTab = 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create / My Code",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) SleekIndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Card Content depending on selected tab
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "TabContent"
                ) { tab ->
                    if (tab == 0) {
                        // TAB 0: Enter Chat Code Screen
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "CONNECT WITH FRIEND",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = SleekIndigoPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Stylish Input Box
                                OutlinedTextField(
                                    value = partnerCodeInput,
                                    onValueChange = {
                                        partnerCodeInput = it.uppercase()
                                        if (pairingState is PairingUiState.Error) {
                                            pairingViewModel.clearError()
                                        }
                                    },
                                    placeholder = { Text("e.g. LIK-8921") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Key,
                                            contentDescription = null,
                                            tint = SleekIndigoPrimary
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (partnerCodeInput.isNotBlank()) {
                                                pairingViewModel.pairWithCode(partnerCodeInput)
                                                onPairingComplete()
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SleekIndigoPrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedContainerColor = SleekIndigoLight.copy(alpha = 0.3f),
                                        unfocusedContainerColor = SleekIndigoLight.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("partner_code_input"),
                                    shape = RoundedCornerShape(16.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Explanation text
                                Text(
                                    text = "Enter your friend's Chat Code to connect privately.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )

                                // Error Banner
                                if (pairingState is PairingUiState.Error) {
                                    val errorMsg = (pairingState as PairingUiState.Error).message
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "⚠️ $errorMsg",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Large Rounded Connect & Chat Button
                                Button(
                                    onClick = {
                                        if (partnerCodeInput.isNotBlank()) {
                                            pairingViewModel.pairWithCode(partnerCodeInput)
                                            onPairingComplete()
                                        } else {
                                            Toast.makeText(context, "Please enter your friend's Chat Code", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("pair_partner_button"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SleekIndigoPrimary
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Connect & Chat",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // TAB 1: Create / My Chat Code Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "YOUR UNIQUE CHAT CODE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = SleekIndigoPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Display My Chat Code
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    SleekIndigoLight,
                                                    SleekIndigoLight.copy(alpha = 0.5f)
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            SleekIndigoPrimary.copy(alpha = 0.3f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = myCode,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 3.sp,
                                                color = SleekIndigoPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap buttons below to copy or share",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action Buttons: Copy & Share
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Copy Code Button
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(myCode))
                                            copiedToClipboard = true
                                            Toast.makeText(context, "Chat Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SleekIndigoLight,
                                            contentColor = SleekIndigoPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (copiedToClipboard) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (copiedToClipboard) "Copied!" else "Copy Code",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Share Code Button
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
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SleekIndigoPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Share Code",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Send this Chat Code to your friend. Once they enter it on their screen, your private 1-on-1 space will open automatically.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Subtle Private / Secure Chat Guarantee Visual
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecurityBulletPoint(
                            icon = Icons.Default.Lock,
                            title = "End-to-End Private Channel",
                            subtitle = "Direct 1-on-1 connection protected with end-to-end encryption."
                        )
                        SecurityBulletPoint(
                            icon = Icons.Default.Shield,
                            title = "No Public Profiles",
                            subtitle = "Your space is completely private and accessible only via Chat Code."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SecurityBulletPoint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SleekIndigoLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SleekIndigoPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

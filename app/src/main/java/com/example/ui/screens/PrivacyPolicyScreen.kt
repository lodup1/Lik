package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekIndigoLight
import com.example.ui.theme.SleekIndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("privacy_policy_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("privacy_policy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary badge & Last Updated banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SleekIndigoLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SleekIndigoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Lik Privacy & Security",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Last updated: August 15, 2026",
                            style = MaterialTheme.typography.labelMedium,
                            color = SleekIndigoPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Section 1: Introduction
            PolicySectionCard(
                sectionNumber = "1",
                title = "Introduction",
                icon = Icons.Default.Info
            ) {
                Text(
                    text = "Welcome to Lik (\"we\", \"our\", or \"us\"). Lik is a dedicated, intimate 1-on-1 private messaging and connection platform created specifically for two paired individuals. We deeply respect your privacy and are committed to safeguarding the personal data and communications shared within your private space.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This Privacy Policy explains how information is collected, used, stored, and protected when you access or use the Lik Android application.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 2: Information We Collect
            PolicySectionCard(
                sectionNumber = "2",
                title = "Information We Collect",
                icon = Icons.Default.FolderShared
            ) {
                Text(
                    text = "We collect only the minimum necessary information required to provide private messaging, pairing, and calling features:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                PolicyBulletItem(
                    title = "Account Details:",
                    description = "Your unique username handle, secure password hash, date of birth, display name, and avatar/emoji choices."
                )
                PolicyBulletItem(
                    title = "Pairing Data:",
                    description = "Pairing codes generated to establish the private connection with your selected partner."
                )
                PolicyBulletItem(
                    title = "Communication Metadata:",
                    description = "Message timestamps, delivery and read receipts, reaction badges, and signaling metadata required to establish voice/video calls."
                )
                PolicyBulletItem(
                    title = "Technical Device Info:",
                    description = "Basic device model, OS version, and application preferences (such as Dark Theme and local chat wallpaper settings)."
                )
            }

            // Section 3: How We Use Information
            PolicySectionCard(
                sectionNumber = "3",
                title = "How We Use Information",
                icon = Icons.Default.Security
            ) {
                Text(
                    text = "We use the information collected solely to provide and enhance your experience:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                PolicyBulletItem(
                    title = "Facilitate 1-on-1 Communication:",
                    description = "To deliver instant messages, media attachments, status updates, and peer-to-peer audio/video calls between you and your paired contact."
                )
                PolicyBulletItem(
                    title = "Account Authentication:",
                    description = "To authenticate your username and password securely and manage your active login sessions."
                )
                PolicyBulletItem(
                    title = "Feature Preferences:",
                    description = "To remember your custom wallpapers, theme preferences, and mood bubbles."
                )
            }

            // Section 4: Messages and Chats
            PolicySectionCard(
                sectionNumber = "4",
                title = "Messages and Chats",
                icon = Icons.Default.ChatBubbleOutline
            ) {
                Text(
                    text = "Your private conversations belong to you and your partner. Messages are stored locally on your device in an on-device database and synchronized directly with your paired contact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You maintain full control over your chat history. You can edit messages, react with emojis, delete individual messages, or clear chat history directly within the conversation view.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 5: Profile Information
            PolicySectionCard(
                sectionNumber = "5",
                title = "Profile Information",
                icon = Icons.Default.Person
            ) {
                Text(
                    text = "Your profile consists of your chosen display name, username, custom profile picture (DP), avatar emoji, and dynamic status/mood bubble.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This profile information is visible only to your paired contact within the app. You can modify or remove your profile photo, status, or avatar emoji at any time in Profile & Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 6: Photos, Media and Files
            PolicySectionCard(
                sectionNumber = "6",
                title = "Photos, Media and Files",
                icon = Icons.Default.PhotoLibrary
            ) {
                Text(
                    text = "When you share photos, recorded voice notes, video clips, or choose custom chat wallpapers, the app accesses only the specific media files you explicitly select or capture.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lik does not scan, index, or access your wider media library, gallery, or document files outside the explicit picker interactions you initiate.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 7: Notifications
            PolicySectionCard(
                sectionNumber = "7",
                title = "Notifications",
                icon = Icons.Default.Notifications
            ) {
                Text(
                    text = "Lik utilizes system notifications to alert you when your paired contact sends a new message, updates their mood bubble, or initiates a voice or video call.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "On Android 13+ devices, notification permissions (POST_NOTIFICATIONS) are requested at runtime. You can manage or disable notification permissions at any time through your Android device settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 8: Camera and Device Permissions
            PolicySectionCard(
                sectionNumber = "8",
                title = "Camera and Device Permissions",
                icon = Icons.Default.Videocam
            ) {
                Text(
                    text = "The application requests specific device permissions only when required to provide core features:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                PolicyBulletItem(
                    title = "Camera (CAMERA):",
                    description = "Used solely when taking a new profile photo, snapping a photo message, or during video calls."
                )
                PolicyBulletItem(
                    title = "Microphone (RECORD_AUDIO):",
                    description = "Used solely when recording voice notes or participating in voice and video calls."
                )
                PolicyBulletItem(
                    title = "Notifications (POST_NOTIFICATIONS):",
                    description = "Used to display real-time incoming message and call alerts."
                )
                PolicyBulletItem(
                    title = "Photo / Media Access:",
                    description = "Used only via the system Photo Picker when you choose a profile picture or chat wallpaper."
                )
            }

            // Section 9: Data Storage and Security
            PolicySectionCard(
                sectionNumber = "9",
                title = "Data Storage and Security",
                icon = Icons.Default.Lock
            ) {
                Text(
                    text = "We implement robust security measures to protect your data:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                PolicyBulletItem(
                    title = "Local Device Storage:",
                    description = "Messages and profile caches are stored in private internal application storage and protected local databases."
                )
                PolicyBulletItem(
                    title = "Secure Transmission:",
                    description = "Network communications and media transfers utilize secure transport channels (HTTPS/WSS/WebRTC)."
                )
                PolicyBulletItem(
                    title = "Infrastructure Protection:",
                    description = "[Placeholder: Specific backend encryption standards, cloud storage providers, and server protocols are maintained per industry best practices]."
                )
            }

            // Section 10: Data Sharing
            PolicySectionCard(
                sectionNumber = "10",
                title = "Data Sharing",
                icon = Icons.Default.Shield
            ) {
                Text(
                    text = "We do not sell, rent, or monetize your personal information or conversation content to third parties, data brokers, or advertising networks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your data is shared exclusively with your designated paired partner to fulfill the direct messaging experience. [Placeholder: If required by lawful legal process or law enforcement under applicable jurisdictional laws, disclosures may occur strictly in accordance with legal mandates].",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 11: Data Retention and Deletion
            PolicySectionCard(
                sectionNumber = "11",
                title = "Data Retention and Deletion",
                icon = Icons.Default.FolderShared
            ) {
                Text(
                    text = "You maintain authority over your data stored in Lik:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                PolicyBulletItem(
                    title = "Unpairing & Reset:",
                    description = "Unpairing disconnects the relationship link and clears session access."
                )
                PolicyBulletItem(
                    title = "Chat Deletion:",
                    description = "You can delete individual messages or clear conversation history from your device."
                )
                PolicyBulletItem(
                    title = "Profile Removal:",
                    description = "You can delete custom profile pictures or clear your status at any time."
                )
                PolicyBulletItem(
                    title = "Account Sign-Out:",
                    description = "Logging out terminates active credentials and closes your local session. [Placeholder: Specific server-side data retention and account deletion timelines apply per organizational policy]."
                )
            }

            // Section 12: Children's Privacy
            PolicySectionCard(
                sectionNumber = "12",
                title = "Children's Privacy",
                icon = Icons.Default.Info
            ) {
                Text(
                    text = "Lik is not directed to children under the age of 13 (or the minimum legal age in your jurisdiction). We do not knowingly collect or solicit personal information from children.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "If we become aware that personal information has been collected from a child without verified parental consent, we will take prompt steps to remove such data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 13: Your Rights
            PolicySectionCard(
                sectionNumber = "13",
                title = "Your Rights",
                icon = Icons.Default.Security
            ) {
                Text(
                    text = "Depending on your geographic location and applicable privacy regulations (such as GDPR, CCPA, or regional equivalents), you may possess rights regarding your data:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                PolicyBulletItem(
                    title = "Right to Access & Review:",
                    description = "View the profile and account details associated with your account."
                )
                PolicyBulletItem(
                    title = "Right to Rectification:",
                    description = "Update or correct inaccurate display names, mood bubbles, or avatars directly in settings."
                )
                PolicyBulletItem(
                    title = "Right to Erasure:",
                    description = "Request removal of your account and personal information."
                )
            }

            // Section 14: Changes to This Privacy Policy
            PolicySectionCard(
                sectionNumber = "14",
                title = "Changes to This Privacy Policy",
                icon = Icons.Default.Info
            ) {
                Text(
                    text = "We may update this Privacy Policy from time to time to reflect modifications to app features, technological advancements, or regulatory requirements.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Any revisions will become effective upon being published with an updated \"Last updated\" date within this screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Section 15: Contact Us
            PolicySectionCard(
                sectionNumber = "15",
                title = "Contact Us",
                icon = Icons.Default.MailOutline
            ) {
                Text(
                    text = "If you have questions, feedback, or concerns regarding this Privacy Policy or our data practices, please reach out to us:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SleekIndigoLight
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Lik Support & Privacy Team",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SleekIndigoPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Email: [privacy@likapp.example.com / support contact placeholder]",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Application: Lik - Private Messaging",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySectionCard(
    sectionNumber: String,
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = SleekIndigoLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = sectionNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SleekIndigoPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SleekIndigoPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )

            content()
        }
    }
}

@Composable
private fun PolicyBulletItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(SleekIndigoPrimary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

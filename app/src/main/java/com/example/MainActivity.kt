package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.data.notification.LikNotificationManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.PairingRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.MainChatScreen
import com.example.ui.screens.OurSpaceScreen
import com.example.ui.screens.PairingScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.LikTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CallViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.PairingViewModel
import com.example.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var pairingRepository: PairingRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var callRepository: CallRepository

    private lateinit var authViewModel: AuthViewModel
    private lateinit var pairingViewModel: PairingViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var callViewModel: CallViewModel
    private lateinit var themeViewModel: ThemeViewModel

    private val targetDestination = MutableStateFlow<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Android O+ Notification Channel
        LikNotificationManager.createNotificationChannel(applicationContext)

        // Request POST_NOTIFICATIONS on Android 13+ (API 33+)
        requestNotificationPermissionIfNeeded()

        // Check if opened from notification
        handleNotificationIntent(intent)

        // Initialize Data Repositories & ViewModels
        val syncManager = com.example.data.remote.LikRealtimeSyncManager(applicationContext)
        authRepository = AuthRepository(applicationContext, syncManager)
        pairingRepository = PairingRepository(applicationContext, authRepository, syncManager)
        chatRepository = ChatRepository(applicationContext, authRepository, pairingRepository, syncManager)
        callRepository = CallRepository(applicationContext, authRepository, pairingRepository, syncManager)

        authViewModel = AuthViewModel(authRepository)
        pairingViewModel = PairingViewModel(pairingRepository)
        chatViewModel = ChatViewModel(chatRepository)
        callViewModel = CallViewModel(callRepository)
        themeViewModel = ThemeViewModel(applicationContext)

        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val pendingScreen by targetDestination.collectAsState()

            LikTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LikAppContent(
                        authViewModel = authViewModel,
                        pairingViewModel = pairingViewModel,
                        chatViewModel = chatViewModel,
                        callViewModel = callViewModel,
                        themeViewModel = themeViewModel,
                        pendingTargetScreen = pendingScreen,
                        onClearPendingTargetScreen = { targetDestination.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val destination = intent?.getStringExtra(LikNotificationManager.EXTRA_NAVIGATE_TO)
        if (!destination.isNullOrBlank()) {
            targetDestination.value = destination
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LikNotificationManager.isAppInForeground = true
    }

    override fun onPause() {
        super.onPause()
        LikNotificationManager.isAppInForeground = false
    }
}

@Composable
fun LikAppContent(
    authViewModel: AuthViewModel,
    pairingViewModel: PairingViewModel,
    chatViewModel: ChatViewModel,
    callViewModel: CallViewModel,
    themeViewModel: ThemeViewModel,
    pendingTargetScreen: String? = null,
    onClearPendingTargetScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val pairingInfo by pairingViewModel.pairingInfo.collectAsState()
    val activeCall by callViewModel.activeCall.collectAsState()

    var currentScreen by remember { mutableStateOf("chat_list") }

    // Handle deep navigation from notification tap
    LaunchedEffect(pendingTargetScreen, currentUser, pairingInfo.isPaired) {
        if (pendingTargetScreen != null && currentUser != null && pairingInfo.isPaired) {
            currentScreen = pendingTargetScreen
            onClearPendingTargetScreen()
        }
    }

    // Sync active chat state & cancel notifications when reading chat
    DisposableEffect(currentScreen) {
        val isChat = (currentScreen == "chat")
        LikNotificationManager.isChatScreenVisible = isChat
        if (isChat) {
            LikNotificationManager.cancelChatNotifications(context)
        }
        onDispose {
            if (isChat) {
                LikNotificationManager.isChatScreenVisible = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = Pair(currentUser, pairingInfo.isPaired), label = "screen_transition") { state ->
            val user = state.first
            val isPaired = state.second

            when {
                user == null -> {
                    AuthScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = {
                            currentScreen = pendingTargetScreen ?: "chat"
                            onClearPendingTargetScreen()
                        }
                    )
                }

                !isPaired -> {
                    PairingScreen(
                        currentUser = user,
                        pairingViewModel = pairingViewModel,
                        onPairingComplete = {
                            currentScreen = pendingTargetScreen ?: "chat"
                            onClearPendingTargetScreen()
                        }
                    )
                }

                else -> {
                    when (currentScreen) {
                        "profile" -> {
                            ProfileScreen(
                                currentUser = user,
                                pairingInfo = pairingInfo,
                                authViewModel = authViewModel,
                                pairingViewModel = pairingViewModel,
                                themeViewModel = themeViewModel,
                                onBack = { currentScreen = "chat_list" },
                                onLoggedOut = { currentScreen = "chat_list" },
                                onOpenPairing = { pairingViewModel.unpair() },
                                onOpenPrivacyPolicy = { currentScreen = "privacy_policy" }
                            )
                        }
                        "privacy_policy" -> {
                            PrivacyPolicyScreen(
                                onBack = { currentScreen = "profile" }
                            )
                        }
                        "our_space" -> {
                            OurSpaceScreen(
                                currentUser = user,
                                pairingInfo = pairingInfo,
                                chatViewModel = chatViewModel,
                                pairingViewModel = pairingViewModel,
                                onBack = { currentScreen = "chat_list" },
                                onOpenChat = { currentScreen = "chat" }
                            )
                        }
                        "chat" -> {
                            MainChatScreen(
                                currentUser = user,
                                pairingInfo = pairingInfo,
                                chatViewModel = chatViewModel,
                                callViewModel = callViewModel,
                                themeViewModel = themeViewModel,
                                onOpenProfile = { currentScreen = "profile" },
                                onBack = { currentScreen = "chat_list" }
                            )
                        }
                        else -> {
                            ChatListScreen(
                                currentUser = user,
                                pairingInfo = pairingInfo,
                                chatViewModel = chatViewModel,
                                pairingViewModel = pairingViewModel,
                                authViewModel = authViewModel,
                                callViewModel = callViewModel,
                                onOpenChat = { currentScreen = "chat" },
                                onOpenProfile = { currentScreen = "profile" },
                                onOpenPairing = { pairingViewModel.unpair() },
                                onOpenOurSpace = { currentScreen = "our_space" }
                            )
                        }
                    }
                }
            }
        }

        // WebRTC Voice/Video Call Overlay Screen (Triggers over any screen during call)
        if (activeCall != null) {
            CallScreen(callViewModel = callViewModel)
        }
    }
}

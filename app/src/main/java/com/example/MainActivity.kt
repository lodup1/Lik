package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.notification.LikNotificationManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.PairingRepository
import com.example.ui.screens.*
import com.example.ui.theme.LikTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val notificationManager = LikNotificationManager(applicationContext)
        val authRepository = AuthRepository(database.userDao())
        val pairingRepository = PairingRepository(database.userDao())
        val chatRepository = ChatRepository(database.messageDao(), database.goalDao(), notificationManager)
        val callRepository = CallRepository()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
            val pairingViewModel: PairingViewModel = viewModel { PairingViewModel(pairingRepository) }
            val chatViewModel: ChatViewModel = viewModel { ChatViewModel(chatRepository) }
            val callViewModel: CallViewModel = viewModel { CallViewModel(callRepository) }

            LikTheme(darkTheme = isDarkMode) {
                LikApp(
                    authViewModel = authViewModel,
                    pairingViewModel = pairingViewModel,
                    chatViewModel = chatViewModel,
                    callViewModel = callViewModel,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}

@Composable
fun LikApp(
    authViewModel: AuthViewModel,
    pairingViewModel: PairingViewModel,
    chatViewModel: ChatViewModel,
    callViewModel: CallViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentCall by callViewModel.currentCall.collectAsState()
    val messages by chatViewModel.messages.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("chat_list", "our_space", "profile")

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (currentUser == null) {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {}
            )
        } else if (currentUser?.pairedPartnerId == null && currentRoute == "pairing") {
            PairingScreen(
                currentUser = currentUser,
                pairingViewModel = pairingViewModel,
                onPairedSuccess = {
                    navController.navigate("chat_list") {
                        popUpTo("pairing") { inclusive = true }
                    }
                }
            )
        } else {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                            NavigationBarItem(
                                selected = currentRoute == "chat_list",
                                onClick = {
                                    navController.navigate("chat_list") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chats") },
                                label = { Text("Chats") },
                                modifier = Modifier.testTag("nav_chats_tab")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "our_space",
                                onClick = {
                                    navController.navigate("our_space") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Favorite, contentDescription = "Our Space") },
                                label = { Text("Our Space") },
                                modifier = Modifier.testTag("nav_our_space_tab")
                            )
                            NavigationBarItem(
                                selected = currentRoute == "profile",
                                onClick = {
                                    navController.navigate("profile") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile") },
                                modifier = Modifier.testTag("nav_profile_tab")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = if (currentUser?.pairedPartnerId == null) "pairing" else "chat_list",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("pairing") {
                        PairingScreen(
                            currentUser = currentUser,
                            pairingViewModel = pairingViewModel,
                            onPairedSuccess = {
                                navController.navigate("chat_list") {
                                    popUpTo("pairing") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("chat_list") {
                        ChatListScreen(
                            currentUser = currentUser,
                            lastMessage = messages.lastOrNull(),
                            onOpenChat = { navController.navigate("chat_detail") },
                            onStartCall = { isVideo ->
                                callViewModel.startCall(currentUser?.displayName ?: "Partner", isVideo)
                            }
                        )
                    }
                    composable("chat_detail") {
                        ChatScreen(
                            currentUser = currentUser,
                            chatViewModel = chatViewModel,
                            themeViewModel = themeViewModel,
                            onBack = { navController.popBackStack() },
                            onStartCall = { isVideo ->
                                callViewModel.startCall(currentUser?.displayName ?: "Partner", isVideo)
                            }
                        )
                    }
                    composable("our_space") {
                        OurSpaceScreen(
                            currentUser = currentUser,
                            authViewModel = authViewModel,
                            chatViewModel = chatViewModel
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            currentUser = currentUser,
                            authViewModel = authViewModel,
                            pairingViewModel = pairingViewModel,
                            themeViewModel = themeViewModel,
                            onNavigateToPrivacy = { navController.navigate("privacy_policy") }
                        )
                    }
                    composable("privacy_policy") {
                        PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }

        // Active Call Overlay
        currentCall?.let { session ->
            CallScreen(
                callSession = session,
                onEndCall = { callViewModel.endCall() },
                onAcceptCall = { callViewModel.acceptCall() }
            )
        }
    }
}

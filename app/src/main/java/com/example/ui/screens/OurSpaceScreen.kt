package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.GoalItem
import com.example.data.model.User
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ChatViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OurSpaceScreen(
    currentUser: User?,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel
) {
    val goals by chatViewModel.goals.collectAsState()
    var newGoalTitle by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(currentUser?.currentMood ?: "Happy") }

    val daysTogether = remember(currentUser?.anniversaryDate) {
        val start = currentUser?.anniversaryDate ?: (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(100))
        val diff = System.currentTimeMillis() - start
        TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Space 💕", fontWeight = FontWeight.Bold) }
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
            // Days Together Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("DAYS TOGETHER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$daysTogether Days",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Every moment with you is special", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Current Mood Selector
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("How are you feeling today?", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("😊 Happy", "🥰 In Love", "😴 Sleepy", "🥳 Excited", "🥺 Missing You").forEach { mood ->
                                val isSelected = selectedMood == mood.substringAfter(" ")
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val m = mood.substringAfter(" ")
                                        selectedMood = m
                                        authViewModel.updateProfile(
                                            displayName = currentUser?.displayName ?: "You",
                                            avatarEmoji = currentUser?.avatarEmoji ?: "❤️",
                                            status = currentUser?.statusMessage ?: "",
                                            mood = m
                                        )
                                    },
                                    label = { Text(mood, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Our Goals & Bucket List Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Our Goals & Milestones 🎯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Add Goal Input
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        placeholder = { Text("Add couple goal (e.g. Paris trip)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_goal_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newGoalTitle.isNotBlank()) {
                                chatViewModel.addGoal(newGoalTitle)
                                newGoalTitle = ""
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .testTag("add_goal_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color.White)
                    }
                }
            }

            // Goals list
            items(goals, key = { it.id }) { goal ->
                GoalRow(
                    goal = goal,
                    onToggle = { chatViewModel.toggleGoal(goal) },
                    onDelete = { chatViewModel.deleteGoal(goal) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun GoalRow(
    goal: GoalItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = goal.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("goal_checkbox_${goal.id}")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = goal.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (goal.isCompleted) FontWeight.Normal else FontWeight.Medium,
                color = if (goal.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

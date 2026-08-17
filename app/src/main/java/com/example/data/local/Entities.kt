package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val pairCode: String,
    val pairedPartnerId: String?,
    val avatarEmoji: String,
    val statusMessage: String,
    val currentMood: String,
    val anniversaryDate: Long?
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean,
    val messageType: String,
    val mediaUri: String?,
    val reaction: String?
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isCompleted: Boolean,
    val targetDate: String?,
    val category: String
)

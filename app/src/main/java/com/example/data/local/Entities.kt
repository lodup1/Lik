package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val mediaType: String,
    val mediaUrl: String?,
    val mediaSizeFormatted: String?,
    val uploadProgress: Float,
    val timestamp: Long,
    val status: String,
    val replyToId: String?,
    val replyToText: String?,
    val replyToSenderName: String?,
    val isDeleted: Boolean
)

/**
 * Persisted registered user account table.
 * Enforces unique usernames and stores hashed passwords.
 */
@Entity(
    tableName = "registered_accounts",
    indices = [Index(value = ["username"], unique = true)]
)
data class RegisteredAccountEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val dateOfBirth: String, // e.g. "YYYY-MM-DD"
    val displayName: String,
    val avatarEmoji: String = "❤️",
    val customAvatarUrl: String? = null,
    val statusMood: String? = null,
    val pairCode: String,
    val pairedUserId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: String,
    val username: String,
    val dateOfBirth: String,
    val displayName: String,
    val avatarEmoji: String,
    val customAvatarUrl: String?,
    val statusMood: String?,
    val pairCode: String,
    val pairedUserId: String?,
    val isLoggedIn: Boolean
)

@Entity(tableName = "partner_profile")
data class PartnerProfileEntity(
    @PrimaryKey val partnerId: String,
    val partnerUsername: String,
    val partnerDisplayName: String,
    val partnerAvatarEmoji: String,
    val partnerAvatarUrl: String?,
    val partnerStatusMood: String?,
    val partnerPhoneNumber: String? = null,
    val isPaired: Boolean,
    val connectedSinceTimestamp: Long = System.currentTimeMillis(),
    val anniversaryDateMs: Long = System.currentTimeMillis()
)
